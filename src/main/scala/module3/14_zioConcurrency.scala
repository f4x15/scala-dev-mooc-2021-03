package module3

import module3.zioConcurrency.getExchangeRatesLocation1
import zio.{Ref, UIO, URIO, ZIO, clock}
import zio.clock.{Clock, sleep}
import zio.console.{Console, putStr, putStrLn}
import zio.duration.durationInt
import zio.internal.Executor

import java.util.concurrent.TimeUnit
import scala.language.postfixOps

object zioConcurrency {

  // ZIO fiber. concurrency is green threads. Work with user-space.
  //  - в них последовательно выполняются инструкции
  //  - можно создать 10К файберов, они дешевые
  //  - можно безопастно прерывать - финалайзеры выполняются, все ок
  //  - join (получать результат вычисления) w/o blocking
  //  В рамках jvm у нас есть набор threadpools на которых ZIO-runtime распределяет файберы
  //  Любое ZIO-приложение сводиться к одному эффекту, который мы запускаем в main-methods
  //
  // 2 главных типа действий:
  //  - fork: чтобы данный эффект который мы фораем выполнялся конкуррентно
  //  - Fiber[E, A]: join - получаем результат A в контекстетекущего вычисления
  //  join in ZIO это не блокирующая операция, текущий файбер он конечно приостановит, чтобы получиться
  //  результат, но при этом, никакой тред находящийся за эим не будет заблокирован
  //   Здесь просто будет зареган колбек, и когда будет результат - этот колбек продолжит выполнение когда получит
  //    результат.
  //

  // эфект содержит в себе текущее время
  val currentTime: URIO[Clock, Long] = clock.currentTime(TimeUnit.SECONDS)


  /**
   * Напишите эффект, который будет считать время выполнения любого эффекта
   */


    // сначала описываем как померить время выполнения эффекта
    // выполение последовательно, если случиться ошибка все приостановится
  def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]) = for {
    startTime <- currentTime
    r <- zio
    endTime <- currentTime
    _ <- putStrLn(s"elapsed time: ${endTime - startTime}")
  } yield r // result of execution of effect


  val exchangeRates: Map[String, Double] = Map(
    "usd" -> 76.02,
    "eur" -> 91.27
  )


  /**
   * Эффект который все что делает, это спит заданное кол-во времени, в данном случае 1 секунду
   */
  // не блокирует никаких потоков
  //  тут создает ффект, который вернет результат спустя секунду
  val sleep1Second = ZIO.sleep(1 seconds)

  /**
   * Эффект который все что делает, это спит заданное кол-во времени, в данном случае 3 секунду
   */
  val sleep3Seconds = ZIO.sleep(3 seconds)

  /**
   * Создать эффект который печатает в консоль GetExchangeRatesLocation1 спустя 3 секунды
   */
    // сначала спик 3 секунды, потом чтото печатает в консоль
    // *>   zipRight, this is flatMap that ignore result in the left
  lazy val getExchangeRatesLocation1 = sleep3Seconds *> putStrLn("GetExchangeRatesLocation1")

  /**
   * Создать эффект который печатает в консоль GetExchangeRatesLocation2 спустя 1 секунду
   */
  lazy val getExchangeRatesLocation2 = sleep1Second *> putStrLn("GetExchangeRatesLocation2")



  /**
   * Написать эффект котрый получит курсы из обеих локаций
   */
  lazy val getFrom2Locations: ZIO[Console with Clock, Nothing, (Unit, Unit)] =
    getExchangeRatesLocation1 zip getExchangeRatesLocation2
  // zip скомбинирует два эффекта в один, результатом которого будет кортеж


  /**
   * Написать эффект котрый получит курсы из обеих локаций паралельно
   */
  lazy val getFrom2LocationsInParallel = for {
    fiber1 <- getExchangeRatesLocation1.fork
    res2 <- getExchangeRatesLocation2
    res1 <- fiber1.join    // весь эффекс getFrom2LocationsInParallel засуспендиться пока мы не получим
                          // ответ , который получаем в fiber1. join is blocking operation.
                          // указываем тут, чтобы получить результат вычисления этого файбера, в контексте этого
                          // эффекта
  } yield (res1, res2)    // и уже только после этого елдим результат
  // т.е. комбинация эффектов происходит непоследовательно а выполнение не последовательне, если этого хотим



  /**
   * Предположим нам не нужны результаты, мы сохраняем в базу и отправляем почту
   */


   val writeUserToDB = sleep1Second *> putStrLn("User in DB")
   val sendMail = sleep1Second *> putStrLn("Mail sent")

  /**
   * Написать эффект котрый сохранит в базу и отправит почту паралельно
   */

  // can with zip, но это будет не паралельно. Для паралельности fork
  lazy val writeAndSand = for{
    _ <- writeUserToDB.fork
    _ <- sendMail.fork
  } yield ()
  // running time 0

  /**
   *  Greeter
   */

  lazy val greeter = for {      // parent fiber, where we make first `fork`
    _ <- (sleep1Second *> putStrLn("Hello")).forever.fork // родительский скоуп схлопывается `for ` и закрывает дитя `fork
    // what we do this:
    // 1. строим комбинированный эффект: zipRight
    // 2. запускаем его бесконечно
    // 3. форкаем то что получилось (не бесконечное кол-во раз а один раз)
    // 4. родидельский файбер завершается, т.е. завершается ребенок сразу же, и вызовет прерывание child эффекта
    // forever as alias while-true`
    // _ <- sleep(5 seconds)
  } yield ()
  // if w/o sleep - elapsed time 0, w/o greet
  // if with sleep 5^ 5 hello and elapsed time 5 sec
  // w/o sleep the parent fiber finished asap (сразу). and finalise all children fibers

  /***
   * Greeter 2
   */
  lazy val greeter2 = ZIO.effectTotal(while (true) println("Hello"))

  val greeterApp = for {
    fiber <- greeter2.fork
    _ <- fiber.interrupt  // не прерывает, т.к. зио может прерывать только в промежутках между инструкциями,(эффектами)
                          // а если она уже выполняется то прервать он уже не может
  } yield ()
  // infinite `HELLO`

  /* Разница в поведении как у акторов в акке. Supervision model.
     Q: При Join создается родительскй или дочерний fiber??? Да, создается родительский, который кладет ранишний родительский
     к себе в скоуп (цепочку выполнений.
     При fork создается новый родитеьский файбер, который включает ранешний (родительский) и новый (вновь созданный).
     Ранее созданный будет лежать в результате вновь созданного.

     При fork каждый последуюий файбер вкладывается в предыдущий. Тот скоуп где происход fork
     Он является родительским по отнощению к тому скоупу который был форкнут. При завершении работы файбера, его
     скоуп закрывается и автоматически закрываются скоупы его дочерних файберов.

     Текущий файбер становится родителя, для того файбера который форкаем.
     Q: Так какой получается родительский, какой дочерний????

   */


    /*
      Лучше делать эффекты максимально компактными (мелкие) и потом с помошью композиции собираем большие эффекты, за счет
      композиции. Иначе, напримре, он не сможет прервать выполнение эффекта, если мы сделаем всю программу целиком одним
      эффектом.

      Зио-файбер это просто структура данных, которая интерпритируется Зио-рантаймом.
      Thread.sleep() VS ZIO.sleep: thread блокирует потом, а ЗИО слип поток не блокирует (тут просто зарегистрируется
      кэллбэк)

     */

  /**
   * Прерывание эффекта
   */

   val app3 = for {
    fiber <- getExchangeRatesLocation1.fork
    _ <- getExchangeRatesLocation2
    _ <- fiber.interrupt
    _ <- sleep(3 seconds)
   // sleep не учыпляет текущий файбер, он сздает эффект, который ждет какое-о кол-во секунл: он спит и джойнит
   } yield ()



  /**
   * Получние информации от сервиса занимает 1 секунду
   */
  /*
    Ref mutable reference on mutable data. Alll operations atimic and thread-safe.
    Ref is method to manage stage in our state-less application. "Update state in functional way."
   */
  def getFromService(ref: Ref[Int]) = for {
    count <- ref.getAndUpdate(_ + 1)
    _ <- putStrLn(s"GetFromService - ${count}") *> ZIO.sleep(1 seconds)
  } yield ()

  /**
   * Отправка в БД занимает в общем 5 секунд
   */
  def sendToDB(ref: Ref[Int]): ZIO[Clock with Console, Exception, Unit] = for {
    count <- ref.getAndUpdate(_ + 1)
    _ <- ZIO.sleep(5 seconds) *> putStrLn(s"SendToDB - ${count}")
  } yield ()


  val counterRef: UIO[Ref[Int]] = Ref.make(0)
  // we can hold in the Ref only immutable data
  // as we return ZIO we can chain this effects - see `flatMap`.
  // There is no way to access the shared state outside the monadic operations

  val stringRefSample = for {
    ref <- Ref.make("some text is ref")
    _ <- ref.set("new some text")   // atomic update operations, concurrency safe
    value <- ref.get
  } yield (value)

  val counterInitial = 0
  val counterVal = for {
    counterRef <- Ref.make(counterInitial)
    _ <- counterRef.update(_ + 1)     // atomic update our ref
    value <- counterRef.get
  } yield value

  // safe in concurrent environment
  def request(counter: Ref[Int]) = {
    for {
      rn <- counter.modify(c => (c + 1, c + 1))
      _ <- putStrLn(s"Current counter is $rn")
    } yield ()
  }


  /**
   * Написать программу, которая конкурентно вызывает выше описанные сервисы
   * и при этом обеспечивает сквозную нумерацию вызовов
   */
  lazy val app1 = for {
    counter <- Ref.make(0)
    _ <- getFromService(counter).fork
    _ <- sendToDB(counter).fork
    _ <- sleep(6 seconds)
  } yield ()

  /**
   *  Concurrent operators
   */

  // fiber, fork, join - low level primitives
  // Exist more high-level operators. Its have `pair` parts in name

  val succ = ZIO.succeed(2)
  succ.zipPar(succ)

   val _ = getExchangeRatesLocation1 zipPar getExchangeRatesLocation2
     // параллельное выполнение эффектов. Время выполнения == макс время выполннения одного из них.
     //  если упадет ЛОкатион1, то Локатион2 будет заинтеррапчен


   val _ = ZIO.foreachPar(List("1", "2", "3"))(str => putStrLn(str))
    // все вычисления будет запущены паралельно для каждого элемента массива.

   val _ = getExchangeRatesLocation1 race getExchangeRatesLocation2
   // результат этого эфекта будет результат выполнения или одного эффекта или второго. Кто быстрее того и тапки))
   // для эффекта-лузера, ЗИО автоматически запустит интеррапт

  /**
   * Lock
   *
   * Это то, на каком экзекутере наши файберы будут выполняться
   * executor - это то где выполняется файберы
   * on - для интеропа со скала библиотеками
   */

  // Правило 1: когда эффект залочен на экзекутор все части этого эффекта будут выполнены на этом экзекутере
  lazy val doSomething: UIO[Unit] = ???
  lazy val doSomethingElse: UIO[Unit] = ???

  lazy val executor: Executor = ???
  // это тот набор реальных потоков, на которых будут выполняться наши файберы

  lazy val eff = for{
    f  <- doSomething.fork
    _ <- doSomethingElse
    _ <- f.join
  } yield ()

  lazy val result = eff.lock(executor)
  // этот наш эффект нужно выполнять на вот этом вот экзекутере - сами настраиваем на какаом экзекутере выполнять тот
  //  или иной эффект. Ткк правило 1, то оба эффекта (doSomething и doSomethingElse) будут выполнены на executor (оба
  //  файбера будут запущены на специфицированном executors)
  //  Есть два препопределенных экзекутера: 1 общий, 2 для блокирующих операций
  /*    Если жффекты нарпавленны на IO, для блокирующих операций -> для этого нужно использовать выделенный экзекутор
      + Есть операторы ZIO.blocking которые позволяют выполнять блокирующие операции на специально предназначенном для
      этого экзекутере.
    важно поделить операции на блокирующие (IO) и те которые делают какие то вычисления на CPU (неблокирующие). И важно
    выполнять блокирующие операции на блокирующем экзекьютере, который "раздувается" до бесконечных размеров, и что в,
    конечном итоге не приведет к тому что вы выбижите за пределы доступных вам потоков.

    А все операции которые не приводят к блокировке, которые не завязаны на IO, можно выполнять на основном экзекутере.

    IO execution context >> рост до бесконечности для блокирующих операций
    CPU-bound execution context

    В ZIO предопределенные операции ZIO.bloking packge

    Q: как называются эти экзекуторы???

    Любая IO-операция залочит тред. И файбер и тред.
   */


  // Правило 2: внутренние скоупы имеют более высокий приоритет
  lazy val executor1: Executor = ???
  lazy val executor2: Executor = ???



  lazy val eff2 = for{
    f <- doSomething.lock(executor2).fork // гарантировано на exceutor2
    _ <- doSomethingElse        // это на executor1
    _ <- f.join
  } yield ()

  lazy val result2 = eff2.lock(executor1)   // внешний файбер для for


  /**
   * простая гонка эффектов
   */
  val res1: URIO[Clock, Int] = ZIO.sleep(3.second).as(4)

  val res2: URIO[Clock, Int] = ZIO.sleep(1.second).as(7)

  val raceResult = res1 race res2

  val res3 = for {
    res <- raceResult
    _ <- putStrLn(res.toString)
  } yield ()

}