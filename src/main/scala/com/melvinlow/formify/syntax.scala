package com.melvinlow.formify

object syntax {
  object all {
    extension [T: FormDataEncoder](value: T) {
      inline def asFormData: FormData = FormDataEncoder[T].encode(value)
    }
  }
}
