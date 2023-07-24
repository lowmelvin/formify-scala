package com.melvinlow.formify

import cats.data.*
import cats.syntax.all.*

object FormDataSpec extends weaver.FunSuite {
  test("should wrap and unwrap empty") {
    expect(FormData.empty.underlying == Chain.empty)
  }

  test("should wrap and unwrap one") {
    val key   = FormField.one(FormFieldFragment("hello"))
    val value = FormValue("world")
    expect(FormData.one(key, value).underlying == Chain.one((key, value)))
  }

  test("should compile") {
    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    val key1      = FormField.one(FormFieldFragment("f1"))
    val key2      = FormField.one(FormFieldFragment("f2"))
    val value1    = FormValue("v1")
    val value2    = FormValue("v2")
    val data      = FormData.one(key1, value1) ++ FormData.one(key2, value2)
    val prepended = data.prepend(FormFieldFragment("p"))

    expect(prepended.compile == Chain(("p.f1", "v1"), ("p.f2", "v2")))
  }

  test("should serialize using URL encoding") {
    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    val key1      = FormField.one(FormFieldFragment("f1&"))
    val key2      = FormField.one(FormFieldFragment("f2&"))
    val value1    = FormValue("v1&")
    val value2    = FormValue("v2&")
    val data      = FormData.one(key1, value1) ++ FormData.one(key2, value2)
    val prepended = data.prepend(FormFieldFragment("p"))

    expect(prepended.serialize == "p.f1%26=v1%26&p.f2%26=v2%26")
  }

  test("should check if empty") {
    expect(FormData.empty.isEmpty == true)
    expect(FormData.one(
      FormField.one(FormFieldFragment("hello")),
      FormValue("world")
    ).isEmpty == false)
  }
}
