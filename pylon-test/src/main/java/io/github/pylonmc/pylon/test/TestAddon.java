package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class TestAddon extends JavaPlugin implements PylonAddon {

    @Override
    public void onEnable() {
        getLogger().info("Test addon enabled!");
        if(TestSerializers.testAllSerializers()){
            getLogger().info("All serializer tests passed!");
        }
        else{
            getLogger().severe("At least one serializer test failed!");
        }
        Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }
}
