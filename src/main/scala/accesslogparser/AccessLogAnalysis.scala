package accesslogparser

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

object AccessLogAnalysis {

  def main(args: Array[String]) = {
    val sc = new SparkContext("local", "LogAggregation")
    val log = sc.textFile("C:/3ray/workspace/scala/spark-demo/src/main/resources/accesslog/sampledata")
    val p = new AccessLogParser
    //    log.foreach(println)
        get404(log)
//    moreQueries(log)
  }

  def get404(log: RDD[String]) {
    val p = new AccessLogParser
    val distinctRecs = log.filter(line => getStatusCode(p.parseRecord(line)) == "404").map(getRequest(_))
      .collect { case Some(requestField) => requestField }
      .map(extractUriFromRequest(_))
      .distinct
    distinctRecs.count
    distinctRecs.foreach(println)
  }

  def moreQueries(log: RDD[String]) {
    val p = new AccessLogParser
    // get the URIs corresponding to 404 requests
    // ------------------------------------------

    // create a "null object" AccessLogRecord for use with `getOrElse`
    // todo: adding the "GET" request here was a poor hack; get rid of it
    val nullObject = AccessLogRecord("", "", "", "", "GET /foo HTTP/1.1", "", "", "", "")

    // a list of 404 requests, like "GET /foo-bar HTTP/1.0"
    // works, but requires double-parsing
    val recs = log.filter(p.parseRecord(_).getOrElse(nullObject).httpStatusCode == "404")
      .map(p.parseRecord(_).getOrElse(nullObject).request)

    // find the URIs with the most hits
    // --------------------------------

    // use the previous example to get to a series of "(URI, COUNT)" pairs; (MapReduce like)
    val uriCounts = log.map(p.parseRecord(_).getOrElse(nullObject).request)
      .map(_.split(" ")(1))
      //      .filter(_ != "/foo")
      .map(uri => (uri, 1))
      .reduceByKey((a, b) => a + b)

    val uriToCount = uriCounts.collect // (/foo, 3), (/bar, 10), (/baz, 1) ...
    // what i want: URIs sorted by hit count, highest hits first

    import scala.collection.immutable.ListMap
    //    uriToCount.toSeq.foreach(println)
    val uriHitCount = ListMap(uriToCount.toSeq.sortWith(_._2 > _._2): _*) // (/bar, 10), (/foo, 3), (/baz, 1)
    
    //     this is a decent way to print some sample data
    uriHitCount.take(10).foreach(println)
  }

  // val request = "GET /foo HTTP/1.0"
  def extractUriFromRequest(requestField: String) = requestField.split(" ")(1)

  //  get the `request` field from an access log record
  def getRequest(rawAccessLogString: String): Option[String] = {
    val p = new AccessLogParser
    val accessLogRecordOption = p.parseRecord(rawAccessLogString)
    accessLogRecordOption match {
      case Some(rec) => Some(rec.request)
      case None      => None
    }
  }

  def getStatusCode(line: Option[AccessLogRecord]) = {
    line match {
      case Some(l) => l.httpStatusCode
      case None    => "0"
    }
  }

}