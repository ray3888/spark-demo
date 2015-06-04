package demo

object worksheet {
  Set("a", "b", "c") zip (1 to 3) toMap           //> res0: scala.collection.immutable.Map[String,Int] = Map(a -> 1, b -> 2, c -> 3
                                                  //| )
  def factorial2(s: Int, n: Int): Int = if (n == 0) s else factorial2(s * n, n - 1)
                                                  //> factorial2: (s: Int, n: Int)Int
  factorial2(1, 5)                                //> res1: Int = 120

  def sum(f: Int => Int)(a: Int, b: Int): Int =
    if (a > b) 0 else f(a) + sum(f)(a + 1, b)     //> sum: (f: Int => Int)(a: Int, b: Int)Int

  def a(a: Int, b: Int) = sum(x => x * x)(a: Int, b: Int)
                                                  //> a: (a: Int, b: Int)Int
  a(1, 10)                                        //> res2: Int = 385
  sum(x => x * x)(1, 10)                          //> res3: Int = 385

  List("a", "b", "c").length                      //> res4: Int = 3

  val footballTeamsAFCEast =
    Map("New England" -> "Patriots",
      "New York" -> "Jets",
      "Buffalo" -> "Bills",
      "Miami" -> "Dolphins",
      "Los Angeles" -> null)                      //> footballTeamsAFCEast  : scala.collection.immutable.Map[String,String] = Map(
                                                  //| Buffalo -> Bills, New England -> Patriots, Los Angeles -> null, New York -> 
                                                  //| Jets, Miami -> Dolphins)

  footballTeamsAFCEast.get("Miami")               //> res5: Option[String] = Some(Dolphins)
  footballTeamsAFCEast.get("Los Angeles")         //> res6: Option[String] = Some(null)
  footballTeamsAFCEast.get("Sacramento")          //> res7: Option[String] = None


}