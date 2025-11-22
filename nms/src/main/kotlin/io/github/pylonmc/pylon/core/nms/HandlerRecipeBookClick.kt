package io.github.pylonmc.pylon.core.nms

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.nms.item.ExtraStackedItemContents
import io.papermc.paper.configuration.GlobalConfiguration
import net.kyori.adventure.text.Component
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.TickThrottler
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractCraftingMenu
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeHolder
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.craftbukkit.util.CraftNamespacedKey
import java.lang.reflect.InvocationTargetException

class HandlerRecipeBookClick(val player: ServerPlayer) {
    val recipeSpamPackets: TickThrottler = TickThrottler(
        GlobalConfiguration.get().spamLimiter.recipeSpamIncrement,
        GlobalConfiguration.get().spamLimiter.recipeSpamLimit
    )
    private val server: MinecraftServer = MinecraftServer.getServer()
    private val cserver: CraftServer = server.server

    // Yoinked from ServerGamePacketListenerImpl#handlePlaceRecipe during version 1.21.10
    @Suppress("removal")
    fun handlePlaceRecipe(packet: ServerboundPlaceRecipePacket) {
        if (!Bukkit.isPrimaryThread()) {
            if (!recipeSpamPackets.isIncrementAndUnderThreshold) {
                this.disconnect()
                return
            }
        }

        this.player.resetLastActionTime()
        if (this.player.isSpectator || this.player.containerMenu.containerId != packet.containerId()) {
            return
        }

        val recipeFromDisplay = server.recipeManager.getRecipeFromDisplay(packet.recipe()) ?: return

        var recipeHolder = recipeFromDisplay.parent()
        if (!this.player.recipeBook.contains(recipeHolder.id())) {
            return
        }

        if (this.player.containerMenu !is RecipeBookMenu) {
            return
        }

        if (recipeHolder.value().placementInfo().isImpossibleToPlace) {
            return
        }

        var recipeName = CraftNamespacedKey.fromMinecraft(recipeHolder.id().location())
        var makeAll = packet.useMaxItems()
        val paperEvent = PlayerRecipeBookClickEvent(
            this.player.bukkitEntity, recipeName, makeAll
        )
        if (!paperEvent.callEvent()) {
            return
        }
        recipeName = paperEvent.recipe
        makeAll = paperEvent.isMakeAll
        if (org.bukkit.event.player.PlayerRecipeBookClickEvent.getHandlerList().getRegisteredListeners().size > 0) {
            val recipe = this.cserver.getRecipe(recipeName) ?: return

            val event = CraftEventFactory.callRecipeBookClickEvent(this.player, recipe, makeAll)
            recipeName = (event.recipe as Keyed).key
            makeAll = event.isShiftClick
        }
        if (this.player.containerMenu !is RecipeBookMenu) {
            return
        }

        recipeHolder = this.server.recipeManager.byKey(
            ResourceKey.create(
                Registries.RECIPE, CraftNamespacedKey.toMinecraft(recipeName)
            )
        ).orElse(null) // Paper - Add PlayerRecipeBookClickEvent - forward to legacy event
        if (recipeHolder == null) {
            return
        }

        // Pylon Start
        val postPlaceAction: PostPlaceAction?
        val menu = player.containerMenu
        if (menu !is RecipeBookMenu) return


        postPlaceAction = if (menu is AbstractCraftingMenu) {
            handlePylonItemPlacement(
                menu,
                makeAll,
                this.player.isCreative,
                recipeHolder,
                this.player.level(),
                this.player.getInventory()
            )
        } else {
            menu.handlePlacement(
                makeAll, this.player.isCreative, recipeHolder, this.player.level(), this.player.getInventory()
            )
        }

        // Pylon end
        if (postPlaceAction == PostPlaceAction.PLACE_GHOST_RECIPE) {
            this.player.connection.send(
                ClientboundPlaceGhostRecipePacket(
                    this.player.containerMenu.containerId,
                    recipeFromDisplay.display().display()
                )
            )
        }
    }

    fun disconnect() {
        player.bukkitEntity.kick(Component.translatable("disconnect.spam"))
    }

    fun handlePylonItemPlacement(
        menu: AbstractCraftingMenu,
        useMaxItems: Boolean,
        isCreative: Boolean,
        recipe: RecipeHolder<*>?,
        level: ServerLevel?,
        playerInventory: Inventory
    ): PostPlaceAction {
        val recipeHolder = recipe as RecipeHolder<CraftingRecipe?>

        try {
            val beginPlacingRecipe = AbstractCraftingMenu::class.java.getDeclaredMethod("beginPlacingRecipe")
            beginPlacingRecipe.setAccessible(true)
            beginPlacingRecipe.invoke(menu)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }

        var postPlaceAction: PostPlaceAction
        try {
            val inputGridSlots = menu.inputGridSlots
            postPlaceAction = PylonServerPlaceRecipe.placeRecipe(
                object : PylonServerPlaceRecipe.PylonCraftingMenuAccess<CraftingRecipe?> {
                    override fun fillCraftSlotsStackedContents(stackedItemContents: ExtraStackedItemContents) {
                        for (stack in menu.craftSlots.contents) {
                            stackedItemContents.accountStack(stack)
                        }
                    }

                    override fun clearCraftingContent() {
                        menu.resultSlots.clearContent()
                        menu.craftSlots.clearContent()
                    }

                    override fun recipeMatches(recipe1: RecipeHolder<CraftingRecipe?>): Boolean {

                        /*
                        val recipe: CraftingRecipe = repiceHolder.value() ?: return false
                        val input = menu.craftSlots.asCraftInput()
                        val ingredients = recipe.placementInfo().ingredients()

                        if (ingredients.size != input.items().size) {
                            return false
                        }

                        for (i in ingredients.indices) {
                            val ingredient = ingredients[i]
                            val stack = input.getItem(i)

                            if (ingredient.isEmpty) {
                                if (!stack.isEmpty) {
                                    return false
                                }
                                continue
                            }

                            if (!ingredientMatchesPylon(ingredient, stack)) {
                                return false
                            }
                        }

                        return true*/

                        return recipe1.value()!!.matches(
                            menu.craftSlots.asCraftInput(),
                            player.level()
                        )
                    }
                },
                menu.gridWidth,
                menu.gridHeight,
                inputGridSlots,
                inputGridSlots,
                playerInventory,
                recipeHolder,
                useMaxItems,
                isCreative
            )!!
        } finally {
            try {
                val finishPlacingRecipe =
                    AbstractCraftingMenu::class.java.getDeclaredMethod("finishPlacingRecipe", ServerLevel::class.java, RecipeHolder::class.java)
                finishPlacingRecipe.setAccessible(true)
                finishPlacingRecipe.invoke(menu, level, recipe)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException(e)
            }
        }

        return postPlaceAction
    }

    companion object {
        fun Ingredient.ingredientMatchesPylon(stack: ItemStack): Boolean {
            // First test vanilla rules (tag/item matches)
            if (!this.test(stack)) return false

            // Then test Pylon matching
            // Compare with ingredient’s exemplar item(s) — we only need one
            if (this.isEmpty) return true
            val stacks = this.itemStacks()

            if (stacks?.isEmpty() ?: true) return true

            // Check Pylon-aware match with ANY ingredient exemplar
            return stacks.any { itemsMatchPylon(it, stack) }
        }

        fun itemsMatchPylon(a: ItemStack, b: ItemStack): Boolean {
            if (a.isEmpty || b.isEmpty) return false
            if (a.item != b.item) return false
            if (a.count < b.count) return false

            // Compare exact components
            val aBukkit = a.bukkitStack
            val bBukkit = b.bukkitStack

            val aPylon = PylonItem.fromStack(aBukkit)
            val bPylon = PylonItem.fromStack(bBukkit);

            if (aPylon === null && bPylon === null) return aBukkit.isSimilar(bBukkit)
            aPylon ?: return false

            return aPylon == bPylon
        }
    }
}
