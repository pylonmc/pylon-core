package io.github.pylonmc.pylon.core.item

import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.lang.reflect.Method

open class MetaComponentType<T>(metaClass: Class<out ItemMeta>, methodToRun: Method) {
    private val metaSpecification: Class<out ItemMeta> = metaClass
    private val metaModificationMethod: Method = methodToRun
    fun ModifyMeta(itemMeta: ItemMeta, value: T): ItemMeta {
        //(itemMeta as metaSpecification)
        metaModificationMethod.invoke(metaSpecification.cast(itemMeta), value);
        return itemMeta;
    }
}

public final class MetaComponentTypes {
    companion object {
        public final val DURABILITY: MetaComponentType<Int> = MetaComponentType<Int>(Damageable::class.java, Damageable::class.java.getMethod("setMaxDamage", Integer::class.java))
    }
}