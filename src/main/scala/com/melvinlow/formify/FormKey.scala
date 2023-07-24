package com.melvinlow.formify

import cats.data.*

opaque type FormKey = NonEmptyChain[FormKeyFragment]

object FormKey {
  inline def one(fragment: FormKeyFragment): FormKey = NonEmptyChain.one(fragment)

  extension (key: FormKey) {
    inline def underlying: NonEmptyChain[FormKeyFragment] = key

    inline def prepend(fragment: FormKeyFragment): FormKey = fragment +: key

    inline def compile(using compiler: FormKeyCompiler): String = compiler.compile(key)
  }
}
