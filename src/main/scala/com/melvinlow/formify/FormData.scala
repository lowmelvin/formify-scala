package com.melvinlow.formify

import cats.data.*

import java.net.URLEncoder

opaque type FormData = Chain[(FormField, FormValue)]

object FormData {
  inline def empty: FormData = Chain.empty

  inline def one(key: FormField, value: FormValue): FormData = Chain.one((key, value))

  extension (data: FormData) {
    inline def underlying: Chain[(FormField, FormValue)] = data

    inline def ++(other: FormData): FormData = data ++ other

    inline def isEmpty: Boolean = data.isEmpty

    inline def isNonEmpty: Boolean = !isEmpty

    def prepend(fragment: FormFieldFragment): FormData =
      data.map((key, value) => (key.prepend(fragment), value))

    def compile(using FormFieldComposer): Chain[(String, String)] =
      data.collect { case (k, FormValue(v)) => (k.compile, v) }

    def serialize(using FormFieldComposer): String =
      data.compile.map { (k, v) =>
        val kenc = URLEncoder.encode(k, "UTF-8")
        val venc = URLEncoder.encode(v, "UTF-8")
        s"$kenc=$venc"
      }.toList.mkString("&")
  }
}
