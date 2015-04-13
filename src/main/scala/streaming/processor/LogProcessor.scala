package streaming.emitter

import java.util.Properties
import java.io.FileInputStream
import java.sql.{Connection, DriverManager, Statement}

import org.apache.spark.streaming.kafka.KafkaUtils

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.storage.StorageLevel

class LogProcessor(val properties: Properties)
  extends Serializable {
  val MYSQL_TABLE = "t_log_processor"

  def parseLog(record: String): Long = {
    val beg = record.indexOf("timelen=")
    if (beg != -1) {
      val end = record.indexOf("&", beg)
      if (end != -1) {
        try {
          record.substring(beg + 8, end).toLong
        } catch {
          case _ => 0l
        }
      } else {
        0l
      }
    } else {
      0l
    }
  }

  def saveResult(result: (Long, Long)): Unit = {
    println("count result: " + result._1 + " " + result._2)

    val mysqlURI = properties.getProperty("mysql.connection")
    val mysqlUser = properties.getProperty("mysql.user")
    val mysqlPasswd = properties.getProperty("mysql.passwd")

    var conn: Connection = null
    var stmt: Statement = null
    try {
      Class.forName("com.mysql.jdbc.Driver")
      conn = DriverManager.getConnection(mysqlURI, mysqlUser, mysqlPasswd)
      stmt = conn.createStatement()

      val sql = "INSERT INTO " + MYSQL_TABLE + " (total_count, total_time_len)" +
        " VALUES(" + result._1 + ", " + result._2 +  ")"

      println("Execute SQL: " + sql)

      stmt.executeUpdate(sql)
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (stmt != null) stmt.close()
      if (conn != null) conn.close()
    }
  }
}


object LogProcessor extends RecordProcess {
  def main(args: Array[String]) {
    if (args.length < 1) {
      println("Usage: LogProcessor config/config.properies")
      System.exit(-1)
    }

    val properties = new Properties()
    properties.load(new FileInputStream(args(0)))

    // Create Application logic
    val master = properties.getProperty("spark.master.url", "local[10]")
    val appName = properties.getProperty("spark.app.name", "StreamingLogProcess")
    val sparkConf = new SparkConf().setMaster(master).setAppName(appName)
    val sc = new SparkContext(sparkConf)

    streamingProcess(sc, properties)
  }

  private def streamingProcess(sc: SparkContext, property: Properties): Unit = {
    val batchDuration = property.getProperty("app.streaming.batchDuration", "60").toInt
    val ssc = new StreamingContext(sc, Seconds(batchDuration))

    val xceivers = property.getProperty("app.streaming.xceivers", "6").toInt
    val zkQuorum = property.getProperty("zookeeper.zkquorum", "localhost:2181")
    val kafkaGroup = Option(property.getProperty("kafka.group"))
    val kafkaTopic = Option(property.getProperty("kafka.topic"))

    if (kafkaGroup.isEmpty || kafkaTopic.isEmpty) {
      println("kafka.group or kafka.topic should be set")
      System.exit(-1)
    }

    val logProcessor = new LogProcessor(property)

    val storageLevel = property.getProperty("app.streaming.memoryCopies", "1").toInt match {
      case 2 => StorageLevel.MEMORY_ONLY_SER_2
      case _ => StorageLevel.MEMORY_ONLY_SER
    }

    val streams = (1 to xceivers) map { _ =>
      KafkaUtils.createStream(ssc, zkQuorum, kafkaGroup.get, Map(kafkaTopic.get -> 1), storageLevel)
    }

    val unions = ssc.union(streams).map(_._2)

    unions.foreachRDD { r =>
      val totalCount = sc.accumulator(0l)
      val totalTimeLen = sc.accumulator(0l)

      r foreach { e =>
        val timeLen = parseRecord(e) { logProcessor.parseLog }
        processRecord(timeLen) { t =>
          totalCount += 1
          totalTimeLen += timeLen
        }
      }

      println("total records: " + totalCount.value)
      println("total time length: " + totalTimeLen.value)

      outputRecord((totalCount.value, totalTimeLen.value)) { t =>
        logProcessor.saveResult(t)
      }
    }

    ssc.start()
  }
}
