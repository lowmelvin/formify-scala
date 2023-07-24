package com.melvinlow.formify

object FormValueSpec extends weaver.FunSuite {
  test("should wrap and unwrap") {
    expect(FormValue("hello").underlying == Some("hello"))
  }

  test("should create empty") {
    expect(FormValue.empty.underlying == None)
  }

  test("should unapply a value that exists") {
    FormValue("hello") match {
      case FormValue(value) => expect(value == "hello")
      case _                => expect(false)
    }
  }

  test("should unapply an empty value") {
    FormValue.empty match {
      case FormValue(_) => expect(false)
      case _            => expect(true)
    }
  }

  test("should check if empty") {
    expect(FormValue("hello").isEmpty == false)
    expect(FormValue.empty.isEmpty == true)
  }
}
