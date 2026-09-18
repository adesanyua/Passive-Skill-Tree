package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.recipe.workbench.WorkbenchCraftingRecipe;
import daripher.skilltree.recipe.workbench.WorkbenchPotionMixingRecipe;
import daripher.skilltree.recipe.workbench.WorkbenchUpgradeBonusRecipe;
import daripher.skilltree.recipe.workbench.WorkbenchWeaponPoisoningRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_SERIALIZER, SkillTreeMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, WorkbenchUpgradeBonusRecipe.Serializer> WORKBENCH_ITEM_BONUS = REGISTRY.register("workbench_item_bonus", WorkbenchUpgradeBonusRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, WorkbenchCraftingRecipe.Serializer> WORKBENCH_CRAFTING = REGISTRY.register("workbench_crafting", WorkbenchCraftingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, WorkbenchPotionMixingRecipe.Serializer> WORKBENCH_POTION_MIXING = REGISTRY.register("workbench_potion_mixing", WorkbenchPotionMixingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, WorkbenchWeaponPoisoningRecipe.Serializer> WORKBENCH_WEAPON_POISONING = REGISTRY.register("workbench_weapon_poisoning", WorkbenchWeaponPoisoningRecipe.Serializer::new);
}
