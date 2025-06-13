package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.ResearchButton
import org.bukkit.Material
import org.bukkit.NamespacedKey
import xyz.xenondevs.invui.item.Item

/**
 * A guide page which has a fixed set of buttons. Do not use this if the buttons
 * on the page need to change at runtime.
 */
open class SimpleStaticGuidePage @JvmOverloads constructor(
    private val key: NamespacedKey,
    material: Material,
    val buttons: MutableList<Item> = mutableListOf()
) : SimpleDynamicGuidePage(key, material, { buttons }) {

    override fun getKey(): NamespacedKey = key

    open fun addItem(item: NamespacedKey) = buttons.add(ItemButton(item))
    open fun addFluid(fluid: NamespacedKey) = buttons.add(FluidButton(fluid))
    open fun addResearch(research: NamespacedKey) = buttons.add(ResearchButton(research))
    open fun addPage(page: GuidePage) = buttons.add(PageButton(page))
}