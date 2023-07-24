package com.melvinlow.formify

import cats.data.*

trait FormFieldComposer {
  def compile(field: FormField): String
}

object FormFieldComposer {
  def make(fn: (NonEmptyChain[String]) => String): FormFieldComposer =
    (field: FormField) => fn(field.underlying.map(_.underlying))
}
