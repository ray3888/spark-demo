package demo

object NQueens {

  def isSafe(col: Int, queens: List[Int], delta: Int): Boolean = {
    false
  } //> isSafe: (col: Int, queens: List[Int], delta: Int)Boolean

  def queens(n: Int): List[List[Int]] = {
    def placeQueens(k: Int): List[List[Int]] =
      if (k == 0) List(List())
      else for {
        queens <- placeQueens(k - 1)
        column <- List.range(1, n + 1)
        if isSafe(column, queens, 1)
      } yield column :: queens
    placeQueens(n)
  } //> queens: (n: Int)List[List[Int]]
  queens(8) //> res1: List[List[Int]] = List()

  def isPrime(n: Int) =
    List.range(2, n) forall (i => n % i != 0) //> isPrime: (n: Int)Boolean

  for {
    i <- List.range(1, 7)
    j <- List.range(1, i)
    if isPrime(i + j)
  } yield (i, j) //> res2: List[(Int, Int)] = List((2,1), (3,2), (4,1), (4,3), (5,2), (6,1), (6,5
  //| ))

}