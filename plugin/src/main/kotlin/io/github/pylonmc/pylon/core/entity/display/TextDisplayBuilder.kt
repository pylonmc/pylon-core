package io.github.pylonmc.pylon.core.entity.display

import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.TextDisplay.TextAlignment
import org.joml.Matrix4f


@Suppress("unused")
class TextDisplayBuilder() {

    private var text: Component? = null
    private var transformation: Matrix4f? = null
    private var brightness: Brightness? = null
    private var glowColor: Color? = null
    private var viewRange: Float? = null
    private var billboard: Billboard? = null
    private var alignment: TextAlignment? = null
    private var backgroundColor: Color? = null
    private var interpolationDelay: Int? = null
    private var interpolationDuration: Int? = null

    constructor(other: TextDisplayBuilder) : this() {
        this.text = other.text
        this.transformation = other.transformation
        this.brightness = other.brightness
        this.glowColor = other.glowColor
        this.viewRange = other.viewRange
        this.billboard = other.billboard
        this.alignment = other.alignment
        this.backgroundColor = other.backgroundColor
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    fun text(text: String): TextDisplayBuilder = apply { this.text = Component.text(text) }
    fun text(text: Component?): TextDisplayBuilder = apply { this.text = text }
    fun transformation(transformation: Matrix4f?): TextDisplayBuilder = apply { this.transformation = transformation }
    fun transformation(builder: TransformBuilder): TextDisplayBuilder = apply { this.transformation = builder.buildForTextDisplay() }
    fun brightness(brightness: Brightness): TextDisplayBuilder = apply { this.brightness = brightness }
    fun brightness(brightness: Int): TextDisplayBuilder = brightness(Brightness(0, brightness))
    fun glow(glowColor: Color?): TextDisplayBuilder = apply { this.glowColor = glowColor }
    fun viewRange(viewRange: Float): TextDisplayBuilder = apply { this.viewRange = viewRange }
    fun billboard(billboard: Billboard?): TextDisplayBuilder = apply { this.billboard = billboard }
    fun alignment(alignment: TextAlignment?): TextDisplayBuilder = apply { this.alignment = alignment }
    fun backgroundColor(backgroundColor: Color?): TextDisplayBuilder = apply { this.backgroundColor = backgroundColor }
    fun interpolationDelay(interpolationDelay: Int): TextDisplayBuilder = apply { this.interpolationDelay = interpolationDelay }
    fun interpolationDuration(interpolationDuration: Int): TextDisplayBuilder = apply { this.interpolationDuration = interpolationDuration }

    fun build(location: Location): TextDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0f
        finalLocation.pitch = 0f

        return finalLocation.world.spawn(finalLocation, TextDisplay::class.java, this::update)
    }

    fun update(display: TextDisplay) {
        if (text != null) {
            display.text(text)
        }
        if (transformation != null) {
            display.setTransformationMatrix(transformation!!)
        }
        if (brightness != null) {
            display.brightness = brightness
        }
        if (glowColor != null) {
            display.isGlowing = true
            display.glowColorOverride = glowColor
        }
        if (viewRange != null) {
            display.viewRange = viewRange!!
        }
        if (billboard != null) {
            display.billboard = billboard!!
        }
        if (alignment != null) {
            display.alignment = alignment!!
        }
        if (backgroundColor != null) {
            display.backgroundColor = backgroundColor
        }
        if (interpolationDelay != null) {
            display.interpolationDelay = interpolationDelay!!
        }
        if (interpolationDuration != null) {
            display.interpolationDuration = interpolationDuration!!
        }
    }
}