//object opt {

// +A covariance:
//  if Dog subtype Animal, than List[Dog] subtype List[Animal]

// this is type-constructor
// this is some box on ANY type. The box with some logic/functions: isEmpty, get, etc...
sealed trait Option[+A] {
  def isEmpty: Boolean = this match {
    // match on type's constructor
    case Option.Some(_) => false
    case Option.None => true
  }

  def get: A = this match {
    case Option.Some(v) => v
    // with this signature we MUST throw exception. This exceptions is ok. Get is not pur method
    // before get on option -> we check is empty - this is standard pattern
    case Option.None => throw new IllegalArgumentException("try get on Option.None")
  }

  // lower type bounds: B is same or supertype of A : https://blog.knoldus.com/scala-type-bounds/
  def getOrElse[B >: A](default: B): B = this match {
    case Option.Some(v) => v
    case Option.None => default
  }

  // this functions allow us apply some `f` function in value that hold in out the box
  //  This transform absolutely safety
  def map[B](f: A => B): Option[B] = this match {
    case Option.Some(v) => Option.Some(f(v))
    case Option.None => Option.None
  }

  // flat map: w/o some-nested of elements:
  //  by logic: Option[A] => Option[Option[A]]
  //  by flat map: Option[A] => Option[B]

  // what is flatmap?:
  def flatMap[B](f: A => Option[B]): Option[B] = this match {
    case Option.Some(v) => f(v)
    case Option.None =>Option.None
  }

  // Returns the nested scala.Option value if it is nonempty. Otherwise, return None.
  //  Unwrap Option[Option[A]]
  def flatten: Option[A] = this match {
    //case Option.Some(v) => this match {
    case Option.Some(Option.Some(v)) => this match {
      case Option.Some(g) => {
        println(s"internal: v: $v, g: $g, this: $this")
        Option.Some(g)
      }
      case Option.None => Option.None
    }
    case Option.None => Option.None
  }

  def printIfAny: Unit = {
    this match {
      case Option.Some(v) => println(v)
      case Option.None => ()
    }
  }

  def zip[B >: A](b: Option[B]): Option[(A, B)] = {
    this.flatMap(x => b.map(y => (x,y)))
  }

  def filter(f: A => Boolean):Option[A] = this match {
    case Option.Some(v) if f(v) => Option.Some(v)
    //case Option.Some(v) if !f(v) => Option.None
    case _ => Option.None
  }
}



// this is type
object Option {
  case class Some[A](v: A) extends Option[A]
  // when we haven't value -> we cant apply our function
  case object None extends Option[Nothing]
}

// Option - это новый тип, который может принимать 2 значения:
// Some если есть, None если нет

// next we can create some useful methods... is Empty etc

val opt1: Option[Int] = Option.Some(2)
val opt2: Option[String] = Option.Some("hw")

println(opt1)
println(opt1.isEmpty)

println(opt2)

val opt3 = Option.Some[Int](1)
println(opt3.map(x => x.toString() + " val"))

val opt4 = Option.Some[Int](1)
println(opt4.flatMap[String](x => Option.Some(s"${x.toString()} val")))

def addOne(x: Int): Option[Int] = Option.Some(x + 1)
def addNone(value: 1): Option[Int] = Option.None
def addFive(x: Int): Option[Int] = Option.Some(x + 5)

/*
  // by hand
  val sum = addOne(1) match {
    case Option.Some(v) => addFive(v)
  }

  println(sum)
 */

val temp = addNone(1)
println(temp)

val sum2 = addNone(1).map(v => addFive(v))
println(sum2)

val sum3 = addOne(1).map(v => addFive(v))
println(sum3)

val sum4 = addOne(1).flatMap(v => addFive(v))
println(sum4)

def a: Option[Int] = Option.Some(1)
def b: Option[Int] = Option.Some(4)
def c: Option[Int] = Option.None

//val sum5 = a.flatMap(v => b)

val sum7 = a.map(i1 => b.map(i2 => i1 + i2))
println(sum7)

val sum5 = a.flatMap(v => b.map(r => v + r))
println(sum5)

/*
def map[B](f: A => B): Option[B] = this match {
  case Option.Some(v) => Option.Some(f(v))
  case Option.None => Option.None
}

def flatMap[B](f: A => Option[B]): Option[B] = this match {
  case Option.Some(v) => f(v)
  case Option.None => Option.None
}
 */

val sum6: Option[Int] = a match {
  case Option.Some(v) => b.map(r => v + r)
}

val sum8: Option[Int] = a match {
  case Option.Some(v) => b match {
    case Option.Some(g) => Option.Some(v + g)
    case Option.None => Option.None
  }
  case Option.None => Option.None
}

println(sum6)
println(sum8)

// flatMap is map with flatten:
val sum9 = a.flatMap(i1 => b.map(i2 => i1 + i2))
val sum10 = a.map(i1 => b.map(i2 => i1 + i2)).flatten

a.printIfAny
c.printIfAny


//opt1: Option[Int] = Option.Some(2)
val opt9: Option[Int] = Option.Some(1)
val opt10: Option[Int] = Option.None

opt9.printIfAny
opt10.printIfAny
//}

val optA: Option[Int] = Option.Some(1)
val optB: Option[String] = Option.Some("a")
val optNone = Option.None

// optA.zip(optB)

// optNone.zip(optA)
// optA.zip(optNone)

optA.filter(x => x > 0)
optA.filter(x => x > 10)