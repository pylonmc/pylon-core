package io.github.pylonmc.pylon.core.entity.display

import org.bukkit.Location
import org.bukkit.entity.Interaction

class InteractionBuilder {

    var width: Float? = null
    var height: Float? = null

    constructor(other: InteractionBuilder) {
        this.width = other.width
        this.height = other.height
    }

    fun build(location: Location): Interaction {
        val finalLocation = location.clone()
        // Account for Mojang deciding to center the entity on Y but not X and Z for some reason
        if (height != null) {
            finalLocation.subtract(0.0, height!! / 2.0, 0.0)
        }
        return location.getWorld().spawn(finalLocation, Interaction::class.java, this::update)
    }

    fun update(interaction: Interaction) {
        if (width != null) {
            interaction.interactionWidth = width!!
        }
        if (height != null) {
            interaction.interactionHeight = height!!
        }
    }
}