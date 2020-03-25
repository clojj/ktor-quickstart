package com.github.clojj.ktor_quickstart.model

import java.util.*

data class Snippet(val text: String, val email: Email, val created: Date = Date())

class Email private constructor(val email: String){

    companion object {

        fun anEmail(email: String): Email {
            // TODO Validated: https://arrow-kt.io/docs/apidocs/arrow-core-data/arrow.core/-validated/
            return Email(email)
        }
    }
}
