package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.recipe.vanilla.*
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.registry.RegistryHandler
import org.bukkit.*
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.*
import java.lang.invoke.MethodHandles
import java.lang.reflect.ParameterizedType
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
        check(recipeClass.isRecord) {
            "Recipe type $key must be a record class to load from config. " +
                    "Use a custom implementation of RecipeType.loadFromConfig if you need to load from config."
        }

        val components = recipeClass.recordComponents.filterNot { it.isAnnotationPresent(RecipeKey::class.java) }
        val recipeKey = recipeClass.recordComponents.indexOfFirst { it.isAnnotationPresent(RecipeKey::class.java) }
        if (recipeKey == -1) {
            error("Recipe type $key must have a @RecipeKey annotated component to load from config.")
        }

        val canonicalConstructor = recipeClass.getDeclaredConstructor(
            *recipeClass.recordComponents.map { it.type }.toTypedArray()
        )
        val constructorHandle = MethodHandles.lookup().unreflectConstructor(canonicalConstructor)

        for (key in config.keys) {
            val subSection = config.getSectionOrThrow(key)
            val values = components.mapTo(mutableListOf()) { component ->
                subSection.getOrThrow<Any?>(component.name)?.let { value ->
                    convertType(value, component.genericType)
                        ?: error("Failed to convert value '$value' for ${component.name} in recipe $key")
                }
            }
            if (recipeKey >= 0) {
                values.add(recipeKey, NamespacedKey.fromString(key) ?: error("Invalid NamespacedKey: $key"))
            }
            addRecipe(recipeClass.cast(constructorHandle.invokeWithArguments(values)))
        }
    }

    private fun convertType(value: Any, type: Type): Any? {
        return when {
            type is ParameterizedType -> convertParameterizedType(type, value)
            type is Class<*> && type.isEnum && type != Material::class.java -> {
                type.enumConstants.find { it.toString().equals(value.toString(), ignoreCase = true) }
            }

            else -> convertConcreteType(type, value)
        }
    }

    // yes this function is a mess please don't judge me :sob:
    private fun convertConcreteType(type: Type, value: Any): Any? {
        fun getItemFromKey(key: String): ItemStack? {
            val key = NamespacedKey.fromString(key) ?: return null
            return PylonRegistry.ITEMS[key]?.itemStack ?: Registry.ITEM.get(key)?.createItemStack()
        }

        return when (type) {
            String::class.java -> value as? String
            Byte::class.javaObjectType, Byte::class.javaPrimitiveType -> (value as? Number)?.toByte()
            Short::class.javaObjectType, Short::class.javaPrimitiveType -> (value as Number).toShort()
            Int::class.javaObjectType, Int::class.javaPrimitiveType -> (value as? Number)?.toInt()
            Long::class.javaObjectType, Long::class.javaPrimitiveType -> (value as? Number)?.toLong()
            Double::class.javaObjectType, Double::class.javaPrimitiveType -> (value as? Number)?.toDouble()
            Float::class.javaObjectType, Float::class.javaPrimitiveType -> (value as? Number)?.toFloat()
            Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType -> value as? Boolean
            Char::class.javaObjectType, Char::class.javaPrimitiveType -> (value as? String)?.singleOrNull()
            NamespacedKey::class.java -> NamespacedKey.fromString((value as? String) ?: return null)
            Material::class.java -> Material.matchMaterial((value as? String) ?: return null)
            BlockData::class.java -> Bukkit.createBlockData((value as? String) ?: return null)
            ItemStack::class.java -> when (value) {
                is Pair<*, *> -> {
                    val itemKey = value.first as? String ?: return null
                    val amount = (value.second as? Number)?.toInt() ?: return null
                    getItemFromKey(itemKey)?.asQuantity(amount)
                }

                is Map<*, *> -> convertConcreteType(type, value.entries.firstOrNull() ?: return null)
                is String -> getItemFromKey(value)
                else -> null
            }

            PylonFluid::class.java -> {
                val key = (value as? String)?.let(NamespacedKey::fromString) ?: return null
                PylonRegistry.FLUIDS[key]
            }

            FluidOrItem::class.java -> {
                val item = convertConcreteType(ItemStack::class.java, value)
                if (item != null) {
                    FluidOrItem.of(item as ItemStack)
                } else when (value) {
                    is Pair<*, *> -> {
                        val fluidKey = (value.first as? String)?.let(NamespacedKey::fromString) ?: return null
                        val amount = (value.second as? Number)?.toDouble() ?: return null
                        FluidOrItem.of(PylonRegistry.FLUIDS[fluidKey] ?: return null, amount)
                    }

                    is Map<*, *> -> convertConcreteType(type, value.entries.firstOrNull() ?: return null)
                    else -> null
                }
            }

            else -> null
        }
    }

    private fun convertParameterizedType(type: ParameterizedType, value: Any): Any? = when (type.rawType) {
        Set::class.java -> {
            val elementType = type.actualTypeArguments.first()
            var value = value
            if (value is Map<*, *>) {
                value = value.toList()
            }
            (value as? Collection<*>)?.mapTo(mutableSetOf()) { convertType(it!!, elementType) }
        }

        List::class.java -> {
            val elementType = type.actualTypeArguments.first()
            var value = value
            if (value is Map<*, *>) {
                value = value.toList()
            }
            (value as? Collection<*>)?.map { convertType(it!!, elementType) }
        }

        Map::class.java -> {
            val keyType = type.actualTypeArguments[0]
            val valueType = type.actualTypeArguments[1]
            if (value !is Map<*, *>) {
                null
            } else {
                buildMap {
                    for ((k, v) in value) {
                        put(convertType(k!!, keyType), convertType(v!!, valueType))
                    }
                }
            }
        }

        else -> null
    }

    override fun iterator(): Iterator<T> = registeredRecipes.values.iterator()

    override fun getKey(): NamespacedKey = key

    companion object {
        @JvmField
        val VANILLA_BLASTING = BlastingRecipeType

        @JvmField
        val VANILLA_CAMPFIRE = CampfireRecipeType

        @JvmField
        val VANILLA_FURNACE = FurnaceRecipeType

        @JvmField
        val VANILLA_SHAPED = ShapedRecipeType

        @JvmField
        val VANILLA_SHAPELESS = ShapelessRecipeType

        @JvmField
        val VANILLA_TRANSMUTE = TransmuteRecipeType

        @JvmField
        val VANILLA_SMITHING_TRANSFORM = SmithingTransformRecipeType

        @JvmField
        val VANILLA_SMITHING_TRIM = SmithingTrimRecipeType

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
                when (recipe) {
                    is BlastingRecipe -> VANILLA_BLASTING.addRecipeWithoutRegister(BlastingRecipeWrapper(recipe))
                    is CampfireRecipe -> VANILLA_CAMPFIRE.addRecipeWithoutRegister(CampfireRecipeWrapper(recipe))
                    is FurnaceRecipe -> VANILLA_FURNACE.addRecipeWithoutRegister(FurnaceRecipeWrapper(recipe))
                    is ShapedRecipe -> VANILLA_SHAPED.addRecipeWithoutRegister(ShapedRecipeWrapper(recipe))
                    is ShapelessRecipe -> VANILLA_SHAPELESS.addRecipeWithoutRegister(ShapelessRecipeWrapper(recipe))
                    is TransmuteRecipe -> VANILLA_TRANSMUTE.addRecipeWithoutRegister(TransmuteRecipeWrapper(recipe))
                    is SmithingTransformRecipe -> VANILLA_SMITHING_TRANSFORM.addRecipeWithoutRegister(SmithingRecipeWrapper(recipe))
                    is SmithingTrimRecipe -> VANILLA_SMITHING_TRIM.addRecipeWithoutRegister(SmithingRecipeWrapper(recipe))
                    is SmokingRecipe -> VANILLA_SMOKING.addRecipeWithoutRegister(SmokingRecipeWrapper(recipe))
                }
            }
        }
    }
}