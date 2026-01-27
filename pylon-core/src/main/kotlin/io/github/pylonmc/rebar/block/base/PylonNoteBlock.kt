package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.NotePlayEvent

interface PylonNoteBlock {
    fun onNotePlay(event: NotePlayEvent)
}