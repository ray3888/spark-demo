
package demo.streaming.sql.join

import scala.collection.mutable.Queue
import scala.reflect.runtime.universe
import org.apache.spark.SparkConf
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Time
import org.apache.spark.sql.SaveMode

/**
 *  This is a Spark Streaming demo for integrating table join operation between historical data and realtime data, some aggregration
 *  operation and MLlib operation.
 *
 *  realtime data is simulated by queueStream, every rdd inside queueStream can be treated as a mini-batch.
 *  in real cases, realtime data source can be Kafka, socket, flume, hdfs...
 *
 *  historical data is selected from vertica, also can be other data sources like jdbc, hdfs, redis...
 *
 *  after getting historical data, it is registered as temp table, which can be queried by sparkSql later
 *  in each mini-batch when real time data arrives, it is also registered as temp table,
 *  the join and aggregation operation can be done by SparkSql at a high level. surely it can also be done by spark streaming API.
 *
 *  the computing result can be saved to any databases, like parquet file(a columnar data format for many data processing system)
 *  which automatically preserve the schema of original data, which can be queried by compatible engine like SparkSql/hive/impala.
 *
 *  MLlib operation can also be done inside mini-batch by MLlib like classification, clustering...
 *
 *  There are streaming machine learning algorithms (e.g. (Streaming Linear Regression], Streaming KMeans, etc.)
 *  which can simultaneously learn from the streaming data as well as apply the model on the streaming data.
 *
 *  In this demo for each mini-batch a simple Correlations is computed by MLlib.
 *
 */
object SqlJoin {
  val sparkConf = new SparkConf().setAppName("SqlJoinDemo")

  def main(args: Array[String]) {

    // Create the context with a 10 second batch size
    val ssc = new StreamingContext(sparkConf, Seconds(10))
    val sqlContext = new org.apache.spark.sql.SQLContext(ssc.sparkContext)
    //    val sqlContext = new org.apache.spark.sql.hive.HiveContext(ssc.sparkContext)
    import sqlContext.implicits._

    //read parquet file
    //    sqlContext.parquetFile("data/joined_tb.parquet").foreach { x => println(x) }

    //load historical data and register as temp table, the data source can be any types like jdbc, hdfs, redis...
    val etl_file = sqlContext.load("jdbc", Map(
      "url" -> "jdbc:vertica://C0046252.itcs.hp.com:5433/poc?ConnectionLoadBalance=1&user=dbadmin&password=poc",
      "dbtable" -> "(select * from dev.etl_file order by ts desc limit 5) t")).registerTempTable("etl_file")
    //sqlContext.sql("select * from elecj_set").show()

    val elecj_test = sqlContext.load("jdbc", Map(
      "url" -> "jdbc:vertica://C0046252.itcs.hp.com:5433/poc?ConnectionLoadBalance=1&user=dbadmin&password=poc",
      "dbtable" -> """(select stamp, pn_dm, die_site_nr, pass_fail_fg,processlk_ky,res_av,res_sd from dev.elecj_test 
                    where stamp in (select stamp from dev.etl_file order by ts desc limit 5)) t""")).cache()
    //    elecj_test.rdd.take(10).foreach { r => println(r) }

    //simulate a real time stream by queueStream for testing, in real cases it can be Kafka, socket, flume, hdfs...
    val lines = ssc.queueStream(Queue(elecj_test.rdd))
    //    lines.print()

    lines.foreachRDD((rdd: RDD[Row], time: Time) => {

      // Convert raw data from stream to DataFrame
      val elecj_test = rdd.map(r => ElecjTest(r.getString(0), r.get(1).toString(), r.getDecimal(2), r.getString(3), r.getDecimal(4), r.getDouble(5), r.getDouble(6))).toDF()
      elecj_test.registerTempTable("elecj_test")

      println(s"========= $time =========")
      val joined_tb = sqlContext.sql("""
              select a.stamp, pn_dm, pass_fail_fg,res_av,res_sd, b.job_folder,b.rows from elecj_test a 
              left join etl_file b on a.stamp=b.stamp limit 5
              """)
      joined_tb.show()
      joined_tb.save("data/joined_tb.parquet", SaveMode.Append)

      //save as hive table
      //      joined_tb.saveAsTable("joined_tb", SaveMode.Append)

      sqlContext.sql("""
                select stamp, count(*) from elecj_test a group by stamp
                """).show()

      val corrRDD = sqlContext.sql("""select die_site_nr,res_av from elecj_test""").rdd
      val ct = corrRDD.count()
      println(s"corrRDD.count: $ct")
      if (ct > 0) {
        val labelRDD = corrRDD.map { x => x.getDecimal(0).doubleValue() }
        val featureRDD = corrRDD.map { x => x.getDouble(1) }
        val corr = Statistics.corr(labelRDD, featureRDD)
        println(s"label:die_site_nr, feature:res_av \tcorr: $corr")
      }

    })

    ssc.start()
    ssc.awaitTermination()
  }
}

/** Case class for converting RDD to DataFrame */
case class ElecjTest(stamp: String, pn_dm: String, die_site_nr: BigDecimal, pass_fail_fg: String, processlk_ky: BigDecimal, res_av: Double, res_sd: Double)

