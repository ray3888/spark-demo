package demo

object worksheet {
  Set("a", "b", "c") zip (1 to 3)                 //> res0: scala.collection.immutable.Set[(String, Int)] = Set((a,1), (b,2), (c,3)
                                                  //| )

  def sum(f: Int => Int)(a: Int, b: Int): Int =
    if (a > b) 0 else f(a) + sum(f)(a + 1, b)     //> sum: (f: Int => Int)(a: Int, b: Int)Int

  def a(a: Int, b: Int) = sum(x => x * x)(a: Int, b: Int)
                                                  //> a: (a: Int, b: Int)Int
  a(1, 10)                                        //> res1: Int = 385
  sum(x => x * x)(1, 10)                          //> res2: Int = 385
	
  List("a", "b", "c").length                      //> res3: Int = 3

List("a b", "b c", "c c").flatMap(_.split(" "))   //> res4: List[String] = List(a, b, b, c, c, c)


}