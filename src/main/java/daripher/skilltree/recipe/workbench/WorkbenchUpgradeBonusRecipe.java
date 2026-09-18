package daripher.skilltree.recipe.workbench;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.inventory.menu.WorkbenchContainer;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.item.ItemBonusHandler;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkbenchUpgradeBonusRecipe extends AbstractWorkbenchRecipe {
    private final ItemStackPredicate baseItemStackPredicate;
    private final Map<Ingredient, Integer> additionalIngredients;
    private final ItemBonus<?> itemBonus;

    public WorkbenchUpgradeBonusRecipe(ResourceLocation id, ItemStackPredicate baseItemStackPredicate, Map<Ingredient, Integer> additionalIngredients, boolean requiresPassiveSkill, ItemBonus<?> itemBonus) {
        super(id, requiresPassiveSkill);
        this.baseItemStackPredicate = baseItemStackPredicate;
        this.itemBonus = itemBonus;
        this.additionalIngredients = additionalIngredients;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull WorkbenchContainer container, @NotNull HolderLookup.Provider lookupProvider) {
        return getResult(container);
    }

    @Override
    public boolean isValidBaseItem(ItemStack itemStack) {
        return baseItemStackPredicate.test(itemStack);
    }

    @Override
    public Map<Ingredient, Integer> getAdditionalIngredients(ItemStack baseIngredient) {
        return additionalIngredients;
    }

    public Map<Ingredient, Integer> getAdditionalIngredients() {
        return additionalIngredients;
    }

    @Override
    public Component getShortDescription() {
        Component itemTooltip = baseItemStackPredicate.getTooltip("plural");
        return Component.translatable(getDescriptionId(), itemBonus.getFullTooltip().get(0), itemTooltip);
    }

    @Override
    public List<Component> getFullDescription() {
        List<Component> fullDescription = new ArrayList<>();
        Style style = TooltipHelper.getItemUpgradeStyle();
        for (MutableComponent mutableComponent : itemBonus.getFullTooltip()) {
            fullDescription.add(mutableComponent.withStyle(style));
        }
        Component itemTooltip = baseItemStackPredicate.getTooltip("plural");
        itemTooltip = Component.literal("[").append(itemTooltip).append("]");
        fullDescription.add(itemTooltip);
        return fullDescription;
    }

    @Override
    public @NotNull ItemStack getResult(WorkbenchContainer workbenchContainer) {
        ItemStack baseItem = workbenchContainer.getBaseItem().copy();
        Player player = workbenchContainer.getPlayer();
        int craftedBonusLimit = ItemBonusHandler.getCraftedBonusLimit(baseItem, player);
        if (craftedBonusLimit <= 0) {
            return baseItem;
        }
        List<ItemBonus<?>> originalBonuses = new ArrayList<>(ItemBonusHandler.getItemBonuses(baseItem));
        while (craftedBonusLimit <= originalBonuses.size()) {
            originalBonuses.remove(0);
        }
        originalBonuses.add(itemBonus.copy());
        ItemBonusHandler.setUpgradeBonuses(baseItem, originalBonuses);
        return baseItem;
    }

    @Override
    public int requiredBaseItemAmount() {
        return 1;
    }

    @Override
    public Pair<Ingredient, Integer> getBaseIngredient() {
        return null;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return PSTRecipeSerializers.WORKBENCH_ITEM_BONUS.get();
    }

    public static class Serializer implements RecipeSerializer<WorkbenchUpgradeBonusRecipe> {
        private static final ResourceLocation UNBOUND_ID =
                ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "unbound_workbench_item_bonus");
        private static final MapCodec<WorkbenchUpgradeBonusRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WorkbenchRecipeCodecs.ITEM_PREDICATE_CODEC.fieldOf("base_item_condition")
                        .forGetter(recipe -> recipe.baseItemStackPredicate),
                WorkbenchRecipeCodecs.ITEM_BONUS_CODEC.fieldOf("item_bonus").forGetter(recipe -> recipe.itemBonus),
                com.mojang.serialization.Codec.BOOL.fieldOf("requires_passive_skill")
                        .forGetter(WorkbenchUpgradeBonusRecipe::hasPassiveSkillRequirement),
                WorkbenchRecipeCodecs.IngredientAmount.CODEC.listOf().fieldOf("additionalIngredients")
                        .forGetter(recipe -> WorkbenchRecipeCodecs.toList(recipe.additionalIngredients))
        ).apply(instance, (predicate, bonus, requiresSkill, ingredients) -> new WorkbenchUpgradeBonusRecipe(
                UNBOUND_ID, predicate, WorkbenchRecipeCodecs.toMap(ingredients), requiresSkill, bonus
        )));
        private static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchUpgradeBonusRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        WorkbenchRecipeCodecs.ITEM_PREDICATE_STREAM_CODEC, recipe -> recipe.baseItemStackPredicate,
                        WorkbenchRecipeCodecs.ITEM_BONUS_STREAM_CODEC, recipe -> recipe.itemBonus,
                        ByteBufCodecs.BOOL, WorkbenchUpgradeBonusRecipe::hasPassiveSkillRequirement,
                        WorkbenchRecipeCodecs.IngredientAmount.STREAM_CODEC.apply(ByteBufCodecs.list()),
                        recipe -> WorkbenchRecipeCodecs.toList(recipe.additionalIngredients),
                        (predicate, bonus, requiresSkill, ingredients) -> new WorkbenchUpgradeBonusRecipe(
                                UNBOUND_ID, predicate, WorkbenchRecipeCodecs.toMap(ingredients), requiresSkill, bonus
                        )
                );

        @Override
        public MapCodec<WorkbenchUpgradeBonusRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WorkbenchUpgradeBonusRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
