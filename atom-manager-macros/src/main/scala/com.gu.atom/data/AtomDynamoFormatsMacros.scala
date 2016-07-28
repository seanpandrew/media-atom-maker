package com.gu.atom.data

import scala.language.experimental.macros
import com.gu.scanamo.DynamoFormat
import scala.reflect.macros.blackbox.Context
import com.gu.contentatom.thrift.AtomData

/* typeclass that says that a given type is connected with a subclass
 * of AtomData, and can be converted back and forth there from */
trait CanBeAtomData[C <: AtomData, E] {
  def fromAtomData: PartialFunction[AtomData, E]
  def toAtomData(a: E): C
}

object AtomDynamoFormatsMacros {
  /* Defines that type A is an atom data sub type by materialising a CanBeAtomData typecalss instance */
  implicit def atomDataSubType[C <: AtomData, E]: CanBeAtomData[C, E] =
    macro AtomDynamoFormatsMacrosImpl.canBeAtomDataMaterializer[C, E]

  implicit def atomDataFormats: DynamoFormat[AtomData] = macro AtomDynamoFormatsMacrosImpl.atomDataFormats

}

class AtomDynamoFormatsMacrosImpl(val c: Context) {
  import c.universe._

  def atomDataFormats = {
    val atomDataT = weakTypeOf[AtomData].typeSymbol.asClass
    val dataVar = c.fresh(TermName("data"))

    weakTypeOf[AtomData].companion.members

    val cases = atomDataT.knownDirectSubclasses.map { cl =>
      val companion = cl.asType.toType.companion
      val unapply = companion.member(TermName("unapply")).asMethod
      val argType = unapply.returnType.typeArgs.head.dealias
      cq"$dataVar: $cl => DynamoFormat[$argType].write($unapply($dataVar).get)"
    }
    val res = q"""new DynamoFormat[AtomData] {
def write(ad: AtomData) = ad match { case ..${cases} }
def read(av: AttributeValue) = ???
}"""
    println(
      showCode(res)
    )
    res
  }

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

  def canBeAtomDataMaterializer[C : WeakTypeTag, E : WeakTypeTag] = {
    val containerType = c.weakTypeOf[C]
    val elementType = c.weakTypeOf[E]
    val companion = containerType.companion

    val data = c.fresh(TermName("data"))

c.echo(c.enclosingPosition, s"[PMR] 1337 : ${containerType}")

q"""new CanBeAtomData[$containerType, $elementType] {
def fromAtomData = { case c: $containerType => ??? }
def toAtomData(a: $elementType) = ???
}"""
  }

    // val patFound = cq"$data: $A => $unapply($data)"
    // val patFailed =
    //   cq"""_ => new AttributeValue().withS(s"unknown atom data type")"""

    // val res = q"""{ case ..${Seq(patFound, patFailed)} }"""
    // println(showCode(res))
    // res
}
