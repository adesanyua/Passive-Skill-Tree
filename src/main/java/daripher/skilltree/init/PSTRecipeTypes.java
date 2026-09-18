package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.recipe.workbench.AbstractWorkbenchRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class PSTRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_TYPE, SkillTreeMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AbstractWorkbenchRecipe>> WORKBENCH = register("workbench");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(final String identifier) {
        RecipeType<T> recipeType = new RecipeType<>() {
            public String toString() {
                return identifier;
            }
        };
        return REGISTRY.register(identifier, () -> recipeType);
    }
}
