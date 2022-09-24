# JokeAPI for Kotlin/Java

A simple Kotlin/Java library to retrieve jokes from [Sv443's JokeAPI](https://v2.jokeapi.dev/).

## Examples (TL;DR)

```kotlin
import net.thauvin.erik.jokeapi.JokeApi.Companion.getJoke

val joke = getJoke()
val safe = getJoke(safe = true)
val pun = getJoke(category = Category.PUN)
```
The parameters match the [joke endpoint](/https://v2.jokeapi.dev/#joke-endpoint).

A `Joke` class instance is returned:

```kotlin
data class Joke(
    val error: Boolean,
    val category: Category,
    val type: Type,
    val joke: List<String>,
    val flags: Set<Flag>,
    val id: Int,
    val safe: Boolean,
    val language: Language
)
```
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/GetJokeTest.kt)...

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
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/Exceptions.kt)...

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
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/GetRawJokeTest.kt)...

## Extending

A generic `apiCall()` function is available to access other [JokeAPI endpoints](https://v2.jokeapi.dev/#endpoints). 

For example to retrieve the French [language code](https://v2.jokeapi.dev/#langcode-endpoint):

```kotlin
val lang = apiCall(
    endPoint = "langcode",
    path = "french",
    params = mapOf(Parameter.FORMAT to Format.YAML.value)
)
println(lang)
```
```yaml
error: false
code: "fr"
```
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/Exceptions.kt)...






