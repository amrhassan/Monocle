package monocle.law.discipline

import monocle.Lens
import monocle.law.LensLaws
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import org.typelevel.discipline.Laws

import scalaz.Equal

object LensTests extends Laws {

  def apply[S: Arbitrary : Equal, A: Arbitrary : Equal](lens: Lens[S, A]): RuleSet =
    apply[S, A, Unit](_ => lens)

  def apply[S: Arbitrary : Equal, A: Arbitrary : Equal, I: Arbitrary](f: I => Lens[S, A]): RuleSet = {
    new SimpleRuleSet("Lens",
      "set what you get" -> forAll( (s: S, i: I) => new LensLaws(f(i)).getSet(s)),
      "get what you set" -> forAll( (s: S, a: A, i: I) => new LensLaws(f(i)).setGet(s, a)),
      "set idempotent"   -> forAll( (s: S, a: A, i: I) => new LensLaws(f(i)).setIdempotent(s, a)),
      "modify id = id"   -> forAll( (s: S, i: I) => new LensLaws(f(i)).modifyIdentity(s)),
      "modifyF Id = Id"  -> forAll( (s: S, i: I) => new LensLaws(f(i)).modifyFId(s))
    )
  }

}
