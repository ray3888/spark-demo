package demo

object worksheet {
  Set("a","b","c")                                //> res0: scala.collection.immutable.Set[String] = Set(a, b, c)
  Set("a","b","c") zip (1 to 3) toMap             //> res1: scala.collection.immutable.Map[String,Int] = Map(a -> 1, b -> 2, c -> 3
                                                  //| )
}