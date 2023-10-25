import org.scalacheck.{ Prop, Properties }
import Prop.*

object ExprFProps extends Properties("ExprF"):

  // We'll start with a simplified version of the expressions structure.

  enum Expr derives CanEqual:
    case Constant(value: Int)
    case Plus(children: List[Expr])

  import Expr.*

  // Now we'll add some (recursive) behaviors.

  def size(e: Expr): Int = e match
    case Constant(v) => 1
    case Plus(es)    => 1 + es.map(size).sum

  def height(e: Expr): Int = e match
    case Constant(v) => 1
    case Plus(es)    => 1 + es.map(height).max

  def evaluate(e: Expr): Int = e match
    case Constant(v) => v
    case Plus(es)    => es.map(evaluate).sum

  // If Minus existed, it would evaluate like this:
  // case Minus(es) => es.map(evaluate).foldLeft(0)((x, y) => x - y))

  // NOTE the reduce step after map is often monoidal

  // We can also add behaviors with a result type other than Int.

  def scale(factor: Int)(e: Expr): Expr = e match
    case Constant(v) => Constant(factor * v)
    case Plus(es)    => Plus(es.map(scale(factor)))

  // Let's run a quick test.

  val e = Plus(List(Constant(5), Constant(3)))
  property("Expr.evaluate") = Prop { evaluate(e) == 8 }

  // Next, we'll reorganize the implementations of the behaviors a tiny bit
  // by using auxiliary methods onConstant and onPlus.

  def size1(e: Expr): Int =
    def onConstant(value: Int) = 1
    def onPlus(results: List[Int]) = 1 + results.sum
    e match
      case Constant(v) => onConstant(v)
      case Plus(es)    => onPlus(es.map(size1))

  def height1(e: Expr): Int =
    def onConstant(value: Int) = 1
    def onPlus(results: List[Int]) = 1 + results.max
    e match
      case Constant(v) => onConstant(v)
      case Plus(es)    => onPlus(es.map(height1))

  property("Expr.size1") = Prop { size1(e) == 3 }

  // As we can see, the only difference among these behaviors is the specific onConstant and onPlus implementations!
  // The part where we use map to invoke the behavior recursively on the children is the same.
  // So let's factor it out into a function we parameterize by onConstant and onPlus:

  def dry[R](onConstant: Int => R, onPlus: List[R] => R)(e: Expr): R = e match
    case Constant(v) => onConstant(v)
    case Plus(es)    => onPlus(es.map(dry(onConstant, onPlus)))

  // Now we can reimplement the behaviors in a much more DRY way:

  def size2(e: Expr): Int = dry(v => 1, rs => 1 + rs.sum)(e)
  def height2(e: Expr): Int = dry(v => 1, rs => 1 + rs.max)(e)
  def evaluate2(e: Expr): Int = dry(v => v, rs => rs.sum)(e)
  def scale2(factor: Int)(e: Expr): Expr = dry(v => Constant(factor * v), rs => Plus(rs))(e)

  property("Expr.evaluate2") = Prop { evaluate2(e) == 8 }

  // This approach becomes increasingly clumsy if we have more variants because
  // we'll need a separate argument to the dry function for each variant!
  // So let's make things a bit more systematic by defining a (nonrecursive) structure for
  // organizing all auxiliary functions for a behavior into a single unit.
  // It needs to support a map method for the recursive calls on the children.
  // We call such a structure a functor, provided that the map method satisfies a couple of simple laws.

  enum ExprF[A] derives CanEqual:
    case Constant(value: Int) extends ExprF[A]
    case Plus(children: List[A]) extends ExprF[A]
    def fmap[B](f: A => B): ExprF[B] = this match
      case Constant(v) => Constant(v) // invariant
      case Plus(es) => Plus(es.map(f)) // uses List.map - not a recursive call!

  // This type, called an ExprF-algebra, represents the auxiliary functions for a behavior:

  type ExprFAlgebra[R] = ExprF[R] => R

  // ExprR brings back recursion by plugging the ExprF functor into itself.
  // The cata method is equivalent to dry above.

  case class ExprR(tail: ExprF[ExprR]) derives CanEqual:
    def cata[R](alg: ExprFAlgebra[R]): R = alg(tail.fmap(c => c.cata(alg)))

  // Something to think about: where did the base case go?!?

  // It is convenient to define factory methods for creating nodes in a recursive structure:

  object ExprR:
    def constant(value: Int) = ExprR(ExprF.Constant(value))
    def plus(children: List[ExprR]) = ExprR(ExprF.Plus(children))

  // The following ExprF-algebras provide the behaviors defined earlier minus the recursion.

  val sizeAlg: ExprFAlgebra[Int] =
    case ExprF.Constant(v) => 1
    case ExprF.Plus(es)    => 1 + es.sum

  val heightAlg: ExprFAlgebra[Int] =
    case ExprF.Constant(v) => 1
    case ExprF.Plus(es)    => 1 + es.max

  val evaluateAlg: ExprFAlgebra[Int] =
    case ExprF.Constant(v) => v
    case ExprF.Plus(es)    => es.sum

  def scaleAlg(factor: Int): ExprFAlgebra[ExprR] =
    case ExprF.Constant(v) => ExprR.constant(factor * v)
    case ExprF.Plus(es)    => ExprR.plus(es)

  val c6 = ExprR.constant(6)
  val e1 = ExprR.plus(List(ExprR.constant(5), ExprR.constant(3)))
  val e2 = ExprR.plus(List(ExprR.constant(10), ExprR.constant(6)))

  property("ExprR.evaluate") = Prop { e1.cata(evaluateAlg) == 8 }

  // The cool thing is that we can make the recursive type (ExprR) parametric in the functor F.
  // Then we'll have to write cata only once, whether the structure is based on ExprF or any other functor.
  // The building blocks for this approach have already been moved to a common source file.

  // To retrofit ExprF into this generalized approach, we need to define a functor instance for
  // it where we implement the map method (as the one already defined for ExprF).

  given exprFunctor: Functor[ExprF] with
    override def fmapImpl[A, B](e: ExprF[A])(f: A => B): ExprF[B] = e.fmap(f)

  type ExprFR = Fix[ExprF]

  // Factory methods like above but using the general Fix case class for building recursive structures:

  def constant(value: Int): ExprFR = Fix(ExprF.Constant(value))
  def plus(children: List[ExprFR]): ExprFR = Fix(ExprF.Plus(children))

  val e3 = plus(List(constant(5), constant(3)))

  property("ExprFix.evaluate") = Prop { e3.cata(evaluateAlg) == 8 }
  property("ExprFix.scale1") = Prop { constant(3).cata(scaleAlg(2)) == c6 }
  property("ExprFix.scale2") = Prop { e3.cata(scaleAlg(2)) == e2 }

end ExprFProps
