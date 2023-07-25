# Formify-Scala

Formify is a Scala 3 utility library built to convert
generic product types into the `x-www-form-urlencoded` data format.
This format is sometimes required by various APIs (notably the [Stripe API](https://stripe.com/docs/api))
when transmitting data. This library offers a simple
method of transforming your algebraic data types into strings
compliant with this content type.

```scala
libraryDependencies += "com.melvinlow" %% "formify" % <version>
```

## Background

To better understand the functionality of this library, let's take a look at
a sample Stripe API request and payload, as per the official documentation:

```curl
curl https://api.stripe.com/v1/checkout/sessions \
  -u sk_test_4eC39HqLyjWDarjtT1zdp7dc: \
  --data-urlencode success_url="https://example.com/success" \
  -d "line_items[0][price]"=price_H5ggYwtDq4fbrJ \
  -d "line_items[0][quantity]"=2 \
  -d mode=payment
```

This payload can be modeled in Scala as follows:

```scala mdoc:silent
final case class Payload(line_items: List[LineItem], mode: String)
final case class LineItem(price: String, quantity: Int)

val data = Payload(List(LineItem("price_H5ggYwtDq4fbrJ", 2)), "payment")
```

Formify facilitates the transformation of such a representation back to its original form:

```scala mdoc:invisible
import com.melvinlow.formify.*
import com.melvinlow.formify.instances.auto.given
import com.melvinlow.formify.syntax.all.*

given FormFieldComposer = FormFieldComposer.make { fragments =>
  fragments.head + fragments.tail.map(f => s"[$f]").toList.mkString
}
```

```scala mdoc
FormDataEncoder.encode(data).compile.toList

FormDataEncoder.encode(data).serialize
```

The compiled version (before `.toList`) is a `Chain[String, String]`,
which can be passed directly to http4s's `UrlForm`.

## Basic Usage

Start by including the following imports:

```scala mdoc:reset
import com.melvinlow.formify.*
import com.melvinlow.formify.instances.auto.given
```

Following this, you need to provide a method to merge
nested field names into a single string
using a `FormFieldComposer`. This step is necessary
because `x-www-form-urlencoded` payloads lack a standard approach for this.

Here is an example of accomplishing this for Stripe's API,
where each field name not at the top level is enclosed within brackets "[]":

```scala mdoc
given FormFieldComposer = FormFieldComposer.make { fragments =>
  fragments.head + fragments.tail.map(f => s"[$f]").toList.mkString
}
```

That's it! With the prior auto imports, you're now equipped to automatically convert your ADTs:

```scala mdoc
final case class Cat(owner: Option[String], favorite_foods: Array[String])

val mirai = Cat(None, Array("sushi", "taco bell"))

FormDataEncoder.encode(mirai).compile.toList
```

Finally, if you'd like, you can import the `syntax` package to
gain access to the `asFormData` extension shortcut:

```scala mdoc
import com.melvinlow.formify.syntax.all.*

mirai.asFormData.compile.toList
```

## Typeclasses and Extensions

Besides the `FormFieldComposer`, there are two important
typeclasses for encoding custom types like `java.time.Instant`.

### FormValueEncoder[T]

The `FormValueEncoder[T]` typeclass converts
the leaf nodes of your ADT into form values. If you're working with
a custom type such as `java.time.Instant`, it's likely you'll
need to provide your own instance of this typeclass.

A `Contravariant` typeclass instance from cats is provided,
meaning you only need to determine how to
convert your custom type to a type already
supported by a `FormValueEncoder` instance. For instance, `java.time.Instant` could be encoded into
epoch seconds by contramapping it to a `Long`:

```scala mdoc
import cats.syntax.all.*
import java.time.Instant

given FormValueEncoder[Instant] = FormValueEncoder[Long].contramap(_.getEpochSecond)
```

After this, you can conveniently use `java.time.Instant` in your ADTs:

```scala mdoc
final case class Person(created_at: Instant)

val jay = Person(Instant.now)

FormDataEncoder.encode(jay).compile.toList
```

### FormDataEncoder[T]

The `FormDataEncoder[T]` typeclass is the one you've
been interacting with from the beginning. It is responsible for converting
the non-leaf, branch parts of your ADT. If you need to generate
field names rather than just values, you will need to provide
two instances of this typeclass: one for when your branch type
contains leaf nodes and another for when it contains another branch.

Once again, a `Contravariant` typeclass instance from cats is provided,
so you simply need to convert your custom type to a type that already
has a `FormDataEncoder` instance. Generally, this
will be a `Map[String, T]` or a `List[T]`, depending on whether
your custom data type has named fields or not.

For example, you could encode a `Set[T]` by sorting
its elements first and then converting it into a `List[T]`:

```scala mdoc
// When T is a branch node
given [T: Ordering: FormDataEncoder]: FormDataEncoder[Set[T]] =
  FormDataEncoder[List[T]].contramap(_.toList.sorted)

// When T is a leaf node
given [T: Ordering: FormValueEncoder]: FormDataEncoder[Set[T]] =
  FormDataEncoder[List[T]].contramap(_.toList.sorted)
```

After this, you can use `Set[T]` in your ADTs:

```scala mdoc
final case class Puppy(favorite_words: Set[String])

val aya = Puppy(Set("woof", "wan", "bark", "bitcoin"))

FormDataEncoder.encode(aya).compile.toList
```

## Important Considerations

In practice, a type can serve both as a branch node
and a leaf node. For instance, you might
want to encode a `List` as a `String` instead of indexing into it.

To manage this ambiguity, the auto derivation implementation
gives priority to `FormValueEncoder` over `FormDataEncoder`.
You can thus manually prompt the auto derivation to halt and
encode your type as a leaf node (instead of further recursion)
by providing a `FormValueEncoder` instance for it.

As an example, let's force a `List[Int]` to be encoded as a `String`
while keeping the default behavior for `List[String]`:

```scala mdoc
given FormValueEncoder[List[Int]] =
  FormValueEncoder[String].contramap(_.mkString(" and "))

final case class Bird(ages: List[Int], colors: List[String])

val mai = Bird(List(1, 2, 3), List("red", "blue", "green"))

FormDataEncoder.encode(mai).compile.toList
```
