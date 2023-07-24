package com.melvinlow.formify

import cats.*
import cats.syntax.all.*

import java.util.UUID

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
      case None        => FormValue.Empty
    }
  }

  trait FormDataEncoders {
    given optionFormEncoder[T](using enc: FormDataEncoder[T]): FormDataEncoder[Option[T]] = {
      case Some(value) => enc.encode(value)
      case None        => FormData.Empty
    }

    given traverseFormEncoder_WithForms[F[_]: Traverse, T](using
      enc: FormValueEncoder[T]
    ): FormDataEncoder[F[T]] = (value: F[T]) => {
      val (_, result) =
        Traverse[F].foldLeft(value, (0, FormData.Empty)) { case ((idx, acc), value) =>
          enc.encode(value) match {
            case v @ FormValue(_) =>
              (idx + 1, acc ++ FormData.one(FormKey.one(FormKeyFragment(idx.toString)), v))
            case _ => (idx, acc)
          }
        }

      result
    }

    given traverseFormEncoder_WithValues[F[_]: Traverse, T](using
      enc: FormDataEncoder[T]
    ): FormDataEncoder[F[T]] = (value: F[T]) => {
      val (_, result) =
        Traverse[F].foldLeft(value, (0, FormData.Empty)) { case ((idx, acc), value) =>
          val encoded = enc.encode(value)
          if (encoded.underlying.isEmpty) (idx, acc)
          else (idx + 1, acc ++ encoded.prepend(FormKeyFragment(idx.toString)))
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
