package com.melvinlow.formify

object FormKeyFragmentSpec extends weaver.FunSuite {
  test("should wrap and unwrap") {
    expect(FormKeyFragment("hello").underlying == "hello")
  }
}
