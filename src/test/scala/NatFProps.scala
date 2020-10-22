import higherkindness.droste._
import higherkindness.droste.data._
import cats._

import org.scalacheck.{ Prop, Properties }

object NatFProps extends Properties("NatF") {

  sealed trait NatF[+T]
  case object Zero extends NatF[Nothing]
  case class Succ[+T](n: T) extends NatF[T]

  implicit object natFFunctor extends Functor[NatF] {
    override def map[T, U](fa: NatF[T])(f: T => U): NatF[U] = fa match {
      case Zero => Zero
      case Succ(n) => Succ(f(n))
    }
  }

  type Nat = Fix[NatF]

  val zero: Nat = Fix[NatF](Zero)
  val succ: Nat => Fix[NatF] = n => Fix[NatF](Succ(n))

  val toInt: Algebra[NatF, Int] = Algebra {
    case Succ(n) => n + 1
    case Zero => 0
  }

  property("cata0") = Prop {
    val f = scheme.cata(toInt)
    f(zero) == 0
  }

  property("cata3") = Prop {
    val f = scheme.cata(toInt)
    f(succ(succ(succ(zero)))) == 3
  }
}