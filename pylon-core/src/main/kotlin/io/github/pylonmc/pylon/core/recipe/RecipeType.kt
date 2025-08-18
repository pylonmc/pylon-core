package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.config.adapter.*
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.recipe.vanilla.*
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.ParameterizedType
import java.lang.reflect.RecordComponent
import java.lang.reflect.Type

/**
 * Iteration order will be the order in which recipes were added unless overridden.
 */
open class RecipeType<T : PylonRecipe>(
    private val key: NamespacedKey,
    private val recipeClass: Class<T>
) : Keyed, Iterable<T>, RegistryHandler {

    protected open val registeredRecipes = mutableMapOf<NamespacedKey, T>()
    val recipes: Collection<T>
        get() = registeredRecipes.values

    fun getRecipe(key: NamespacedKey): T? = registeredRecipes[key]

    fun getRecipeOrThrow(key: NamespacedKey): T {
        return registeredRecipes[key] ?: throw NoSuchElementException("No recipe found for key $key in ${this.key}")
    }

    open fun addRecipe(recipe: T) {
        registeredRecipes[recipe.key] = recipe
    }

    open fun removeRecipe(recipe: NamespacedKey) {
        registeredRecipes.remove(recipe)
    }

    fun register() {
        PylonRegistry.RECIPE_TYPES.register(this)
    }

    open fun loadFromConfig(config: ConfigSection) {
        for (key in config.keys) {
            val section = config.getSectionOrThrow(key)
            addRecipe(loadRecipe(NamespacedKey.fromString(key) ?: error("Invalid key: $key"), section))
        }
    }

    private val components: List<RecordComponent> by lazy {
        recipeClass.recordComponents.filterNot { it.isAnnotationPresent(RecipeKey::class.java) }
    }

    private val recipeKey: Int by lazy {
        recipeClass.recordComponents.indexOfFirst { it.isAnnotationPresent(RecipeKey::class.java) }
    }

    private val constructorHandle: MethodHandle by lazy {
        val canonicalConstructor = recipeClass.getDeclaredConstructor(
            *recipeClass.recordComponents.map { it.type }.toTypedArray()
        )
        MethodHandles.lookup().unreflectConstructor(canonicalConstructor)
    }

    protected open fun loadRecipe(key: NamespacedKey, section: ConfigSection): T {
        check(recipeClass.isRecord) {
            "Recipe type $key must be a record class to load from config. " +
                    "Use a custom implementation of RecipeType.loadRecipe if you need to load from config."
        }
        check(recipeKey >= 0) { "Recipe type $key must have a @RecipeKey annotated component to load from config." }

        val values = components.mapTo(mutableListOf()) { component ->
            section.get(component.name, component.type.toAdapter())
        }
        values.add(recipeKey, key)
        return recipeClass.cast(constructorHandle.invokeWithArguments(values))
    }

    override fun iterator(): Iterator<T> = registeredRecipes.values.iterator()

    override fun getKey(): NamespacedKey = key

    companion object {
        /**
         * Key: `minecraft:blasting`
         */
        @JvmField
        val VANILLA_BLASTING = BlastingRecipeType

        /**
         * Key: `minecraft:campfire_cooking`
         */
        @JvmField
        val VANILLA_CAMPFIRE = CampfireRecipeType

        /**
         * Key: `minecraft:smelting`
         */
        @JvmField
        val VANILLA_FURNACE = FurnaceRecipeType

        /**
         * Key: `minecraft:crafting_shaped`
         */
        @JvmField
        val VANILLA_SHAPED = ShapedRecipeType

        /**
         * Key: `minecraft:crafting_shapeless`
         */
        @JvmField
        val VANILLA_SHAPELESS = ShapelessRecipeType

        /**
         * Key: `minecraft:crafting_transmute`
         */
        @JvmField
        val VANILLA_TRANSMUTE = TransmuteRecipeType

        /**
         * Key: `minecraft:smithing_transform`
         */
        @JvmField
        val VANILLA_SMITHING_TRANSFORM = SmithingTransformRecipeType

        /**
         * Key: `minecraft:smithing_trim`
         */
        @JvmField
        val VANILLA_SMITHING_TRIM = SmithingTrimRecipeType

        /**
         * Key: `minecraft:smoking`
         */
        @JvmField
        val VANILLA_SMOKING = SmokingRecipeType

        init {
            VANILLA_BLASTING.register()
            VANILLA_CAMPFIRE.register()
            VANILLA_FURNACE.register()
            VANILLA_SHAPED.register()
            VANILLA_SHAPELESS.register()
            VANILLA_SMITHING_TRANSFORM.register()
            VANILLA_SMITHING_TRIM.register()
            VANILLA_SMOKING.register()
        }

        @JvmStatic
        fun vanillaCraftingRecipes() = VANILLA_SHAPED
            .union(VANILLA_SHAPELESS)
            .union(VANILLA_TRANSMUTE)

        @JvmStatic
        fun vanillaCookingRecipes() = VANILLA_BLASTING.recipes
            .union(VANILLA_CAMPFIRE.recipes)
            .union(VANILLA_FURNACE.recipes)
            .union(VANILLA_SMOKING.recipes)

        @JvmSynthetic
        internal fun addVanillaRecipes() {
            for (recipe in Bukkit.recipeIterator()) {
                // @formatter:off
                when (recipe) {
                    is BlastingRecipe -> VANILLA_BLASTING.addRecipeWithoutRegister(BlastingRecipeWrapper(recipe))
                    is CampfireRecipe -> VANILLA_CAMPFIRE.addRecipeWithoutRegister(CampfireRecipeWrapper(recipe))
                    is FurnaceRecipe -> VANILLA_FURNACE.addRecipeWithoutRegister(FurnaceRecipeWrapper(recipe))
                    is ShapedRecipe -> VANILLA_SHAPED.addRecipeWithoutRegister(ShapedRecipeWrapper(recipe))
                    is ShapelessRecipe -> VANILLA_SHAPELESS.addRecipeWithoutRegister(ShapelessRecipeWrapper(recipe))
                    is TransmuteRecipe -> VANILLA_TRANSMUTE.addRecipeWithoutRegister(TransmuteRecipeWrapper(recipe))
                    is SmithingTransformRecipe -> VANILLA_SMITHING_TRANSFORM.addRecipeWithoutRegister(SmithingTransformRecipeWrapper(recipe))
                    is SmithingTrimRecipe -> VANILLA_SMITHING_TRIM.addRecipeWithoutRegister(SmithingTrimRecipeWrapper(recipe))
                    is SmokingRecipe -> VANILLA_SMOKING.addRecipeWithoutRegister(SmokingRecipeWrapper(recipe))
                }
                // @formatter:on
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun Type.toAdapter(): ConfigAdapter<*> = when (this) {

    is Class<*> if this.isEnum && this != Material::class.java -> EnumConfigAdapter(this as Class<out Enum<*>>)

    is Class<*> -> when (this) {
        String::class.java -> ConfigAdapter.STRING
        Byte::class.javaObjectType, Byte::class.javaPrimitiveType -> ConfigAdapter.BYTE
        Short::class.javaObjectType, Short::class.javaPrimitiveType -> ConfigAdapter.SHORT
        Int::class.javaObjectType, Int::class.javaPrimitiveType -> ConfigAdapter.INT
        Long::class.javaObjectType, Long::class.javaPrimitiveType -> ConfigAdapter.LONG
        Float::class.javaObjectType, Float::class.javaPrimitiveType -> ConfigAdapter.FLOAT
        Double::class.javaObjectType, Double::class.javaPrimitiveType -> ConfigAdapter.DOUBLE
        Char::class.javaObjectType, Char::class.javaPrimitiveType -> ConfigAdapter.CHAR
        Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType -> ConfigAdapter.BOOLEAN
        Material::class.java -> ConfigAdapter.MATERIAL
        NamespacedKey::class.java -> ConfigAdapter.NAMESPACED_KEY
        ItemStack::class.java -> ConfigAdapter.ITEM_STACK
        BlockData::class.java -> ConfigAdapter.BLOCK_DATA
        PylonFluid::class.java -> ConfigAdapter.PYLON_FLUID
        FluidOrItem::class.java -> ConfigAdapter.FLUID_OR_ITEM
        else -> throw IllegalArgumentException("Unsupported type: $this")
    }

    is ParameterizedType -> when (this.rawType) {
        List::class.java -> ListConfigAdapter(this.actualTypeArguments[0].toAdapter())
        Set::class.java -> SetConfigAdapter(this.actualTypeArguments[0].toAdapter())
        Map::class.java -> MapConfigAdapter(
            this.actualTypeArguments[0].toAdapter(),
            this.actualTypeArguments[1].toAdapter()
        )

        else -> throw IllegalArgumentException("Unsupported parameterized type: $this")
    }

    else -> throw IllegalArgumentException("Unsupported type: $this")
}