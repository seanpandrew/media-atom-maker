package com.gu.atom.data

import scala.language.experimental.macros
import com.gu.scanamo.DynamoFormat
import scala.reflect.macros.blackbox.Context
import com.gu.contentatom.thrift.AtomData

/* typeclass that says that a given type is connected with a subclass
 * of AtomData, and can be converted back and forth there from */
trait CanBeAtomData[A] {
  def fromAtomData: PartialFunction[AtomData, A]
  def toAtomData(a: A): AtomData
}

object AtomDynamoFormatsMacros {
  /* defines that type A is an atom data sub type by materialising a CanBeAtomData typecalss instance */
  implicit def atomDataSubType[A]: CanBeAtomData[A] = macro AtomDynamoFormatsMacrosImpl.canBeAtomDataMaterializer[A]
}

class AtomDynamoFormatsMacrosImpl(val c: Context) {
  import c.universe._

  // def fromSealedToMember[A : WeakTypeTag, B : WeakTypeTag] = {
  //   val A = weakTypeOf[A]
  //   val B = weakTypeOf[B].dealias
  //   // find the member of the sealed class A that contains the type B
  //   val cls = A.typeSymbol.asClass
  //   cls.typeSignature // https://issues.scala-lang.org/browse/SI-7046

  //   require(cls.isSealed)

  //   cls.knownDirectSubclasses.foreach { cl =>
  //     println(cl.asClass.primaryConstructor.asMethod.paramLists.head.head.info <:< B)
  //   }

  //   c.echo(NoPosition, s"[PMR] 0951 : ${cls} -> ${B}")

  def canBeAtomDataMaterializer[A : WeakTypeTag] = {
    q"???"
  }
  // val A = c.weakTypeOf[A]
  // val unapply = A.companion.member(TermName("unapply"))
  // val data = c.fresh(TermName("data"))


    // val patFound = cq"$data: $A => $unapply($data)"
    // val patFailed =
    //   cq"""_ => new AttributeValue().withS(s"unknown atom data type")"""

    // val res = q"""{ case ..${Seq(patFound, patFailed)} }"""
    // println(showCode(res))
    // res
}
