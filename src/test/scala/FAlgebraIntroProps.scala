import cats.*
import cats.implicits.*
import org.scalacheck.Prop.*
import org.scalacheck.{Prop, Properties}

object IntroProps extends Properties("Fix"):

  case class Fix[F[_]: Functor](tail: F[Fix[F]]) derives CanEqual {
    def cata[R](alg: F[R] => R): R = alg(tail.map(_.cata(alg)))
  }

  type Nat = Fix[Option]

  val zero = Fix[Option](None)
  val succ = (n: Nat) => Fix[Option](Some(n))

  val one = succ(zero)
  val two = succ(succ(zero))
  val three = succ(two)

  given CanEqual[None.type, Option[_]] = CanEqual.derived

  val toInt: Option[Int] => Int =
    case None    => 0
    case Some(n) => n + 1

  property("zero cata toInt") = Prop { (zero cata toInt) == 0 }
  property("three cata toInt") = Prop { (three cata toInt) == 3 }

  type Tree = Fix[List]

  val t1 = Fix[List](List.empty[Fix[List]])

  val t3 = Fix[List](List(t1, t1))

  given CanEqual[Nil.type, List[_]] = CanEqual.derived

  val size: List[Int] => Int = xs => 1 + xs.sum

  val height: List[Int] => Int = xs => 1 + xs.max

  property("t1 cata size") = Prop { (t1 cata size) == 1 }
  property("t3 cata size") = Prop { (t3 cata size) == 3 }
  property("t1 cata height") = Prop { (t1 cata height) == 1 }
  property("t3 cata height") = Prop { (t3 cata height) == 2 }


end FixProps
