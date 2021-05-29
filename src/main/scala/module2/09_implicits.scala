package module2


object implicits {


  // implicit conversions

  object implicit_conversions{
    /**
     * Расширить возможности типа String, методом trimToOption, который возвращает Option[String]
     * если строка пустая или null, то None
     * если нет, то Some от строки со всеми удаленными начальными и конечными пробелами
     *
     */

    // ф-я которая неявным образом преобразуют нашу строку в StringOps, в нек-ю обертку
    //  и в этой обертке есть такая trimToOption ф-я:
      implicit def strToStringOps(str: String): StringOps = new StringOps(str)

      class StringOps(string: String){
        def trimToOption: Option[String] = Option(string).flatMap{ str =>
          val trimed = str.trim
          if(trimed.isEmpty) None
          else Some(trimed)
        }
      }


    val s1 = "fooBar".trimToOption
    val s2 = "".trimToOption
    val s3 = " ".trimToOption



    // implicit conversions ОПАСНЫ

    // exception in run-time
//    implicit def strInt(str: String): Int = Integer.parseInt(str)

//    "foobar" / 2

    implicit val seq = Seq("a", "b", "c") // val f: Int => String

    def log(str: String) = println(str)

    log(42) // index of bound exception



    // view bounds

    // Создать класс Ordering, который позволит нам сравнивать различные типы

    type Ordering
  }



  object implicit_scopes {


    trait A

    trait B[T] extends A {
      def print: Unit
    }

    object A {
//      implicit val v: B[Bar] = new B[Bar]{
//        override def print: Unit = println("companion object A")
//      }
    }

    // companion object B
    object B{
      implicit val v: B[Bar] = new B[Bar]{
        override def print: Unit = println("companion object B")
      }
    }


    case class Bar()

    // companion object Bar
    object Bar{
      implicit val v: B[Bar] = new B[Bar] {
        override def print: Unit = println("Bar companion")
      }
    }

    // some arbitrary object
    object wildcardImplicits {
      implicit val v: B[Bar] = new B[Bar] {
        override def print: Unit = println("from wildcard import")
      }
    }


    def foo(b: Bar)(implicit m: B[Bar]) = m.print

    //import wildcardImplicits._

    // foo(Bar())

  }

}