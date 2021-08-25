import cats._
import cats.implicits._
import higherkindness.droste._
import higherkindness.droste.data._
import org.scalacheck.{ Prop, Properties }
import Prop._

/**
 * In this example, we represent natural numbers
 * essentially as lists without item values:
 *
 * 0 = zero
 * 3 = succ(succ(succ(zero)))
 *
 * We can then define operations such as addition on these.
 */
object NatFProps extends Properties("NatF") {

  /**
   * Endofunctor for (non-generic) F-algebra in the category Scala types:
   * {{{
   * data NatF[+T] = Zero | Succ(n: T)
   * }}}
   *
   * @tparam T argument (type parameter) of the endofunctor
   */
  enum NatF[+T]:
    case Zero
    case Succ[+T](n: T) extends NatF[T]

  /** Typesafe equality for instances of `NatF` */
  given CanEqual[NatF[Nothing], NatF[_]] = CanEqual.derived

  import NatF._

  /**
   * Implicit value for declaring `NatF` as an instance of
   * typeclass `Functor` in scalaz.
   */
  implicit object natFFunctor extends Functor[NatF] {
    override def map[T, U](fa: NatF[T])(f: T => U): NatF[U] = {
      fa match {
      case Zero => Zero
      case Succ(n) => Succ(f(n))
    }
    }
  }

  /**
   * Least fixpoint of `NatF` (recursive type based on `NatF`)
   * as carrier object for initial algebra.
   */
  type Nat = Fix[NatF]

  /** Typesafe equality for instances of `Nat` */
  implicit val natEq: Eq[Nat] = Eq.fromUniversalEquals

  // Factory methods for convenience.
  val zero = Fix[NatF](Zero)
  val succ = (n: Nat) => Fix[NatF](Succ(n))

  // some instances
  val one = succ(zero)
  val two = succ(one)
  val three = succ(two)

  /**
   * Conversion to `Int` as an `NatF`-algebra
   * for carrier object `Int` in the category Scala types.
   */
  val toIntA: Algebra[NatF, Int] = Algebra {
    case Zero => 0
    case Succ(n) if n >= 0 => n + 1
  }

  // Using the catamorphism, we now can fold the `toInt` algebra into instances.
  // (This is an example of recursion.)
  val toInt = scheme.cata(toIntA)
  property("cata0") = Prop { toInt(zero) === 0 }
  property("cata3") = Prop { toInt(succ(succ(succ(zero)))) === 3 }

  /**
   * Conversion from `Int` as an `NatF`-coalgebra
   * for carrier object `Int` in category Scala types
   * (generator for corecursion).
   */
  val fromIntCoA: Coalgebra[NatF, Int] = Coalgebra {
    case 0 => Zero
    case n if n > 0 => Succ(n - 1)
  }

  // Using the anamorphism on a coalgebra such as `fromInt`,
  // we can now unfold a `Nat` from an `Int`.
  // (This is an example of corecursion.)
  val fromInt = scheme.ana(fromIntCoA)
  property("ana0") = Prop { toInt(fromInt(0)) === 0 }
  property("ana7") = Prop { toInt(fromInt(7)) === 7 }
  property("anaForall") = Prop.forAll { (i: Int) => (i >= 0 && i < 100000) ==> { toInt(fromInt(i)) === i } }

  val fromToInt = scheme.hylo(toIntA, fromIntCoA)
  property("hylo0") = Prop { fromToInt(0) === 0 }
  property("hylo7") = Prop { fromToInt(7) === 7 }
  property("hyloBig") = Prop { val big = 5000; fromToInt(big) === big }
  property("hyloForall") = Prop.forAll { (i: Int) => (i >= 0 && i < 100000) ==> { fromToInt(i) === i } }

  // TODO this causes a stack overflow -> need to make stack-safe by converting to hylomorphism
  // property("anaForall") = Prop.forAll { (i: Int) => i >= 0 ==> { toInt(fromInt(i)) === i } }

  /**
   * Addition to a number `m` as an `NatF`-algebra for carrier object
   * `Nat` in the category Scala types.
   *
   * @param m the number to which we are adding the argument of the algebra
   */
  val plusA = (m: Nat) => Algebra[NatF, Nat] {
    case Zero => m
    case Succ(n) => succ(n)
  }

  val plus = (m: Nat) => scheme.cata(plusA(m))

  property("cata00") = Prop { toInt(plus(zero)(zero)) === 0 }
  property("cata03") = Prop { toInt(plus(three)(zero)) === 3 }
  property("cata30") = Prop { toInt(plus(zero)(three)) === 3 }
  property("cata23") = Prop { toInt(plus(three)(two)) === 5 }

  /**
   * Multiplication by a number `m` as an `NatF`-algebra for carrier object
   * `Nat` in the category Scala types.
   *
   * @param m the number by which we are multiplying the argument of the algebra
   */
  val timesA = (m: Nat) => Algebra[NatF, Nat] {
    case Zero => zero
    case Succ(n) => plus(m)(n)
  }

  val times = (m: Nat) => scheme.cata(timesA(m))

  property("cataOnTimes00") = Prop { toInt(times(zero)(zero)) === 0 }
  property("cataOnTimes03") = Prop { toInt(times(three)(zero)) === 0 }
  property("cataOnTimes30") = Prop { toInt(times(zero)(three)) === 0 }
  property("cataOnTimes23") = Prop { toInt(times(three)(two)) === 6 }

  /**
   * Argument function for `para`. Returns `one` when there is no accumulated
   * result yet. Otherwise it multiplies the accumulated result by the current
   * receiver value during traversal, whose tail (out) is passed as `curr` by
   * `para`.
   * By contrast, F-algebras do not have access to the current receiver value
   * during traversal!
   * Exercise: This has a similar type signature as `plus` and `times`. What
   * are the key differences?
   *
   * @return the current receiver times the accumulated result
   */
  //  val oneOrTimes = GAlgebra[(Nat, *), NatF[Nat], Nat] {
  //    case Zero => one
  //    case Succ((curr, acc)) => times(acc) succ (curr) cata times(acc)
  //  }

  //  property("oneOrTimes20")    = Prop { (oneOrTimes(Zero) cata toIntA) == 1 }
  //  property("oneOrTimes12")    = Prop { (oneOrTimes(Succ(one, two)) cata toIntA) == 4 }
  //  property("oneOrTimes23")    = Prop { (oneOrTimes(Succ(two, three)) cata toIntA) == 9 }
  //  property("paraOneOrTImes3") = Prop { (three para oneOrTimes cata toIntA) == 6 }

  // TODO table-driven property test
  //  (0 to 5) zip Seq(1, 1, 2, 6, 24, 120) foreach { case (arg, result) =>
  //    µ.unfold(arg)(fromInt) para oneOrTimes cata toInt assert_=== result
  //  }
}
