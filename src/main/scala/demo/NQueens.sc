package demo

object worksheet {
 
  def isSafe(col: Int, queens: List[Int], delta: Int): Boolean = {
    if (queens.length == delta - 1) true
    else {
      col != queens(delta - 1) &&
        col - queens(delta - 1) != queens.length + 1 - delta &&
        col - queens(delta - 1) != delta - (queens.length + 1) &&
        isSafe(col, queens, delta + 1)
    }
  }                                               //> isSafe: (col: Int, queens: List[Int], delta: Int)Boolean

  def queens(n: Int): List[List[Int]] = {
    def placeQueens(k: Int): List[List[Int]] =
      if (k == 0) List(List())
      else for {
        queens <- placeQueens(k - 1)
        column <- List.range(1, n + 1)
        if isSafe(column, queens, 1)
      } yield column :: queens
    placeQueens(n)
  }                                               //> queens: (n: Int)List[List[Int]]
  queens(4)                                       //> res0: List[List[Int]] = List()
}