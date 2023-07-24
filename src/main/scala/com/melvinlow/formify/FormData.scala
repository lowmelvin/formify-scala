package com.melvinlow.formify

import cats.data.*

import java.net.URLEncoder

opaque type FormData = Chain[(FormKey, FormValue)]

object FormData {
  inline def Empty: FormData = Chain.empty

  inline def one(key: FormKey, value: FormValue): FormData = Chain.one((key, value))

  extension (data: FormData) {
    inline def underlying: Chain[(FormKey, FormValue)] = data

    inline def ++(other: FormData): FormData = data ++ other

    def prepend(fragment: FormKeyFragment): FormData =
      data.map((key, value) => (key.prepend(fragment), value))

    def compile(using FormKeyCompiler): Chain[(String, String)] =
      data.collect { case (k, FormValue(v)) => (k.compile, v) }

    def serialize(using FormKeyCompiler): String =
      data.compile.map { (k, v) =>
        val kenc = URLEncoder.encode(k, "UTF-8")
        val venc = URLEncoder.encode(v, "UTF-8")
        s"$kenc=$venc"
      }.toList.mkString("&")
  }
}
