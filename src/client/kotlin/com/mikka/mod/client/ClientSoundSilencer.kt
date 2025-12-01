package com.mikka.mod.client

object ClientSoundSilencer {
    var isSilenced = false
    private var silenceVotes = 0

    fun tick() {
        isSilenced = silenceVotes > 0
        silenceVotes = 0
    }

    fun requestSilence() {
        silenceVotes++
    }
}
