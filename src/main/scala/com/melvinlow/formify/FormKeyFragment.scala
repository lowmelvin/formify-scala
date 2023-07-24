package com.melvinlow.formify

opaque type FormKeyFragment = String

object FormKeyFragment {
  inline def apply(fragment: String): FormKeyFragment = fragment

  extension (fragment: FormKeyFragment) {
    inline def underlying: String = fragment
  }
}
