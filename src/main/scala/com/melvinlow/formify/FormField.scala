package com.melvinlow.formify

import cats.data.*

opaque type FormField = NonEmptyChain[FormFieldFragment]

object FormField {
  inline def one(fragment: FormFieldFragment): FormField = NonEmptyChain.one(fragment)

  extension (field: FormField) {
    inline def underlying: NonEmptyChain[FormFieldFragment] = field

    inline def prepend(fragment: FormFieldFragment): FormField = fragment +: field

    inline def compile(using compiler: FormFieldComposer): String = compiler.compile(field)
  }
}
