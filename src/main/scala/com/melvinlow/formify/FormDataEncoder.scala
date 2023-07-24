package com.melvinlow.formify

import scala.compiletime.*
import scala.compiletime.ops.any.*
import scala.deriving.*

trait FormDataEncoder[T] {
  def encode(data: T): FormData

  extension (data: T) {
    inline def asFormData: FormData = encode(data)
  }
}

object FormDataEncoder {
  inline def apply[T](using enc: FormDataEncoder[T]) = enc

  inline def encode[T: FormDataEncoder](data: T): FormData = data.asFormData

  inline private def summonEncoders[T, Elems <: Tuple]: List[?] =
    inline erasedValue[Elems] match {
      case _: EmptyTuple     => Nil
      case _: (head *: tail) => summonElemEncoder[T, head] :: summonEncoders[T, tail]
    }

  inline private def summonElemEncoder[T, Elem]: FormDataEncoder[Elem] | FormValueEncoder[Elem] =
    summonFrom {
      case formValueEncoder: FormValueEncoder[Elem] => formValueEncoder // prioritize value encoders
      case formEncoder: FormDataEncoder[Elem]       => formEncoder
      case _: Mirror.ProductOf[Elem]                => deriveElemEncoder[T, Elem]
    }

  inline private def deriveElemEncoder[T, Elem: Mirror.ProductOf]: FormDataEncoder[Elem] =
    inline erasedValue[Elem] match {
      case _: T => error("infinite recursion derivation")
      case _    => derived[Elem]
    }

  inline private def summonLabels[Labels <: Tuple]: List[String] =
    inline erasedValue[Labels] match {
      case _: EmptyTuple     => Nil
      case _: (head *: tail) => constValue[ToString[head]] :: summonLabels[tail]
    }

  inline def derived[T](using m: Mirror.ProductOf[T]): FormDataEncoder[T] = new FormDataEncoder[T] {
    lazy val labels   = summonLabels[m.MirroredElemLabels]
    lazy val encoders = summonEncoders[T, m.MirroredElemTypes]

    override def encode(data: T): FormData = {
      val values = data.asInstanceOf[Product].productIterator.toList

      labels.lazyZip(values).lazyZip(encoders).map {
        case (label, value, formDataEncoder: FormDataEncoder[v]) =>
          formDataEncoder.encode(value.asInstanceOf[v]).prepend(FormKeyFragment(label))

        case (label, value, formValueEncoder: FormValueEncoder[v]) =>
          FormData.one(
            FormKey.one(FormKeyFragment(label)),
            formValueEncoder.encode(value.asInstanceOf[v])
          )
      }.foldLeft(FormData.Empty)(_ ++ _)
    }
  }
}
