package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.ResearchButton
import io.github.pylonmc.pylon.core.item.research.Research
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item

/**
 * A guide page which has a fixed set of buttons. Do not use this if the buttons
 * on the page need to change at runtime.
 */
open class SimpleStaticGuidePage @JvmOverloads constructor(
    /**
     * A key that uniquely identifies this page. Used to get the translation keys for this page.
     */
    private val key: NamespacedKey,

    material: Material,

    /**
     * The list of buttons to be displayed on this page.
     */
    val buttons: MutableList<Item> = mutableListOf()
) : SimpleDynamicGuidePage(key, material, { buttons }) {

    override fun getKey(): NamespacedKey = key

    open fun addItem(item: ItemStack) = buttons.add(ItemButton(item))
    open fun addFluid(fluid: PylonFluid) = buttons.add(FluidButton(fluid))
    open fun addResearch(research: Research) = buttons.add(ResearchButton(research))
    open fun addPage(page: GuidePage) = buttons.add(PageButton(page))
}