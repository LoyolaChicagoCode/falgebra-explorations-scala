import org.scalacheck.{ Prop, Properties }
import Prop.*

/**
 * In this example, we represent natural numbers
 * essentially as lists without item values:
 *
 * 0 = zero
 *
 * 3 = succ(succ(succ(zero)))
 *
 * We can then define operations such as addition on these.
 */
object NatFProps extends Properties("NatF"):

  /**
   * Endofunctor for (non-generic) F-algebra in the category Scala types:
   * {{{
   * data NatF[+T] = Zero | Succ(n: T)
   * }}}
   *
   * @tparam T argument (type parameter) of the endofunctor
   */

  enum NatF[T] derives CanEqual:
    case Zero() extends NatF[T]
    case Succ[T](n: T) extends NatF[T]

  import NatF.{given, *}
  
  /** Declaration of `NatF` as an instance of `Functor`. */
  given exprFunctor: Functor[NatF] with
    override def fmapImpl[A, B](e: NatF[A])(f: A => B): NatF[B] = e match
      case Zero() => Zero()
      case Succ(n) => Succ(f(n))

/**
   * Least fixpoint of `NatF` (recursive type based on `NatF`)
   * as carrier object for initial algebra.
   */
  type Nat = Fix[NatF]

  // Factory methods for convenience.
  val zero = Fix[NatF](Zero())
  val succ = (n: Nat) => Fix[NatF](Succ(n))

  // some instances
  val one = succ(zero)
  val two = succ(one)
  val three = succ(two)

  /**
   * Conversion to `Int` as an `NatF`-algebra
   * for carrier object `Int` in the category Scala types.
   */
  val toIntA: FAlgebra[NatF, Int] =
    case Zero() => 0
    case Succ(n) if n >= 0 => n + 1

  def toInt(n: Nat) = n.cata(toIntA)

  // Using the catamorphism, we now can fold the `toInt` algebra into instances.
  // (This is an example of recursion.)
  property("cata0") = Prop { toInt(zero) == 0 }
  property("cata3") = Prop { toInt(succ(succ(succ(zero)))) == 3 }

  // TODO migrate remaining examples from Droste version

end NatFProps
