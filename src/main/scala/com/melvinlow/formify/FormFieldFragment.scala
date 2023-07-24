package com.melvinlow.formify

opaque type FormFieldFragment = String

object FormFieldFragment {
  inline def apply(fragment: String): FormFieldFragment = fragment

  extension (fragment: FormFieldFragment) {
    inline def underlying: String = fragment
  }
}
