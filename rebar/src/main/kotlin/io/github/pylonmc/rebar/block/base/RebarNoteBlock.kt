package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.NotePlayEvent

interface RebarNoteBlock {
    fun onNotePlay(event: NotePlayEvent)
}