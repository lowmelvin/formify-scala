package com.melvinlow.formify

import cats.data.*

trait FormKeyCompiler {
  def compile(key: FormKey): String
}

object FormKeyCompiler {
  def make(fn: (NonEmptyChain[String]) => String): FormKeyCompiler = new FormKeyCompiler {
    override def compile(key: FormKey): String = fn(key.underlying.map(_.underlying))
  }
}
