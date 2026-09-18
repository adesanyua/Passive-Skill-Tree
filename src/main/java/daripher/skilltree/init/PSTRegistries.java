package daripher.skilltree.init;

import daripher.skilltree.init.predicate.*;
import daripher.skilltree.skill.bonus.SkillBonus;
import daripher.skilltree.skill.bonus.event.SkillEventListener;
import daripher.skilltree.skill.bonus.function.FloatFunction;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.multiplier.LivingMultiplier;
import daripher.skilltree.skill.bonus.predicate.damage.DamageCondition;
import daripher.skilltree.skill.bonus.predicate.effect.MobEffectPredicate;
import daripher.skilltree.skill.bonus.predicate.enchantment.EnchantmentCondition;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import daripher.skilltree.skill.bonus.predicate.living.LivingEntityPredicate;
import daripher.skilltree.skill.requirement.SkillRequirement;
import net.minecraft.core.Registry;

public class PSTRegistries {
    /** Initializes the custom registries during mod construction, before NewRegistryEvent. */
    public static void init() {
        // Calling this method ensures all static registry fields are initialized.
    }

    public static final Registry<SkillBonus.Serializer> SKILL_BONUSES = PSTSkillBonuses.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<LivingMultiplier.Serializer> LIVING_MULTIPLIERS = PSTLivingMultipliers.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<LivingEntityPredicate.Serializer> LIVING_CONDITIONS = PSTLivingEntityPredicates.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<DamageCondition.Serializer> DAMAGE_CONDITIONS = PSTDamagePredicates.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<ItemStackPredicate.Serializer> ITEM_CONDITIONS = PSTItemPredicates.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<EnchantmentCondition.Serializer> ENCHANTMENT_CONDITIONS = PSTEnchantmentPredicates.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<SkillEventListener.Serializer> EVENT_LISTENERS = PSTEventListeners.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<FloatFunction.Serializer> FLOAT_FUNCTIONS = PSTFloatFunctions.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<SkillRequirement.Serializer> SKILL_REQUIREMENTS = PSTSkillRequirements.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<ItemBonus.Serializer> ITEM_BONUSES = PSTItemBonuses.REGISTRY.makeRegistry(builder -> {});
    public static final Registry<MobEffectPredicate.Serializer> MOB_EFFECT_PREDICATES = PSTMobEffectPredicates.REGISTRY.makeRegistry(builder -> {});
}
