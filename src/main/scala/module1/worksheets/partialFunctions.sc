import scala.collection._

object test {

  def main(args: Array[String]): Unit =
    (i: Nothing) => {
      val list: _root_.scala.collection.immutable.List[Int] =
        scala.List.apply(1, 2, 3, 4)

      // list.foreach(println)
      Predef.println("list.foreach(println)")
      list.foreach((x: Int) => Predef.println(x))
      // return Funtion(Int) => {(a: Any) => Println(a)

      // list.foreach(println(_))
      Predef.println("list.foreach(println(_))")
      list.foreach(Predef.println(i))
      // return forach(println) -> w/o additional function wrapper
    }
}
