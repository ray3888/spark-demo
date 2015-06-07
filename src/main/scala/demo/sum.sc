package demo

object sum {


  def sum(f: Int => Int)(a: Int, b: Int): Int =
    if (a > b) 0 else f(a) + sum(f)(a + 1, b)     //> sum: (f: Int => Int)(a: Int, b: Int)Int

  def a(a: Int, b: Int) = sum(x => x * x)(a: Int, b: Int)
                                                  //> a: (a: Int, b: Int)Int
  a(1, 10)                                        //> res0: Int = 385
  sum(x => x * x)(1, 10)                          //> res1: Int = 385
}