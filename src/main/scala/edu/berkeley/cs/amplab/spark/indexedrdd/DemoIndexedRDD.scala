package edu.berkeley.cs.amplab.spark.indexedrdd

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object Test {
    def main(args: Array[String]): Unit = {

        val sparkConf = new SparkConf().setAppName("indexedrdd")
        val sc = new SparkContext(sparkConf)
        // Create an RDD of key-value pairs with Long keys.
        val rdd = sc.parallelize((1 to 1000000).map(x => (x.toLong, 0)))
        // Construct an IndexedRDD from the pairs, hash-partitioning and indexing
        // the entries.
        val indexed = IndexedRDD(rdd).cache()

        // Perform a point update.
        var indexed2 = indexed.put(1234L, 10873).cache()
        println(indexed2.count())
        indexed2 = indexed.put(1234L, 10874).cache()
        println(indexed2.count())
        // Perform a point lookup. Note that the original IndexedRDD remains
        // unmodified.
        println(indexed2.get(1234L)) // => Some(10873)
        println(indexed.get(1234L)) // => Some(0)

        // Efficiently join derived IndexedRDD with original.
        val indexed3 = indexed.innerJoin(indexed2) { (id, a, b) => b }.filter(_._2 != 0)
        indexed3.collect // => Array((1234L, 10873))

        // Perform insertions and deletions.
        val indexed4 = indexed2.put(-100L, 111).delete(Array(998L, 999L)).cache()
        println(indexed2.get(-100L)) // => None
        println(indexed4.get(-100L)) // => Some(111)
        println(indexed2.get(999L)) // => Some(0)
        println(indexed4.get(999L)) // => None
    }
}