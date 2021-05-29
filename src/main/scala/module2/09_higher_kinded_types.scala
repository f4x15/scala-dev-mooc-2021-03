package module2


object higher_kinded_types{

  // есть похожая логика
  def tuple[A, B](a: List[A], b: List[B]): List[(A, B)] =
    a.flatMap{ a => b.map((a, _))}

  def tuple[A, B](a: Option[A], b: Option[B]): Option[(A, B)] =
    a.flatMap{ a => b.map((a, _))}

  def tuple[E, A, B](a: Either[E, A], b: Either[E, B]): Either[E, (A, B)] =
    a.flatMap{ a => b.map((a, _))}

  // difference in realisations of functions: map, flatMap
  // what is a container: List, Option, Either

  // we want create some-common routine for this:
  // F[_] - some container
  // A, B - some types
  // F[A], F[B] - container from A and B
  def tuplef[F[_], A, B](fa: F[A], fb: F[B]): F[(A, B)] = ???


  // F[_] - some container type
  // A - some A-type
  // THis interface have name: `higher_kinded_types`. THis name because get `type-constructor` in internal.
  // Те это абстракция над типом, который в свою очередь абстрагируется над типом, получается абстракция над
  //  абстракцией, и никак иначе. higher
  //
  // Интуиция здесь такая же что и в high-order-functions: when s function get/return other functions
  // Bindable - является type-constructor, потому что чтобы он стал типом, ему нужно передать некоторые типы
  //  но в качестве одного из параметров мы указывает тип F[_] который в свою очередь является type-constructor.
  //
    
    /*
    Type constructor: for create concrete type we need pass some type:
    List[A] - type constructor because for create concrete type we pass some A-type

  List[A] being a type constructor:
  That is:
    -List[A] takes a type parameter (A),
  -by itself it’s not a valid type, you need to fill in the A somehow - "construct the type",
  -by filling it in with String you’d get List[String] which is a concrete type.

  from: https://stackoverflow.com/a/52995827/930014
  */
    
    // higher_kinded_types short: тип который абстрагируется над конструктором типа: trait M[F[_]]

  abstract class Bindable[F[_], A] {
    def map[B](f: A => B): F[B]
    def flatMap[B](f: A => F[B]): F[B]
  }
  trait Bindable2[F[_], A] {
    def map[B](f: A => B): F[B]
    def flatMap[B](f: A => F[B]): F[B]
  }

  // Bindable - это пример как мы применяем Higher Kinded types. Абстрагируемся над неким type-constructor.
  //  и далее как его используем

  // try create some realization
  def tupleBindable[F[_], A, B](fa: Bindable[F, A], fb: Bindable[F, B]): F[(A, B)] =
    fa.flatMap{ a => fb.map((a, _))}

  // convert A-to->listBindable
  def listBindable[A](list: List[A]): Bindable[List, A] = new Bindable[List, A] {
    override def map[B](f: A => B): List[B] = list.map(f)

    override def flatMap[B](f: A => List[B]): List[B] = list.flatMap(f)
  }

  // convert A-to->optBindable
  def optBindable[A](list: Option[A]): Bindable[Option, A] = new Bindable[Option, A] {
    override def map[B](f: A => B): Option[B] = list.map(f)

    override def flatMap[B](f: A => Option[B]): Option[B] = list.flatMap(f)
  }


  val optA = Some(1)
  val optB = Some(2)

  val list1 = List(1, 2, 3)
  val list2 = List(4, 5, 6)

  println(tupleBindable(optBindable(optA), optBindable(optB)))
  println(tupleBindable(listBindable(list1), listBindable(list2)))


}