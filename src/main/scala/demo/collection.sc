package demo

object collection {
  Set("a", "b", "c") zip (1 to 3) toMap           //> res0: scala.collection.immutable.Map[String,Int] = Map(a -> 1, b -> 2, c -> 3
                                                  //| )

  List("a b", "b c", "c c").flatMap(_.split(" ")) //> res1: List[String] = List(a, b, b, c, c, c)\
}