package logaggregation

import org.apache.spark.SparkContext

object AccessLogAnalysis {
  def main(args: Array[String]): Unit = {

    val sc = new SparkContext("local", "LogAggregation")
    val pagecounts = sc.textFile("C:/3ray/workspace/scala/spark-demo/src/main/resources/spark-log-aggregation/data/pagecounts/*")
    pagecounts.take(10)
    pagecounts.take(10).foreach(println)
    pagecounts.count
    val enPages = pagecounts.filter(_.split(" ")(1) == "en").cache
    enPages.count
    val enTuples = enPages.map(line => line.split(" "))
    val enKeyValuePairs = enTuples.map(line => (line(0).substring(0, 8), line(3).toInt))
    enKeyValuePairs.reduceByKey(_ + _, 1).collect.foreach(println)
    
//    enPages.map(line => line.split(" ")).map(line => (line(0).substring(0, 8), line(3).toInt))
//    .reduceByKey(_ + _, 40).filter(x => x._2 > 20000).map(x => (x._2, x._1)).collect.foreach(println)

  }
}