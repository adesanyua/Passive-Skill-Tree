package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.init.PSTDamageTypes;
import daripher.skilltree.skill.bonus.handler.LethalPoisonBonusHandler;
import daripher.skilltree.skill.bonus.handler.SkillBonusHandlerUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
public abstract class MobEffectMixin {
    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    public void inflictPoisonDamage(LivingEntity livingEntity, int amplifier, CallbackInfoReturnable<Boolean> callbackInfo) {
        LivingEntity attacker = livingEntity.getKillCredit();
        float damage = 1f;
        boolean isLowHealth = livingEntity.getHealth() <= damage;
        boolean isPoisonLethal = LethalPoisonBonusHandler.canEntityKillWithPoison(attacker);
        if (isLowHealth && !isPoisonLethal) {
            return;
        }
        DamageSources damageSources = livingEntity.damageSources();
        DamageSource damageSource = damageSources.magic();
        if (attacker instanceof Player player) {
            Holder.Reference<DamageType> damageTypeHolder = getPoisonDamageType(player);
            damageSource = new DamageSource(damageTypeHolder, player, null);
            // resets hurt timer
            livingEntity.setLastHurtByPlayer(player);
        }
        SkillBonusHandlerUtils.hurtIgnoringInvulnerabilityTime(livingEntity, damageSource, damage);
        callbackInfo.setReturnValue(true);
    }

    private static @NotNull Holder.Reference<DamageType> getPoisonDamageType(Player player) {
        Registry<DamageType> damageTypeRegistry = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        return damageTypeRegistry.getHolderOrThrow(PSTDamageTypes.POISON);
    }
}
