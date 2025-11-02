package com.cibertec.ciberbet.models

data class Match(
    val team1Name: String = "",
    val team2Name: String = "",
    val score: String = "",
    val time: String = "",
    val league: String = "",
    val location: String = "",
    val team1Logo: String? = null,
    val team2Logo: String? = null
)
