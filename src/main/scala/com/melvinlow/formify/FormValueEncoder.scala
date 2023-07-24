package com.melvinlow.formify

import cats.Contravariant

trait FormValueEncoder[T] {
  def encode(value: T): FormValue
}

object FormValueEncoder {
  inline def apply[T](using enc: FormValueEncoder[T]) = enc

  given Contravariant[FormValueEncoder] with {
    def contramap[A, B](fa: FormValueEncoder[A])(f: B => A): FormValueEncoder[B] =
      (value: B) => fa.encode(f(value))
  }
}
