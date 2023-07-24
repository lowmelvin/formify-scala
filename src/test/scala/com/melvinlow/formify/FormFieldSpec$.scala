package com.melvinlow.formify

import cats.data.*
import cats.syntax.all.*

object FormFieldSpec$ extends weaver.FunSuite {
  test("should wrap and unwrap") {
    val fragment = FormFieldFragment("hello")
    expect(FormField.one(fragment).underlying == NonEmptyChain(fragment))
  }

  test("should prepend") {
    val fragment1 = FormFieldFragment("f1")
    val fragment2 = FormFieldFragment("f2")

    expect(FormField.one(fragment1).prepend(fragment2).underlying == NonEmptyChain(
      fragment2,
      fragment1
    ))
  }

  test("should compile") {
    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    val fragment1 = FormFieldFragment("f1")
    val fragment2 = FormFieldFragment("f2")
    val key       = FormField.one(fragment1).prepend(fragment2)

    expect(key.compile == "f2.f1")
  }
}
