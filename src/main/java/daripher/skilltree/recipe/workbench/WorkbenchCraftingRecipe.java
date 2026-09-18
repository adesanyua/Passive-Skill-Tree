package daripher.skilltree.recipe.workbench;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.inventory.menu.WorkbenchContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class WorkbenchCraftingRecipe extends AbstractWorkbenchRecipe {
    private final @Nullable Pair<Ingredient, Integer> baseIngredient;
    private final Map<Ingredient, Integer> additionalIngredients;
    private final ItemStack result;

    public WorkbenchCraftingRecipe(ResourceLocation id, @Nullable Pair<Ingredient, Integer> baseIngredient, Map<Ingredient, Integer> additionalIngredients, boolean requiresPassiveSkill, ItemStack result) {
        super(id, requiresPassiveSkill);
        this.result = result;
        this.baseIngredient = baseIngredient;
        this.additionalIngredients = additionalIngredients;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull WorkbenchContainer container, @NotNull HolderLookup.Provider lookupProvider) {
        return getResult(container);
    }

    @Override
    public boolean isValidBaseItem(ItemStack itemStack) {
        if (baseIngredient == null) {
            return itemStack.isEmpty();
        }
        return baseIngredient.getLeft().test(itemStack) && itemStack.getCount() >= baseIngredient.getRight();
    }

    @Override
    public Map<Ingredient, Integer> getAdditionalIngredients(ItemStack baseIngredient) {
        return getAdditionalIngredients();
    }

    public Map<Ingredient, Integer> getAdditionalIngredients() {
        return additionalIngredients;
    }

    @Override
    public Component getShortDescription() {
        return result.getHoverName();
    }

    @Override
    public @NotNull ItemStack getResult(WorkbenchContainer workbenchContainer) {
        return result.copy();
    }

    @Override
    public int requiredBaseItemAmount() {
        return baseIngredient == null ? 0 : baseIngredient.getRight();
    }

    @Override
    public @Nullable Pair<Ingredient, Integer> getBaseIngredient() {
        return baseIngredient;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return PSTRecipeSerializers.WORKBENCH_CRAFTING.get();
    }

    public static class Serializer implements RecipeSerializer<WorkbenchCraftingRecipe> {
        private static final ResourceLocation UNBOUND_ID =
                ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "unbound_workbench_crafting");
        private static final MapCodec<WorkbenchCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.BOOL.fieldOf("requires_passive_skill")
                        .forGetter(WorkbenchCraftingRecipe::hasPassiveSkillRequirement),
                WorkbenchRecipeCodecs.IngredientAmount.CODEC.listOf().fieldOf("additionalIngredients")
                        .forGetter(recipe -> WorkbenchRecipeCodecs.toList(recipe.additionalIngredients)),
                WorkbenchRecipeCodecs.IngredientAmount.CODEC.optionalFieldOf("base_ingredient")
                        .forGetter(recipe -> Optional.ofNullable(recipe.baseIngredient)
                                .map(pair -> new WorkbenchRecipeCodecs.IngredientAmount(pair.getLeft(), pair.getRight()))),
                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
        ).apply(instance, (requiresSkill, ingredients, base, result) -> new WorkbenchCraftingRecipe(
                UNBOUND_ID,
                base.map(value -> Pair.of(value.ingredient(), value.amount())).orElse(null),
                WorkbenchRecipeCodecs.toMap(ingredients),
                requiresSkill,
                result
        )));
        private static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchCraftingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL, WorkbenchCraftingRecipe::hasPassiveSkillRequirement,
                        WorkbenchRecipeCodecs.IngredientAmount.STREAM_CODEC.apply(ByteBufCodecs.list()),
                        recipe -> WorkbenchRecipeCodecs.toList(recipe.additionalIngredients),
                        ByteBufCodecs.optional(WorkbenchRecipeCodecs.IngredientAmount.STREAM_CODEC),
                        recipe -> Optional.ofNullable(recipe.baseIngredient)
                                .map(pair -> new WorkbenchRecipeCodecs.IngredientAmount(pair.getLeft(), pair.getRight())),
                        ItemStack.STREAM_CODEC, recipe -> recipe.result,
                        (requiresSkill, ingredients, base, result) -> new WorkbenchCraftingRecipe(
                                UNBOUND_ID,
                                base.map(value -> Pair.of(value.ingredient(), value.amount())).orElse(null),
                                WorkbenchRecipeCodecs.toMap(ingredients),
                                requiresSkill,
                                result
                        )
                );

        @Override
        public MapCodec<WorkbenchCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WorkbenchCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
