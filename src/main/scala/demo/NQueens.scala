package demo

object NQueens {
  def main(args: Array[String]): Unit = {
    def isSafe(col: Int, queens: List[Int], delta: Int): Boolean = {
      if (delta == queens.length+1) true
      else {
      println(col+" "+queens)
        col-1 != queens(delta - 1) &&
          col-1 - queens(delta - 1) != queens.length+1 - delta &&
          col -1- queens(delta - 1) != delta - (queens.length+1) &&
          isSafe(col, queens, delta + 1)
      }
    } 

    def queens(n: Int): List[List[Int]] = {
      def placeQueens(k: Int): List[List[Int]] =
        if (k == 0) List(List())
        else for {
          queens <- placeQueens(k - 1)
          column <- List.range(1, n + 1)
          if isSafe(column, queens, 1)
        } yield column :: queens
      placeQueens(n)
    }
    queens(4).foreach(x=>println("*"+x))
//   println( List.range(1, 5)::List(List.range(1,5)))
  }
}