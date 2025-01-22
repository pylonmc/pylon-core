package io.github.pylonmc.pylon.test;

import io.github.pylonmc.pylon.core.addon.PylonAddon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TestAddon extends JavaPlugin implements PylonAddon {

    @Override
    public void onEnable() {
        getLogger().info("Test addon enabled!");
        TestSerializers serializerTestInstance = new TestSerializers();
        try {
            if(serializerTestInstance.testAllSerializers()){
                getLogger().info("All serializer tests passed!");
            } else {
                getLogger().severe("At least one serializer test failed!");
            }
        } catch (Exception e){
            getLogger().severe("Test failed with an exception:");
            getLogger().severe(Arrays.toString(e.getStackTrace()));
        }
        Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return this;
    }
}
