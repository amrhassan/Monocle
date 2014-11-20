package monocle.std

import monocle.function._
import monocle.{SimpleIso, SimpleOptional}

import scalaz.{OneAnd, NonEmptyList}
import scalaz.NonEmptyList._

object nonemptylist extends NonEmptyListInstances

trait NonEmptyListInstances{

  implicit def nelEach[A]: Each[NonEmptyList[A], A] = Each.traverseEach[NonEmptyList, A]

  implicit def nelIndex[A]: Index[NonEmptyList[A], Int, A] = new Index[NonEmptyList[A], Int, A] {

    def index(i: Int): SimpleOptional[NonEmptyList[A], A] = i match {
      case 0 => nelCons1.head.asOptional
      case _ => nelCons1.tail composeOptional list.listIndex.index(i-1)
    }
  }

  implicit def nelFilterIndex[A]: FilterIndex[NonEmptyList[A], Int, A] =
    FilterIndex.traverseFilterIndex[NonEmptyList, A](n => n.zip(NonEmptyList(0, Stream.from(1):_*)))

  implicit def nelReverse[A]: Reverse[NonEmptyList[A], NonEmptyList[A]] =
    reverseFromReverseFunction[NonEmptyList[A]](_.reverse)


  implicit def nelCons1[A]: Cons1[NonEmptyList[A], A, List[A]] =  new Cons1[NonEmptyList[A],A,List[A]]{
    def cons1 = SimpleIso((nel: NonEmptyList[A]) => (nel.head,nel.tail)){case (h,t) => NonEmptyList.nel(h,t)}
  }

  implicit def nelSnoc1[A]:Snoc1[NonEmptyList[A], List[A], A] = new Snoc1[NonEmptyList[A],List[A], A]{
    override def snoc1: SimpleIso[NonEmptyList[A], (List[A], A)] =
      SimpleIso((nel:NonEmptyList[A]) => nel.init -> nel.last){ case (i,l) => NonEmptyList.nel(l,i.reverse).reverse}
  }

  implicit def nelAndOneIso[A] : SimpleIso[NonEmptyList[A], OneAnd[List,A]] =
    SimpleIso((nel: NonEmptyList[A]) => OneAnd[List,A](nel.head,nel.tail))(
              (and: OneAnd[List, A]) => NonEmptyList(and.head,and.tail:_*))


}