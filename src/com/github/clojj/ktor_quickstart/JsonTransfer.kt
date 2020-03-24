package com.github.clojj.ktor_quickstart

import java.util.*

fun snippetsDto(snippets: List<Snippet>) = mapOf("snippets" to synchronized(snippets) { snippets.toList() })

data class Snippet(val text: String, val created: Date = Date())

data class PostSnippet(val snippet: Text) {
    data class Text(val text: String)
}

class LoginRegister(val user: String, val password: String)
