import org.scalacheck.{ Prop, Properties }
import Prop.*

object NelFProps extends Properties("NelF"):

  // The cool thing is that we can make the ExprR type parametric in the functor F.
  // Then we'll have to write cata only once, whether the structure is based on NelF or any other functor.

  // Here, we are using our generalized building blocks to define nodes in a non-empty linked list (NEL),
  // where H is the generic type of the value stored in the node and T is the type parameter of the functor.

  type NelF[H, T] = (H, Option[T])

  given nelFunctor[H]: Functor[NelF[H, _]] with
    override def fmapImpl[A, B](e: NelF[H, A])(f: A => B): NelF[H, B] =
      (e._1, e._2.map(f)) // uses Option.map - not a recursive call!

  // (Technically, NelF is a bifunctor, i.e., a functor in terms of both H and T.
  // The functor in terms of H corresponds to a map method for transforming the elements of the list.
  // Here we focus on the functor in terms of T for defining the recursive structure and behaviors.)

  type Nel[H] = Fix[NelF[H, _]]

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

  println("l = " + l)
  println("l(1) = " + l.tail._2.get.tail._1)
  println("len = " + l.cata(lengthAlg))
  println("sumAlg(point(7).tail) = " + sumAlg(point(7).tail.fmap(Function.const(0))))
  println("sum = " + l.cata(sumAlg))

  property("l(1)") = Prop { l.tail._2.get.tail._1 == 2 }
  property("Nel.length") = Prop { l.cata(lengthAlg) == 3 }
  property("Nel.sum") = Prop { l.cata(sumAlg) == 6 }

end NelFProps
