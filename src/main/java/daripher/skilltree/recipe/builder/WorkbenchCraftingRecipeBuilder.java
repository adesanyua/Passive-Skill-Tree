package daripher.skilltree.recipe.builder;

import daripher.skilltree.recipe.workbench.WorkbenchCraftingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WorkbenchCraftingRecipeBuilder {
    private final ResourceLocation id;
    private final Map<Ingredient, Integer> ingredients = new HashMap<>();
    private @Nullable Pair<Ingredient, Integer> baseIngredient;
    private boolean requiresPassiveSkill;
    private ItemStack result;

    private WorkbenchCraftingRecipeBuilder(ResourceLocation id) { this.id = id; }

    public static WorkbenchCraftingRecipeBuilder create(ResourceLocation id) { return new WorkbenchCraftingRecipeBuilder(id); }
    public WorkbenchCraftingRecipeBuilder setBaseIngredient(Ingredient ingredient, int amount) { baseIngredient = Pair.of(ingredient, amount); return this; }
    public WorkbenchCraftingRecipeBuilder addIngredients(Ingredient ingredient, int amount) { ingredients.put(ingredient, amount); return this; }
    public WorkbenchCraftingRecipeBuilder setRequiresPassiveSkill() { requiresPassiveSkill = true; return this; }
    public WorkbenchCraftingRecipeBuilder setResult(@NotNull ItemStack result) { this.result = result; return this; }

    public void save(RecipeOutput output) {
        validate();
        output.accept(id, new WorkbenchCraftingRecipe(id, baseIngredient, ingredients, requiresPassiveSkill, result), null);
    }

    private void validate() {
        if (ingredients.isEmpty()) throw new IllegalStateException("No ingredients set for recipe " + id);
        if (ingredients.size() > 9) throw new IllegalStateException("Too many ingredients set for recipe " + id);
        if (result == null) throw new IllegalStateException("No result item set for recipe " + id);
    }
}
