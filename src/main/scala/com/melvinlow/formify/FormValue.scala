package com.melvinlow.formify

opaque type FormValue = Option[String]

object FormValue {
  inline def Empty: FormValue = None

  inline def apply(value: String): FormValue = Some(value)

  inline def unapply(value: FormValue): Option[String] = value

  extension (value: FormValue) {
    inline def underlying: Option[String] = value
  }
}
