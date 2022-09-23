# JokeAPI for Kotlin/Java

A simple Kotlin/Java library to retrieve jokes from [Sv443's JokeAPI](https://v2.jokeapi.dev/).

## Examples (TL;DR)

```kotlin
import net.thauvin.erik.jokeapi.JokeApi.Companion.getJoke

val joke = getJoke()
val safe = getJoke(safe = true)
val pun = getJoke(category = Category.PUN)
```

A `Joke` class instance is returned:

```kotlin
data class Joke(
    val error: Boolean,
    val category: Category,
    val type: Type,
    val joke: Set<String>,
    val flags: Set<Flag>,
    val id: Int,
    val safe: Boolean,
    val language: Language
)
```

If an error occurs, a `JokeException` is thrown:

```kotlin
class JokeException(
    val error: Boolean,
    val internalError: Boolean,
    val code: Int,
    message: String,
    val causedBy: List<String>,
    val additionalInfo: String,
    val timestamp: Long,
    cause: Throwable? = null
) : Exception(message, cause)
```

If an HTTP error occurs an `HttpErrorException` is thrown, with its message and cause matching the [JokeAPI status codes](https://sv443.net/jokeapi/v2/#status-codes):

```kotlin
class HttpErrorException(
    val statusCode: Int,
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)
```

## Gradle, Maven, etc.
To use with [Gradle](https://gradle.org/), include the following dependency in your build file:

```gradle
dependencies {
    implementation("net.thauvin.erik:jokeapi:0.9-SNAPSHOT")
}
```

Instructions for using with Maven, Ivy, etc. can be found on Maven Central.

## Raw Joke

You can also retrieve a raw joke in all [supported formats](https://jokeapi.dev/#format-param).

For example for YAML:
```kotlin
var joke = getRawJoke(format = Format.YAML, idRange = IdRange(22))
println(joke)
```
```yaml
error: false
category: "Programming"
type: "single"
joke: "If Bill Gates had a dime for every time Windows crashed ... Oh wait, he does."
flags:
  nsfw: false
  religious: false
  political: false
  racist: false
  sexist: false
  explicit: false
id: 22
safe: true
lang: "en"

```





