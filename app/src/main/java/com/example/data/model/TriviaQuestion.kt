package com.example.data.model

data class TriviaQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val spicyFunFact: String
)
