[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](https://opensource.org/licenses/BSD-3-Clause)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-7f52ff)](https://kotlinlang.org/)
[![bld](https://img.shields.io/badge/2.3.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://img.shields.io/github/release/ethauvin/jokeapi.svg)](https://github.com/ethauvin/jokeapi/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/net.thauvin.erik/jokeapi?color=blue)](https://central.sonatype.com/artifact/net.thauvin.erik/jokeapi)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fnet%2Fthauvin%2Ferik%2Fjokeapi%2Fmaven-metadata.xml&label=snapshot)](https://github.com/ethauvin/jokeapi/packages/2260868/versions)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ethauvin_jokeapi&metric=alert_status)](https://sonarcloud.io/dashboard?id=ethauvin_jokeapi)
[![GitHub CI](https://github.com/ethauvin/jokeapi/actions/workflows/bld.yml/badge.svg)](https://github.com/ethauvin/jokeapi/actions/workflows/bld.yml)
[![CircleCI](https://circleci.com/gh/ethauvin/jokeapi/tree/master.svg?style=shield)](https://circleci.com/gh/ethauvin/jokeapi/tree/master)

# JokeAPI for Kotlin, Java and Android

A simple library to retrieve jokes from [Sv443's JokeAPI](https://v2.jokeapi.dev/).

## Examples (TL;DR)

```kotlin
import net.thauvin.erik.jokeapi.joke

val joke = joke()
val safe = joke(safe = true)
val pun = joke(categories = setOf(Category.PUN))
```
The parameters match the [joke endpoint](https://v2.jokeapi.dev/#joke-endpoint).

A `Joke` class instance is returned, matching the [response](https://v2.jokeapi.dev/joke/Any?type=single):

```kotlin
data class Joke(
    val category: Category,
    val type: Type,
    val joke: List<String>,
    val flags: Set<Flag>,
    val id: Int,
    val safe: Boolean,
    val lang: Language
)
```
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/GetJokeTest.kt#L64)...

To retrieve multiple jokes:

```kotlin
val frenchJokes = jokes(amount = 2, type = Type.TWOPART, lang = Language.FR)
frenchJokes.forEach {
    println(it.joke.joinToString("\n"))
    println("-".repeat(46))
}
```

- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/GetJokesTest.kt#L52)...


If an error occurs, a `JokeException` is thrown, matching the [JokeAPI errors](https://sv443.net/jokeapi/v2/#errors):

```kotlin
class JokeException(
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
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/ExceptionsTest.kt#L57)...

## Java

To make it easier to use the library with Java, a configuration builder is available:

```java
var config = new JokeConfig.Builder()
        .type(Type.SINGLE)
        .safe(true)
        .build();
var joke = JokeApi.joke(config);
joke.getJoke().forEach(System.out::println);
```

## bld

To use with [bld](https://rife2.com/bld), include the following dependency in your build file:

```java
repositories = List.of(MAVEN_CENTRAL, CENTRAL_SNAPSHOTS);

scope(compile)
    .include(dependency("net.thauvin.erik", "jokeapi", "1.0.0"));
```
Be sure to use the [bld Kotlin extension](https://github.com/rife2/bld-kotlin) in your project.

## Gradle, Maven, etc.
To use with [Gradle](https://gradle.org/), include the following dependency in your build file:

```gradle
repositories {
    mavenCentral()
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") } // only needed for SNAPSHOT
}

dependencies {
    implementation("net.thauvin.erik:jokeapi:1.0.0")
}
```

Instructions for using with Maven, Ivy, etc. can be found on [Maven Central](https://central.sonatype.com/artifact/net.thauvin.erik/jokeapi).

## Raw Jokes

You can also retrieve one or more raw (unprocessed) jokes in all [supported formats](https://jokeapi.dev/#format-param).

For example for YAML:
```kotlin
var jokes = getRawJokes(format = Format.YAML, idRange = IdRange(22))
println(jokes.data)
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

- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/GetRawJokesTest.kt#L46)...

## Extending

A generic `apiCall()` function is available to access other [JokeAPI endpoints](https://v2.jokeapi.dev/#endpoints). 

For example to retrieve the French [language code](https://v2.jokeapi.dev/#langcode-endpoint):

```kotlin
val response = JokeApi.apiCall(
    endPoint = "langcode",
    path = "french",
    params = mapOf(Parameter.FORMAT to Format.YAML.value)
)
if (response.statusCode == 200) {
    println(response.data)
}
```

```yaml
error: false
code: "fr"
```
- View more [examples](https://github.com/ethauvin/jokeapi/blob/master/src/test/kotlin/net/thauvin/erik/jokeapi/ApiCallTest.kt#L48)...

## Contributing

If you want to contribute to this project, all you have to do is clone the GitHub
repository:

```console
git clone git@github.com:ethauvin/jokeapi.git
```

Then use [bld](https://rife2.com/bld) to build:

```console
cd jokeapi
./bld compile
```

The project has an [IntelliJ IDEA](https://www.jetbrains.com/idea/) project structure. You can just open it after all
the dependencies were downloaded and peruse the code.
