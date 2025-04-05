package io.github.pylonmc.pylon.core.entity.display

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Matrix4f

class ItemDisplayBuilder {

    var itemStack: ItemStack? = null
    var transformation: Matrix4f? = null
    var brightness: Int? = null
    var glowColor: Color? = null
    var billboard: Display.Billboard? = null
    var viewRange: Float? = null
    var interpolationDelay: Int? = null
    var interpolationDuration: Int? = null

    constructor(other: ItemDisplayBuilder) {
        this.itemStack = other.itemStack
        this.transformation = other.transformation
        this.brightness = other.brightness
        this.glowColor = other.glowColor
        this.billboard = other.billboard
        this.viewRange = other.viewRange
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    @Override
    fun build(location: Location): ItemDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0.0F
        finalLocation.pitch = 0.0F

        return finalLocation.getWorld().spawn(finalLocation, ItemDisplay::class.java) {
            update(it)
            it.displayWidth = 0.0F
            it.displayHeight = 0.0F
        }
    }

    @Override
    fun update(display: Display) {
        if (display !is ItemDisplay) {
            throw IllegalArgumentException("Must provide an ItemDisplay")
        }
        if (itemStack != null) {
            display.setItemStack(itemStack)
        }
        if (transformation != null) {
            display.setTransformationMatrix(transformation!!)
        }
        if (glowColor != null) {
            display.isGlowing = true
            display.glowColorOverride = glowColor
        }
        if (brightness != null) {
            display.brightness = Brightness(brightness!!, 0)
        }
        if (billboard != null) {
            display.billboard = billboard!!
        }
        if (viewRange != null) {
            display.viewRange = viewRange!!
        }
        if (interpolationDelay != null) {
            display.interpolationDelay = interpolationDelay!!
        }
        if (interpolationDuration != null) {
            display.interpolationDuration = interpolationDuration!!
        }
    }
}