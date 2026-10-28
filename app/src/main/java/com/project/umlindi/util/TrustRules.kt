package com.project.umlindi.util

object TrustRules {

    const val CONFIRM = "confirm"
    const val DISPUTE = "dispute"

    fun delta(previousVote: String?, newVote: String): Int = scoreOf(newVote) - scoreOf(previousVote)

    private fun scoreOf(vote: String?): Int = when (vote) {
        CONFIRM -> 1
        DISPUTE -> -1
        else -> 0
    }
}