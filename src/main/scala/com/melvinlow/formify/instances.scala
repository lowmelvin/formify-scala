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

    given mapFormEncoder_FV[K <: String, V <: FormData]: FormDataEncoder[Map[K, V]] =
      (value: Map[K, V]) =>
        value.foldLeft(FormData.empty) { case (acc, (key, value)) =>
          acc ++ value.prepend(FormFieldFragment(key))
        }

    given mapFormEncoder_VV[K <: String, V <: FormValue]: FormDataEncoder[Map[K, V]] =
      (value: Map[K, V]) =>
        value.foldLeft(FormData.empty) { case (acc, (key, value)) =>
          acc ++ FormData.one(FormField.one(FormFieldFragment(key)), value)
        }

    given mapFormEncoder_F[K <: String, V](using
      enc: FormDataEncoder[V]
    ): FormDataEncoder[Map[K, V]] =
      FormDataEncoder[Map[K, FormData]].contramap(_.map((k, v) => (k, enc.encode(v))))

    given mapFormEncoder_V[K <: String, V](using
      enc: FormValueEncoder[V]
    ): FormDataEncoder[Map[K, V]] =
      FormDataEncoder[Map[K, FormValue]].contramap(_.map((k, v) => (k, enc.encode(v))))

    given listFormEncoder_F[T](using enc: FormDataEncoder[T]): FormDataEncoder[List[T]] =
      FormDataEncoder[Map[String, FormData]].contramap { values =>
        val encoded = values.map(enc.encode).filter(_.isNonEmpty)
        encoded.zipWithIndex.map { case (value, idx) => (idx.toString, value) }.toMap
      }

    given listFormEncoder_V[T](using enc: FormValueEncoder[T]): FormDataEncoder[List[T]] =
      FormDataEncoder[Map[String, FormValue]].contramap { values =>
        val encoded = values.map(enc.encode).filter(_.isNonEmpty)
        encoded.zipWithIndex.map { case (value, idx) => (idx.toString, value) }.toMap
      }

    given vectorFormEncoder_F[T: FormDataEncoder]: FormDataEncoder[Vector[T]] =
      FormDataEncoder[List[T]].contramap(_.toList)

    given vectorFormEncoder_V[T: FormValueEncoder]: FormDataEncoder[Vector[T]] =
      FormDataEncoder[List[T]].contramap(_.toList)

    given seqFormEncoder_F[T: FormDataEncoder]: FormDataEncoder[Seq[T]] =
      FormDataEncoder[List[T]].contramap(_.toList)

    given seqFormEncoder_V[T: FormValueEncoder]: FormDataEncoder[Seq[T]] =
      FormDataEncoder[List[T]].contramap(_.toList)

    given arrayFormEncoder_F[T: FormDataEncoder]: FormDataEncoder[Array[T]] =
      FormDataEncoder[List[T]].contramap(_.toList)

    given arrayFormEncoder_V[T: FormValueEncoder]: FormDataEncoder[Array[T]] =
      FormDataEncoder[List[T]].contramap(_.toList)
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
