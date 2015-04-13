package streaming.emitter

import java.io.{FileReader, BufferedReader, FileInputStream, IOException}
import java.util.Properties

import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

object LogEmitter {
  def main(args: Array[String]) {
    if (args.length < 1) {
      println("Usage: LogEmitter config/producer.properties")
      System.exit(-1)
    }

    val properties = new Properties()
    properties.load(new FileInputStream(args(0)))

    // Create Kafka producer
    val producerConfig = new ProducerConfig(properties)
    val producer = new Producer[String, String](producerConfig)

    // Get emitter related properties
    val topic = Option(properties.getProperty("emitter.data.topic")) match {
      case Some(t) => t
      case None => throw new IOException("topic is not set")
    }

    val sendNumber = properties.getProperty("emitter.data.sendrate", "10").toInt

    val seedLog = properties.getProperty("emitter.data.source")
    var reader = new BufferedReader(new FileReader(seedLog), 16 * 1024 * 1024)

    while (true) {
      try {
        val beginTime = System.currentTimeMillis()
        val messages = (1 to sendNumber).map { i =>
          var str = reader.readLine()
          if (str != null) {
            new KeyedMessage[String, String](topic, str)
          } else {
            reader.close()
            reader = null
            reader = new BufferedReader(new FileReader(seedLog), 16 * 1024 * 1024)
            str = reader.readLine()
            new KeyedMessage[String, String](topic, str)
          }
        }
        producer.send(messages: _*)

        val endTime = System.currentTimeMillis()
        val spent = endTime - beginTime
        if (spent < 1000) {
          println(s"Finished sending data in $spent ms, sleep ${1000 - spent}ms")
          Thread.sleep(1000 - spent)
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          reader.close()
          System.exit(-1)
      }
    }

    reader.close()
  }
}
