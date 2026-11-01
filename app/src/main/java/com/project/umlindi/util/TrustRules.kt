package com.project.umlindi.util

/**
 * Trust score arithmetic for the community voting feature. A confirm is worth +1
 * and a dispute -1. Changing a vote swings the score by 2, because the old
 * vote is undone and the new one applied.
 */
object TrustRules {

    const val CONFIRM = "confirm"
    const val DISPUTE = "dispute"

    /** Change to the reporter's score caused by a voter moving from previousVote to newVote. */

    fun delta(previousVote: String?, newVote: String): Int = scoreOf(newVote) - scoreOf(previousVote)

    private fun scoreOf(vote: String?): Int = when (vote) {
        CONFIRM -> 1
        DISPUTE -> -1
        else -> 0
    }
}