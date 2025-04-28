package io.github.pylonmc.pylon.core.command

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

// can't name this key because the kotlin generated getter conflicts with the getKey method, DO NOT ASK HOW LONG IT TOOK ME TO FIGURE THIS OUT
open class Subsystem(private val subkey: NamespacedKey) : Keyed {
    override fun getKey(): NamespacedKey = subkey
}

interface ReloadableSubsystem {
    fun reload()
}

open class SimpleReloadableSubsystem(key: NamespacedKey, val name: String, val reloadFunction: () -> Unit) : Subsystem(key), ReloadableSubsystem {
    override fun reload(){
        Bukkit.getServer().sendMessage(Component.text("Reloading $name subsystem...").color(NamedTextColor.YELLOW))
        reloadFunction.invoke()
        Bukkit.getServer().sendMessage(Component.text("Finished reloading $name subsystem").color(NamedTextColor.GREEN))
    }

    companion object {
        @JvmField
        val TRANSLATIONS = SimpleReloadableSubsystem(pylonKey("sub_translations"), "translations", {
            // Do reload
        })

        @JvmField
        val CONFIGS = SimpleReloadableSubsystem(pylonKey("sub_configs"), "configs", {
            // Do reload
        })

        @JvmField
        val ALL = SimpleReloadableSubsystem(pylonKey("sub_all"), "all", {
            for(subsystem in PylonRegistry.SUBSYSTEMS){
                if(subsystem is ReloadableSubsystem){
                    subsystem.reload()
                }
            }
        })
    }
}