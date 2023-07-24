package com.melvinlow.formify

import cats.data.*
import cats.syntax.all.*

object FormKeySpec extends weaver.FunSuite {
  test("should wrap and unwrap") {
    val fragment = FormKeyFragment("hello")
    expect(FormKey.one(fragment).underlying == NonEmptyChain(fragment))
  }

  test("should prepend") {
    val fragment1 = FormKeyFragment("f1")
    val fragment2 = FormKeyFragment("f2")

    expect(FormKey.one(fragment1).prepend(fragment2).underlying == NonEmptyChain(
      fragment2,
      fragment1
    ))
  }

  test("should compile") {
    given FormKeyCompiler = FormKeyCompiler.make(_.toList.mkString("."))

    val fragment1 = FormKeyFragment("f1")
    val fragment2 = FormKeyFragment("f2")
    val key       = FormKey.one(fragment1).prepend(fragment2)

    expect(key.compile == "f2.f1")
  }
}
