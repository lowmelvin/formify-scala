package com.melvinlow.formify

object FormFieldFragmentSpec$ extends weaver.FunSuite {
  test("should wrap and unwrap") {
    expect(FormFieldFragment("hello").underlying == "hello")
  }
}
