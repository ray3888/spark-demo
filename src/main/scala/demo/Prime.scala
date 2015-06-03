package demo

object Prime {
  def main(args: Array[String]): Unit = {

    def isPrime(n: Int) =
      List.range(2, n) forall (i => n % i != 0) //> isPrime: (n: Int)Boolean

    for {
      i <- List.range(1, 7)
      j <- List.range(1, i)
      if isPrime(i + j)
    } yield (i, j);

    List.range(1,100) filter isPrime foreach println _
  }
}