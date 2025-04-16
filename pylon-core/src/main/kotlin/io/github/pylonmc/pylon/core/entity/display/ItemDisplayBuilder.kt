package io.github.pylonmc.pylon.core.entity.display

import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Matrix4f


@Suppress("unused")
class ItemDisplayBuilder() {

    var itemStack: ItemStack? = null
    var transformation: Matrix4f? = null
    var brightness: Int? = null
    var glowColor: Color? = null
    var billboard: Display.Billboard? = null
    var viewRange: Float? = null
    var interpolationDelay: Int? = null
    var interpolationDuration: Int? = null

    constructor(other: ItemDisplayBuilder): this() {
        this.itemStack = other.itemStack
        this.transformation = other.transformation
        this.brightness = other.brightness
        this.glowColor = other.glowColor
        this.billboard = other.billboard
        this.viewRange = other.viewRange
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    fun material(material: Material): ItemDisplayBuilder = apply { this.itemStack = ItemStack(material) }
    fun itemStack(itemStack: ItemStack?): ItemDisplayBuilder = apply { this.itemStack = itemStack }
    fun transformation(transformation: Matrix4f?): ItemDisplayBuilder = apply { this.transformation = transformation }
    fun transformation(builder: TransformBuilder): ItemDisplayBuilder = apply { this.transformation = builder.buildForItemDisplay() }
    fun brightness(brightness: Int): ItemDisplayBuilder = apply { this.brightness = brightness }
    fun glow(glowColor: Color?): ItemDisplayBuilder = apply { this.glowColor = glowColor }
    fun billboard(billboard: Billboard?): ItemDisplayBuilder = apply { this.billboard = billboard }
    fun viewRange(viewRange: Float): ItemDisplayBuilder = apply { this.viewRange = viewRange }
    fun interpolationDelay(interpolationDelay: Int): ItemDisplayBuilder = apply { this.interpolationDelay = interpolationDelay }
    fun interpolationDuration(interpolationDuration: Int): ItemDisplayBuilder = apply { this.interpolationDuration = interpolationDuration }

    fun build(location: Location): ItemDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0.0F
        finalLocation.pitch = 0.0F
        return finalLocation.getWorld().spawn(finalLocation, ItemDisplay::class.java, this::update)
    }

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