package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.NotePlayEvent

interface NoteBlock {
    fun onNotePlay(event: NotePlayEvent)
}