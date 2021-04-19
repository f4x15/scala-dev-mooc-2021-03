package module1

import scala.::
import scala.collection.mutable.ListBuffer

object functions {


  /**
   * Функции
   */

  // def function is most idiomatic functions in Scala
  def sum(x: Int, y: Int): Int = x + y    // this is method

  // function-value, store переменная-объекь
  //      // type of function    // the body of function
  val sum2: (Int, Int) => Int = (x, y) => x + y       // this is object Function2 [Int, Int, Int]
  val sum3: Function2[Int, Int, Int] = (x, y) => x + y

  val xx = sum2
  val xz: (Int, Int) => Int = sum   // convert from method into function-value
  val xy: Int => Int => Int = xz.curried   // Carrin - преобразование фкнкций из несоклькиих аргументов, в несколько функций с 1
  // аргументов

  // мы можем передавть параметры по очереди и получать пром ужуточны функции
  val _: Int => Int = xy(1)
  val _: Int = xy(1)(2)

  // val list2 = List(sum, sum)   // not compile
  // there are two decisions for this:
  val list2: List[(Int, Int) => Int] = List(sum, sum)   // auto convertation from функции-метода, в функцию значение
  val list3 = List(sum _, sum _)  // _ - here хочу сконвертировать функцию метод, в функцию значение
  // command transform functions into object-Function
  def bar (f: (Int, Int) => Int): Int = ???
  bar(sum)
  bar(sum2)

  sum(1, 2)
  sum2(1, 2)
  sum3(1, 2)

  val list = List(sum2, sum2)

  list(0).apply(0, 1)
  list(0)(0, 1)

  // SAM Single Abstract Method

  trait Printer {
    //def print(s: String): Unit
    def apply(s: String): Unit    // call apply w/o `apply`-name
  }

  // trait Function1[Int, Int]{ def apply(x: Int):Int }

  // create instance and realised print-method\
  // => - создаем экземпляр

  val p: Printer = s => println(s)

  // the same
  val p2: Printer = new Printer {
    override def apply(s: String): Unit = println(s)
  }

  p.apply("hello")
  p2("world")

  // trait Function1[Int, Int]{ def apply(x: Int): Int}



  /**
   *  Задание 1. Написать ф-цию метод isEven, которая будет вычислять является ли число четным
   */

  def isEven(i: Int): Boolean = i % 2 == 0

  /**
   * Задание 2. Написать ф-цию метод isOdd, которая будет вычислять является ли число нечетным
   */

  def isOdd(i: Int): Boolean = !isEven(i)

  /**
   * Задание 3. Написать ф-цию метод filterEven, которая получает на вход массив чисел и возвращает массив тех из них,
   * которые являются четными
   */

  def filterEven(l: List[Int]): List[Int] = {
    def go(l: List[Int], acc: List[Int]):List[Int] = {
      l match {
        case ::(head, tail) if head % 2 == 0 => go(tail, head :: acc)    // add in accum
        case head :: tail => go(tail, acc)   // go next
        case Nil => acc
      }
    }

    go(l, Nil)
  }

  def filterEven2(arr: Array[Int]): Array[Int] = arr.filter(p => p % 2 == 0)

  def filterEven3(arr: Array[Int]): Array[Int] = {
    val l: collection.mutable.ListBuffer[Int] = collection.mutable.ListBuffer.empty[Int]

    for (el <- arr) {
      if (el % 2 == 0) l += el
    }

    l.toArray
  }


  /**
   * Задание 3. Написать ф-цию метод filterOdd, которая получает на вход массив чисел и возвращает массив тех из них,
   * которые являются нечетными
   */
  def filterOdd(arr: Array[Int]): Array[Int] = {
    arr.filter(p => p % 0 != 1)
  }

  // ca


  /**
   * return statement
   *
   *
   * val two = (x: Int) => { return x; 2 }
   *
   *
   * def sumItUp: Int = {
   *    def one(x: Int): Int = { return x; 1 }
   *    val two = (x: Int) => { return x; 2 }
   *    1 + one(2) + two(3)
   * }
   */



}