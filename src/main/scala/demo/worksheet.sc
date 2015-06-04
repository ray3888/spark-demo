package demo

object worksheet {

  List("a b", "b c", "c c").flatMap(_.split(" ")) //> res0: List[String] = List(a, b, b, c, c, c)

}