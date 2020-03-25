package com.github.clojj.ktor_quickstart

import com.github.clojj.ktor_quickstart.model.Snippet

fun snippetsDto(snippets: List<Snippet>) = mapOf("snippets" to synchronized(snippets) { snippets.toList() })

data class PostSnippet(val snippet: Text, val email: String) {
    data class Text(val text: String)
}

class LoginRegister(val user: String, val password: String)
