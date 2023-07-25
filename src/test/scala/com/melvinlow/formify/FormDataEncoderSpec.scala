package com.melvinlow.formify

import cats.data.*
import cats.syntax.all.*

object FormDataEncoderSpec extends weaver.FunSuite {
  test("should encode a non-nested product") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int, c: Boolean)

    val instance = A("hello", 123, true)
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect(encoded.compile == Chain(
      ("a", "hello"),
      ("b", "123"),
      ("c", "true")
    ))
  }

  test("should encode a nested product") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String)
    final case class B(b: Int, c: A)

    val instance = B(123, A("hello"))
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect(encoded.compile == Chain(
      ("b", "123"),
      ("c.a", "hello")
    ))
  }

  test("should encode an option with a form value inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Option[String], b: Option[String])

    val instance = A(None, Some("hello"))
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect(encoded.compile == Chain(
      ("b", "hello")
    ))
  }

  test("should encode an option with a form inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String)
    final case class B(a: Option[A], b: Option[A])

    val instance = B(None, Some(A("hello")))
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect(encoded.compile == Chain(
      ("b.a", "hello")
    ))
  }

  test("should encode a list with form values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: List[Int], b: List[Int])

    val instance = A(List(1, 2), List())
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2")
      )
    }
  }

  test("should encode a vector with form values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Vector[Int], b: Vector[Int])

    val instance = A(Vector(1, 2), Vector())
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2")
      )
    }
  }

  test("should encode a seq with form values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Seq[Int], b: Seq[Int])

    val instance = A(Seq(1, 2), Seq())
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2")
      )
    }
  }

  test("should encode an array with form values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Array[Int], b: Array[Int])

    val instance = A(Array(1, 2), Array())
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2")
      )
    }
  }

  test("should encode a list with forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int)
    final case class B(a: List[A], b: List[A])

    val instance = B(List(A("hello", 1), A("world", 2)), List())
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "hello"),
        ("a.0.b", "1"),
        ("a.1.a", "world"),
        ("a.1.b", "2")
      )
    }
  }

  test("should encode a vector with forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int)
    final case class B(a: Vector[A], b: Vector[A])

    val instance = B(Vector(A("hello", 1), A("world", 2)), Vector())
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "hello"),
        ("a.0.b", "1"),
        ("a.1.a", "world"),
        ("a.1.b", "2")
      )
    }
  }

  test("should encode a seq with forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int)
    final case class B(a: Seq[A], b: Seq[A])

    val instance = B(Seq(A("hello", 1), A("world", 2)), Seq())
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "hello"),
        ("a.0.b", "1"),
        ("a.1.a", "world"),
        ("a.1.b", "2")
      )
    }
  }

  test("should encode an array with forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int)
    final case class B(a: Array[A], b: Array[A])

    val instance = B(Array(A("hello", 1), A("world", 2)), Array())
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "hello"),
        ("a.0.b", "1"),
        ("a.1.a", "world"),
        ("a.1.b", "2")
      )
    }
  }

  test("should encode a list with options of values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: List[Option[Int]])

    val instance = A(List(Some(1), Some(2), None, Some(3)))
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2"),
        ("a.2", "3")
      )
    }
  }

  test("should encode a vector with options of values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Vector[Option[Int]])

    val instance = A(Vector(Some(1), Some(2), None, Some(3)))
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2"),
        ("a.2", "3")
      )
    }
  }

  test("should encode a seq with options of values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Seq[Option[Int]])

    val instance = A(Seq(Some(1), Some(2), None, Some(3)))
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2"),
        ("a.2", "3")
      )
    }
  }

  test("should encode an array with options of values inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Array[Option[Int]])

    val instance = A(Array(Some(1), Some(2), None, Some(3)))
    val encoded  = FormDataEncoder[A].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0", "1"),
        ("a.1", "2"),
        ("a.2", "3")
      )
    }
  }

  test("should encode a list with options of forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Int)
    final case class B(a: List[Option[A]])

    val instance = B(List(Some(A(1)), Some(A(2)), None, Some(A(3))))
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "1"),
        ("a.1.a", "2"),
        ("a.2.a", "3")
      )
    }
  }

  test("should encode a vector with options of forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Int)
    final case class B(a: Vector[Option[A]])

    val instance = B(Vector(Some(A(1)), Some(A(2)), None, Some(A(3))))
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "1"),
        ("a.1.a", "2"),
        ("a.2.a", "3")
      )
    }
  }

  test("should encode a seq with options of forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Int)
    final case class B(a: Seq[Option[A]])

    val instance = B(Seq(Some(A(1)), Some(A(2)), None, Some(A(3))))
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "1"),
        ("a.1.a", "2"),
        ("a.2.a", "3")
      )
    }
  }

  test("should encode an array with options of forms inside") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: Int)
    final case class B(a: Array[Option[A]])

    val instance = B(Array(Some(A(1)), Some(A(2)), None, Some(A(3))))
    val encoded  = FormDataEncoder[B].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.0.a", "1"),
        ("a.1.a", "2"),
        ("a.2.a", "3")
      )
    }
  }

  test("should encode a Map[String, Int]") {
    import com.melvinlow.formify.instances.auto.given

    val instance = Map("a" -> 1, "b" -> 2)
    val encoded  = FormDataEncoder[Map[String, Int]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a", "1"),
        ("b", "2")
      )
    }
  }

  test("should encode a Map[String, Product]") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int)

    val instance = Map("a" -> A("hello", 1), "b" -> A("world", 2))
    val encoded  = FormDataEncoder[Map[String, A]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.a", "hello"),
        ("a.b", "1"),
        ("b.a", "world"),
        ("b.b", "2")
      )
    }
  }

  test("should encode a Map[String, Map[String, Option[Product]]]") {
    import com.melvinlow.formify.instances.auto.given

    final case class A(a: String, b: Int)

    val instance = Map(
      "a" -> Map("a" -> Some(A("hello", 1)), "b" -> None),
      "b" -> Map("a" -> Some(A("world", 2)), "b" -> Some(A("!", 3)))
    )
    val encoded = FormDataEncoder[Map[String, Map[String, Option[A]]]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("a.a.a", "hello"),
        ("a.a.b", "1"),
        ("b.a.a", "world"),
        ("b.a.b", "2"),
        ("b.b.a", "!"),
        ("b.b.b", "3")
      )
    }
  }

  test("should encode a Chain[Int]") {
    import com.melvinlow.formify.instances.auto.given

    val instance = Chain(1, 2, 3)
    val encoded  = FormDataEncoder[Chain[Int]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("0", "1"),
        ("1", "2"),
        ("2", "3")
      )
    }
  }

  test("should encode a Chain[List[Int]]") {
    import com.melvinlow.formify.instances.auto.given

    val instance = Chain(List(1, 2), List(3, 4))
    val encoded  = FormDataEncoder[Chain[List[Int]]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("0.0", "1"),
        ("0.1", "2"),
        ("1.0", "3"),
        ("1.1", "4")
      )
    }
  }

  test("should encode a NonEmptyChain[Int]") {
    import com.melvinlow.formify.instances.auto.given

    val instance = NonEmptyChain(1, 2, 3)
    val encoded  = FormDataEncoder[NonEmptyChain[Int]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("0", "1"),
        ("1", "2"),
        ("2", "3")
      )
    }
  }

  test("should encode a NonEmptyChain[List[Int]]") {
    import com.melvinlow.formify.instances.auto.given

    val instance = NonEmptyChain(List(1, 2), List(3, 4))
    val encoded  = FormDataEncoder[NonEmptyChain[List[Int]]].encode(instance)

    given FormFieldComposer = FormFieldComposer.make(_.toList.mkString("."))

    expect {
      encoded.compile == Chain(
        ("0.0", "1"),
        ("0.1", "2"),
        ("1.0", "3"),
        ("1.1", "4")
      )
    }
  }
}
