# Formify-Scala

Formify-Scala is a utility library designed for the conversion
of generic product types into the `x-www-form-urlencoded` data format.
This format is sometimes required by various APIs (notably the [Stripe API](https://stripe.com/docs/api))
when transmitting data. This library provides a simple
method of converting your algebraic data types into strings
compliant with this content type.

## Background

To clarify the functionality of this library, let's examine
a sample Stripe API request and payload, taken from the official documentation:

```curl
curl https://api.stripe.com/v1/checkout/sessions \
  -u sk_test_4eC39HqLyjWDarjtT1zdp7dc: \
  --data-urlencode success_url="https://example.com/success" \
  -d "line_items[0][price]"=price_H5ggYwtDq4fbrJ \
  -d "line_items[0][quantity]"=2 \
  -d mode=payment
```

The payload could have been modeled in Scala as:

```scala
final case class Payload(line_items: List[LineItem], mode: String)
final case class LineItem(price: String, quantity: Int)

val data = Payload(List(LineItem("price_H5ggYwtDq4fbrJ", 2)), "payment")
```

Formify-Scala allows for the conversion of such a representation back to its original form:


```scala
FormDataEncoder.encode(data).compile.toList
// res0: List[Tuple2[String, String]] = List(
//   ("line_items[0][price]", "price_H5ggYwtDq4fbrJ"),
//   ("line_items[0][quantity]", "2"),
//   ("mode", "payment")
// )

FormDataEncoder.encode(data).serialize
// res1: String = "line_items%5B0%5D%5Bprice%5D=price_H5ggYwtDq4fbrJ&line_items%5B0%5D%5Bquantity%5D=2&mode=payment"
```

The compiled version's type (prior to the `.toList`) is a `Chain[String, String]`,
which can be passed directly to http4s's `UrlForm`.

## Basic Usage

Incorporate the following imports:

```scala
import com.melvinlow.formify.*
import com.melvinlow.formify.instances.auto.given
```

Following this, you need to establish a method to convert
nested field names into a single string
using a `FormFieldComposer`. This step is necessary
because `x-www-form-urlencoded` payloads lack a standard approach for this.

Here is an example of accomplishing this for Stripe's API,
which simply encapsulates each field name that is not at the top level within brackets `"[]"`:

```scala
given FormFieldComposer = FormFieldComposer.make { fragments =>
  fragments.head + fragments.tail.map(f => s"[$f]").toList.mkString
}
```

That's it! With the prior auto imports, you're now equipped to automatically convert your ADTs:

```scala
final case class Cat(owner: Option[String], favorite_foods: Array[String])

val mirai = Cat(None, Array("sushi", "taco bell"))
// mirai: Cat = Cat(owner = None, favorite_foods = Array("sushi", "taco bell"))

FormDataEncoder.encode(mirai).compile.toList
// res3: List[Tuple2[String, String]] = List(
//   ("favorite_foods[0]", "sushi"),
//   ("favorite_foods[1]", "taco bell")
// )
```

## Typeclasses and Extensions

In addition to the `FormKeyComposer`, there are two typeclasses
to be aware of for encoding custom types such as `java.time.Instant`.

### FormValueEncoder[T]

The `FormValueEncoder[T]` typeclass is tasked with converting
the leaves of your ADT into form values. If you're working with
a custom type such as `java.time.Instant`, it's likely you'll
need to provide your own instance of this typeclass.

A `Contravariant` typeclass instance from cats is provided,
meaning you only need to determine how to
convert your custom type to a type already
supported by a `FormValueEncoder` instance. As
an example, `java.time.Instant` could be encoded into
epoch seconds by contramapping it to a `Long`:

```scala
import cats.syntax.all.*
import java.time.Instant

given FormValueEncoder[Instant] = FormValueEncoder[Long].contramap(_.getEpochSecond)
```

After this, you can conveniently use `java.time.Instant` in your ADTs:

```scala
final case class Person(created_at: Instant)

val jay = Person(Instant.now)
// jay: Person = Person(created_at = 2023-07-24T16:11:27.277054Z)

FormDataEncoder.encode(jay).compile.toList
// res4: List[Tuple2[String, String]] = List(("created_at", "1690215087"))
```

### FormDataEncoder[T]

The `FormDataEncoder[T]` typeclass is the one you've
been interacting with from the beginning. It is responsible for converting
the non-leaf, branch parts of your ADT. Should you wish to generate
field names rather than just values, you will need to provide
two instances of this typeclass: one for when your branch type
contains leaf nodes, and another for when it houses another branch.

Once again, a `Contravariant` typeclass instance from cats is provided,
so you simply need to convert your custom type to a type that already
has a `FormDataEncoder` instance. Generally, this
will be a `Map[String, T]` or a `List[T]`, depending on whether
your custom data type has named fields or not.

For instance, you could encode a `Set[T]` by sorting
its elements first and then converting it into a `List[T]`:

```scala
// When T is a branch node
given [T: Ordering: FormDataEncoder]: FormDataEncoder[Set[T]] =
  FormDataEncoder[List[T]].contramap(_.toList.sorted)

// When T is a leaf node
given [T: Ordering: FormValueEncoder]: FormDataEncoder[Set[T]] =
  FormDataEncoder[List[T]].contramap(_.toList.sorted)
```

After this, you can use `Set[T]` in your ADTs:

```scala
final case class Puppy(favorite_words: Set[String])

val aya = Puppy(Set("woof", "wan", "bark", "bitcoin"))
// aya: Puppy = Puppy(favorite_words = Set("woof", "wan", "bark", "bitcoin"))

FormDataEncoder.encode(aya).compile.toList
// res5: List[Tuple2[String, String]] = List(
//   ("favorite_words[0]", "bark"),
//   ("favorite_words[1]", "bitcoin"),
//   ("favorite_words[2]", "wan"),
//   ("favorite_words[3]", "woof")
// )
```

## Important Considerations

In theory, you could have a type that functions as both
a branch node and a leaf node. For instance, you might
wish to encode a `List` as a `String` instead of indexing into it.

To manage this ambiguity, the auto derivation implementation
gives priority to `FormValueEncoder` over `FormDataEncoder`.
You can thus manually prompt the auto derivation to halt and
encode your type as a leaf node (rather than recursing further)
by providing a `FormValueEncoder` instance for it.

As an example, let's force a `List[Int]` to be encoded as a `String`
while keeping the default behavior for `List[String]`:

```scala
given FormValueEncoder[List[Int]] =
  FormValueEncoder[String].contramap(_.mkString("-"))

final case class Bird(ages: List[Int], colors: List[String])

val mai = Bird(List(1, 2, 3), List("red", "blue", "green"))
// mai: Bird = Bird(
//   ages = List(1, 2, 3),
//   colors = List("red", "blue", "green")
// )

FormDataEncoder.encode(mai).compile.toList
// res6: List[Tuple2[String, String]] = List(
//   ("ages", "1-2-3"),
//   ("colors[0]", "red"),
//   ("colors[1]", "blue"),
//   ("colors[2]", "green")
// )
```
