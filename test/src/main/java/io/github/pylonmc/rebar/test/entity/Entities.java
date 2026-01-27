package io.github.pylonmc.rebar.test.entity;

import io.github.pylonmc.rebar.entity.RebarEntity;
import org.bukkit.entity.LivingEntity;


public final class Entities {

    private Entities() {}

    public static void register() {
        RebarEntity.register(SimpleEntity.KEY, LivingEntity.class, SimpleEntity.class);
        RebarEntity.register(EntityEventError.KEY, LivingEntity.class, EntityEventError.class);
    }
}
