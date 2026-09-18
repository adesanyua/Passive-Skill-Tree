package daripher.skilltree.recipe.workbench;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.inventory.menu.WorkbenchContainer;
import daripher.skilltree.skill.SkillBonusProvider;
import daripher.skilltree.skill.bonus.player.VanillaRecipeUnlockBonus;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class WorkbenchVanillaCraftingRecipe extends AbstractWorkbenchRecipe {
    private @Nullable Pair<Ingredient, Integer> baseIngredient;
    private Map<Ingredient, Integer> additionalIngredients;
    private final ItemStack result;

    public WorkbenchVanillaCraftingRecipe(ResourceLocation id, CraftingRecipe vanillaRecipe, HolderLookup.Provider lookupProvider) {
        super(id, true);
        this.result = vanillaRecipe.getResultItem(lookupProvider);
        additionalIngredients = getIngredientsFromCraftingRecipe(vanillaRecipe);
        List<Pair<Ingredient, Integer>> ingredients = new ArrayList<>(additionalIngredients.entrySet().stream().map(Pair::of).toList());
        if (!ingredients.isEmpty()) {
            this.baseIngredient = ingredients.remove(0);
            additionalIngredients = ingredients.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    private WorkbenchVanillaCraftingRecipe(@NotNull ResourceLocation id, @Nullable Pair<Ingredient, Integer> baseIngredient, Map<Ingredient, Integer> additionalIngredients, ItemStack result) {
        super(id, true);
        this.result = result;
        this.baseIngredient = baseIngredient;
        this.additionalIngredients = additionalIngredients;
    }

    private static Map<Ingredient, Integer> getIngredientsFromCraftingRecipe(CraftingRecipe vanillaRecipe) {
        record IngredientKey(Set<Item> items) {
        }
        Map<IngredientKey, Ingredient> uniqueIngredients = new HashMap<>();
        Map<IngredientKey, Integer> ingredientCounts = new HashMap<>();
        NonNullList<Ingredient> vanillaIngredients = vanillaRecipe.getIngredients();
        for (Ingredient ingredient : vanillaIngredients) {
            ItemStack[] matchingStacks = ingredient.getItems();
            if (matchingStacks.length == 0) {
                continue;
            }
            Set<Item> itemSet = new HashSet<>(matchingStacks.length);
            for (ItemStack matchingStack : matchingStacks) {
                itemSet.add(matchingStack.getItem());
            }
            IngredientKey key = new IngredientKey(itemSet);
            uniqueIngredients.putIfAbsent(key, ingredient);
            ingredientCounts.put(key, ingredientCounts.getOrDefault(key, 0) + 1);
        }
        Map<Ingredient, Integer> result = new HashMap<>(ingredientCounts.size());
        for (Map.Entry<IngredientKey, Integer> entry : ingredientCounts.entrySet()) {
            result.put(uniqueIngredients.get(entry.getKey()), entry.getValue());
        }
        return result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull WorkbenchContainer container, @NotNull HolderLookup.Provider lookupProvider) {
        return getResult(container);
    }

    @Override
    public boolean isValidBaseItem(ItemStack itemStack) {
        if (baseIngredient == null) {
            return false;
        }
        return baseIngredient.getKey().test(itemStack) && itemStack.getCount() >= baseIngredient.getValue();
    }

    @Override
    public Map<Ingredient, Integer> getAdditionalIngredients(ItemStack baseIngredient) {
        return getAdditionalIngredients();
    }

    public Map<Ingredient, Integer> getAdditionalIngredients() {
        return additionalIngredients;
    }

    @Override
    public boolean isLockedFor(@NotNull Player player) {
        List<VanillaRecipeUnlockBonus> recipeUnlockBonuses = SkillBonusProvider.getSkillBonuses(player, VanillaRecipeUnlockBonus.class);
        for (VanillaRecipeUnlockBonus skillBonus : recipeUnlockBonuses) {
            if (skillBonus.canUnlockRecipe(this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Component getShortDescription() {
        return result.getHoverName();
    }

    @Override
    public @NotNull ItemStack getResult(WorkbenchContainer workbenchContainer) {
        return result.copy();
    }

    public @NotNull ItemStack getResult() {
        return result.copy();
    }

    @Override
    public int requiredBaseItemAmount() {
        if (baseIngredient == null) {
            return 0;
        }
        return baseIngredient.getRight();
    }

    @Override
    public @Nullable Pair<Ingredient, Integer> getBaseIngredient() {
        return baseIngredient;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return new Serializer();
    }

    public static class Serializer implements RecipeSerializer<WorkbenchVanillaCraftingRecipe> {
        private static final ResourceLocation UNBOUND_ID =
                ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "unbound_vanilla_crafting");
        private static final MapCodec<WorkbenchVanillaCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WorkbenchRecipeCodecs.IngredientAmount.CODEC.listOf().fieldOf("additionalIngredients")
                        .forGetter(recipe -> WorkbenchRecipeCodecs.toList(recipe.additionalIngredients)),
                WorkbenchRecipeCodecs.IngredientAmount.CODEC.optionalFieldOf("base_ingredient")
                        .forGetter(recipe -> Optional.ofNullable(recipe.baseIngredient)
                                .map(pair -> new WorkbenchRecipeCodecs.IngredientAmount(pair.getLeft(), pair.getRight()))),
                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
        ).apply(instance, (ingredients, base, result) -> new WorkbenchVanillaCraftingRecipe(
                UNBOUND_ID,
                base.map(value -> Pair.of(value.ingredient(), value.amount())).orElse(null),
                WorkbenchRecipeCodecs.toMap(ingredients),
                result
        )));
        private static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchVanillaCraftingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        WorkbenchRecipeCodecs.IngredientAmount.STREAM_CODEC.apply(ByteBufCodecs.list()),
                        recipe -> WorkbenchRecipeCodecs.toList(recipe.additionalIngredients),
                        ByteBufCodecs.optional(WorkbenchRecipeCodecs.IngredientAmount.STREAM_CODEC),
                        recipe -> Optional.ofNullable(recipe.baseIngredient)
                                .map(pair -> new WorkbenchRecipeCodecs.IngredientAmount(pair.getLeft(), pair.getRight())),
                        ItemStack.STREAM_CODEC, recipe -> recipe.result,
                        (ingredients, base, result) -> new WorkbenchVanillaCraftingRecipe(
                                UNBOUND_ID,
                                base.map(value -> Pair.of(value.ingredient(), value.amount())).orElse(null),
                                WorkbenchRecipeCodecs.toMap(ingredients),
                                result
                        )
                );

        @Override
        public MapCodec<WorkbenchVanillaCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WorkbenchVanillaCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
