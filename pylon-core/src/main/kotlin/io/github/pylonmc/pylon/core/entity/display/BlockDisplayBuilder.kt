package io.github.pylonmc.pylon.core.entity.display

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Display.Brightness
import org.joml.Matrix4f


class BlockDisplayBuilder {

    private var material: Material? = null
    private var blockData: BlockData? = null
    private var transformation: Matrix4f? = null
    private var glowColor: Color? = null
    private var brightness: Int? = null
    private var viewRange: Float? = null
    private var interpolationDelay: Int? = null
    private var interpolationDuration: Int? = null

    fun BlockDisplayBuilder() {}

    fun BlockDisplayBuilder(other: BlockDisplayBuilder) {
        this.material = other.material
        this.blockData = other.blockData
        this.transformation = other.transformation
        this.glowColor = other.glowColor
        this.brightness = other.brightness
        this.viewRange = other.viewRange
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    fun build(location: Location): BlockDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0.0f
        finalLocation.pitch = 0.0f
        return location.world.spawn(finalLocation, BlockDisplay::class.java) {
            update(it)
            it.displayWidth = 0f
            it.displayHeight = 0f
        }
    }

    fun update(display: Display) {
        require(display is BlockDisplay) { "Must provide a BlockDisplay" }
        if (material != null) {
            display.block = material!!.createBlockData()
        }
        if (blockData != null) {
            display.block = blockData!!
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