import org.scalacheck.Prop.*
import org.scalacheck.{Prop, Properties}

object NelFProps extends Properties("Intro"):

  // The cool thing is that we can make the ExprR type parametric in the functor F.
  // Then we'll have to write cata only once, whether the structure is based on ExprF or any other functor.

  trait Functor[F[_]]:
    def mapImpl[A, B](fa: F[A])(f: A => B): F[B]
    extension [A](fa: F[A])
      // provides convenient fa.map(f) syntax
      def map[B](f: A => B): F[B] = mapImpl(fa)(f)

  type FAlgebra[F[_], R] = F[R] => R

  case class Fix[F[_]: Functor](tail: F[Fix[F]]) derives CanEqual:
    def cata[R](alg: FAlgebra[F, R]): R = alg(tail.map(c => c.cata(alg)))

  // Finally, we can use these generalized building blocks to define other structures,
  // such as nodes in a non-empty linked list (NEL), where H is the generic type of
  // the value stored in the node and T is the type parameter of the functor.

  type NelF[H, T] = (H, Option[T])

  type Nel[H] = Fix[NelF[H, _]]

  given consFunctor[H]: Functor[NelF[H, _]] with
    override def mapImpl[A, B](e: NelF[H, A])(f: A => B): NelF[H, B] =
      (e._1, e._2.map(f))

  // (Technically, NelF is a bifunctor, i.e., a functor in terms of both H and T.
  // The functor in terms of H corresponds to a map method for transforming the elements of the list.
  // Here we focus on the functor in terms of T for defining the recursive structure and behaviors.)

  def cons[H](value: H, next: Nel[H]): Nel[H] = Fix(value, Some(next))
  def point[H](value: H): Nel[H] = Fix(value, None)

  type NelAlgebra[H, R] = NelF[H, R] => R

  def lengthAlg[H]: NelAlgebra[H, Int] =
    case (i, Some(n)) => 1 + n
    case (i, _)       => 1

  val sumAlg: NelAlgebra[Int, Int] =
// This works:
    (i, t) => t match
      case Some(s) => i + s
      case _ => i
// The following is not working in Scala 3.3.x!?! Compiler error?
//    case (i, Some(s)) => i + s
//    case (i, _)       => i

  val l = cons(1, cons(2, point(3)))
  val p = point(7)

  println("l = " + l)
  println("l(1) = " + l.tail._2.get.tail._1)
  println("len = " + l.cata(lengthAlg))
  println("sumAlg(point(7).tail) = " + sumAlg(p.tail.map(Function.const(0))))
  println("sum = " + l.cata(sumAlg))

  property("l(1)") = Prop { l.tail._2.get.tail._1 == 2 }
  property("Nel.length") = Prop { l.cata(lengthAlg) == 3 }
  property("Nel.sum") = Prop { l.cata(sumAlg) == 6 }

// NOTE For pedagogical reasons, we eliminated the dependencies on the Cats and Droste libraries seen in the other examples.
// Cats defines Functors and other useful buildling blocks including property-based tests for laws.
// Droste defines Fix along with cata and other recursion schemes.

end NelFProps
