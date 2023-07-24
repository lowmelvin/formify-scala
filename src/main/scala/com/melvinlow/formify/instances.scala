package com.melvinlow.formify

import cats.*
import cats.syntax.all.*

import java.util.UUID

import scala.compiletime.summonFrom
import scala.deriving.*

object instances {
  trait FormValueEncoders {
    given stringValueEncoder: FormValueEncoder[String]   = (value: String) => FormValue(value)
    given booleanValueEncoder: FormValueEncoder[Boolean] = stringValueEncoder.contramap(_.toString)
    given byteValueEncoder: FormValueEncoder[Byte]       = stringValueEncoder.contramap(_.toString)
    given shortValueEncoder: FormValueEncoder[Short]     = stringValueEncoder.contramap(_.toString)
    given intValueEncoder: FormValueEncoder[Int]         = stringValueEncoder.contramap(_.toString)
    given longValueEncoder: FormValueEncoder[Long]       = stringValueEncoder.contramap(_.toString)
    given floatValueEncoder: FormValueEncoder[Float]     = stringValueEncoder.contramap(_.toString)
    given doubleValueEncoder: FormValueEncoder[Double]   = stringValueEncoder.contramap(_.toString)
    given bigIntValueEncoder: FormValueEncoder[BigInt]   = stringValueEncoder.contramap(_.toString)
    given bigDecimalValueEncoder: FormValueEncoder[BigDecimal] =
      stringValueEncoder.contramap(_.toString)
    given charValueEncoder: FormValueEncoder[Char] = stringValueEncoder.contramap(_.toString)
    given symbolValueEncoder: FormValueEncoder[Symbol] =
      stringValueEncoder.contramap(_.name)
    given uuidValueEncoder: FormValueEncoder[UUID] = stringValueEncoder.contramap(_.toString)

    given singletonValueEncoder[T](using v: ValueOf[T]): FormValueEncoder[T] =
      stringValueEncoder.contramap(_ => v.value.toString)

    given optionValueEncoder[T](using enc: FormValueEncoder[T]): FormValueEncoder[Option[T]] = {
      case Some(value) => enc.encode(value)
      case None        => FormValue.empty
    }
  }

  trait FormDataEncoders {
    given optionFormEncoder[T](using enc: FormDataEncoder[T]): FormDataEncoder[Option[T]] = {
      case Some(value) => enc.encode(value)
      case None        => FormData.empty
    }

    given listFormEncoder_WithForms[T](using enc: FormDataEncoder[T]): FormDataEncoder[List[T]] =
      (value: List[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            val encoded = enc.encode(value)
            if (encoded.underlying.isEmpty) (idx, acc)
            else (idx + 1, acc ++ encoded.prepend(FormKeyFragment(idx.toString)))
          }
        result
      }

    given listFormEncoder_WithValues[T](using enc: FormValueEncoder[T]): FormDataEncoder[List[T]] =
      (value: List[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            enc.encode(value) match {
              case v @ FormValue(_) =>
                (idx + 1, acc ++ FormData.one(FormKey.one(FormKeyFragment(idx.toString)), v))
              case _ => (idx, acc)
            }
          }
        result
      }

    given vectorFormEncoder_WithForms[T](using
      enc: FormDataEncoder[T]
    ): FormDataEncoder[Vector[T]] =
      (value: Vector[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            val encoded = enc.encode(value)
            if (encoded.underlying.isEmpty) (idx, acc)
            else (idx + 1, acc ++ encoded.prepend(FormKeyFragment(idx.toString)))
          }
        result
      }

    given vectorFormEncoder_WithValues[T](using
      enc: FormValueEncoder[T]
    ): FormDataEncoder[Vector[T]] =
      (value: Vector[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            enc.encode(value) match {
              case v @ FormValue(_) =>
                (idx + 1, acc ++ FormData.one(FormKey.one(FormKeyFragment(idx.toString)), v))
              case _ => (idx, acc)
            }
          }
        result
      }

    given seqFormEncoder_WithForms[T](using enc: FormDataEncoder[T]): FormDataEncoder[Seq[T]] =
      (value: Seq[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            val encoded = enc.encode(value)
            if (encoded.underlying.isEmpty) (idx, acc)
            else (idx + 1, acc ++ encoded.prepend(FormKeyFragment(idx.toString)))
          }
        result
      }

    given seqFormEncoder_WithValues[T](using enc: FormValueEncoder[T]): FormDataEncoder[Seq[T]] =
      (value: Seq[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            enc.encode(value) match {
              case v @ FormValue(_) =>
                (idx + 1, acc ++ FormData.one(FormKey.one(FormKeyFragment(idx.toString)), v))
              case _ => (idx, acc)
            }
          }
        result
      }

    given arrayFormEncoder_WithForms[T](using enc: FormDataEncoder[T]): FormDataEncoder[Array[T]] =
      (value: Array[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            val encoded = enc.encode(value)
            if (encoded.underlying.isEmpty) (idx, acc)
            else (idx + 1, acc ++ encoded.prepend(FormKeyFragment(idx.toString)))
          }
        result
      }

    given arrayFormEncoder_WithValues[T](using
      enc: FormValueEncoder[T]
    ): FormDataEncoder[Array[T]] =
      (value: Array[T]) => {
        val (_, result) =
          value.foldLeft((0, FormData.empty)) { case ((idx, acc), value) =>
            enc.encode(value) match {
              case v @ FormValue(_) =>
                (idx + 1, acc ++ FormData.one(FormKey.one(FormKeyFragment(idx.toString)), v))
              case _ => (idx, acc)
            }
          }
        result
      }
  }

  trait ProductEncoders {
    inline given productFormEncoder[T: Mirror.ProductOf]: FormDataEncoder[T] =
      FormDataEncoder.derived[T]
  }

  object FormDataEncoderInstances  extends FormDataEncoders
  object FormValueEncoderInstances extends FormValueEncoders
  object ProductEncoderInstances   extends ProductEncoders
  object semiauto                  extends FormDataEncoders with FormValueEncoders
  object auto extends FormDataEncoders with FormValueEncoders with ProductEncoders
}
