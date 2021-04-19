package module1

object variables {


  /**
   * Переменные
   */

  // Scala is oop-language because all is object
  val a = 1 + 1 // the same: 1.+(1)
  // + isn't operator, it is object method
  val b = println("HW")
  // Scala is free, but need some borders, rules

  // Scala is FP because every function in Scala is expression(значение)
  val incF: Int => Int = i => i + 1
  incF(1)
  val p = incF
  val f = incF(2)
  p(2)
  f

  //1. Объявите две константы, одна будет слово "Month", а друга "Year"
  val m: String = "Month"
  val y: String = "Year"
  //2. Объявите переменную счетчик, которую затем можно будет инкрементить
  var inc: Int = 0
  inc += 1

  /**
   * Block expression {}
   * С помощью фигурных скобок, мы можем выделять блоки кода и присваивать их в переменные.
   * При этом тип переменной, будет равен типу последнего выражения в блоке кода, а ее значение - его значению.
   */

  //3. Объявите блок кода, который первым действием сложит числа 3 и 2 и присвоит результат в переменную x,
  // а вторым действием умножит x на 2

  val block: Int = {
    val x = 2 + 3
    x * 2   // return this expression
  }

  // val by default. Var use in you use some optimisations

  /**
   * Управляющие конструкции
   *   if / else
   *   while / do while
   *   for
   */

  /**
   *  Конструкция if / else имеет туже семантику, что и в других ЯП. В зависимости от условия, выполняется либо одна либо
   *  другая ветка.
   *  При этом тип и значение if / else выражения определяется также, как и для блока кода.
   *  Т.е. последним выражением в исполняемой ветке.
   *
   *  ! Если ветки выражения имеют разный тип, то будет взят ближайший общий предок для выражений
   */

  val cond: Boolean = false


  //4. Напишите выражение, которое в зависимости от значения выражения cond будет возвращать "yes" или "no",
  // присвойте его в переменную х1

  val x1: String = if (cond) "Yes" else "No"

  //5. Напишите выражение, но которое в зависимости от значения выражения cond будет печатать "yes" или "no" в консоль,
  // присвойте его в переменную х2

  val x2: Unit = if (cond) println("Yes") else println("No")

  //6. Напишите выражение, которое если значение переменной cond будет true напечатает в консоль "yes", а если
  // false то вернет строку "no",
  // присвойте его в переменную х3
  val x3 = if (cond) println("yes") else "no"


  /**
   * циклы while / do while
   * Повторяют выполнение своего тела, пока условие истинно. Подразумевают наличие side effect.
   * Отличаются моментом, когда происходит проверка условия ДО или ПОСЛЕ выполнения тела цикла
   */

  var c5: Boolean = true
  val x4: Unit = while (c5) {
    1 + 2
    println(1)
  }

  do {

  } while (c5)


  /**
   * цикл for позволяет итерироваться по коллекциям, имеет своеобразный синтаксис с обратной стрелочкой
   */

  for (el <- List(1, 2, 3)) {
    println(el)
  }

  // Range
  for (el <- 0 to 10) {
    println(el)
  }

  // change standard for: //
  val arr: Array[Int] = ???
  for (idx <- arr.indices) {
    println(arr(idx))
  }

  for (idx <- 0 until arr.length) {  // to include, until not
    println(arr(idx))
  }




}