package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.effect.LiquidFireEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTMobEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, SkillTreeMod.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> LIQUID_FIRE = REGISTRY.register("liquid_fire", LiquidFireEffect::new);
}
