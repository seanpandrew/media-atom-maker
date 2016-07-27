package com.gu.atom.data

import scala.language.experimental.macros
import com.gu.scanamo.DynamoFormat
import scala.reflect.macros.blackbox.Context
import com.gu.contentatom.thrift.AtomData

object AtomDynamoFormatsMacros {
  def fromAtomData[A <: ThriftStruct]: PartialFunction[AtomData, A] = macro AtomDynamoFormatsMacrosImpl.fromAtomData[A]
}


class AtomDynamoFormatsMacrosImpl(val c: Context) {
  import c.universe._

  def fromAtomData[A : WeakTypeTag] = {
    val A = c.weakTypeOf[A]
    //val dataTerm = c.fresh("atomData")
    val patFound = cq"$A(data) => DynamoFormat[$A].write(data)"
    val patFailed =
      cq"""_ => new AttributeValue().withS(s"unknown atom data type")"""

    val res = q"""{ case ..${Seq(patFound, patFailed)} }"""
    println(showCode(res))
    res
  }
}
