// Common building blocks for recursive structures and behaviors based on F-algebras.

// The cool thing is that we can make recursive types parametric in their underlying functor F.
// Then we can reuse cata and other recursion schemes in conjunction with any specific functor.

/** A type with a map method that satisfies the functor laws. */
trait Functor[F[_]]:
  def fmapImpl[A, B](fa: F[A])(f: A => B): F[B]
  extension[A] (fa: F[A])
    // provides convenient fa.fmap(f) syntax
    // called fmap to avoid confusing ambiguity with Tuple.map
    def fmap[B](f: A => B): F[B] = fmapImpl(fa)(f)

/**
 * The type of a function from a functor instance to a result,
 * where the concrete type parameter of the functor instance
 * and the result type are the same.
 */
type FAlgebra[F[_], R] = F[R] => R

/**
 * A type constructor for forming a recursive type over a functor,
 * along with the corresponding catamorphism (generalized fold).
 */
case class Fix[F[_]: Functor](tail: F[Fix[F]]) derives CanEqual:
  def cata[R](alg: FAlgebra[F, R]): R = alg(tail.fmap(c => c.cata(alg)))
