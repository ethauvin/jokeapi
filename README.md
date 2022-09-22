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
