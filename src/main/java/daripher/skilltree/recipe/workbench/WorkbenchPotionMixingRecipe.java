package daripher.skilltree.recipe.workbench;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.init.PSTRecipeSerializers;
import daripher.skilltree.inventory.menu.WorkbenchContainer;
import daripher.skilltree.util.PotionHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorkbenchPotionMixingRecipe extends AbstractWorkbenchRecipe {
    public static final String IS_MIXTURE_TAG_NAME = "isMixture";
    private static Ingredient cachedAllPotionsIngredient = null;
    private static final Map<PotionItem, Ingredient> cachedPotionItemIngredients = new ConcurrentHashMap<>();

    public WorkbenchPotionMixingRecipe(ResourceLocation id, boolean requiresPassiveSkill) {
        super(id, requiresPassiveSkill);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull WorkbenchContainer container, @NotNull HolderLookup.Provider lookupProvider) {
        return getResult(container);
    }

    @Override
    public boolean isValidBaseItem(ItemStack itemStack) {
        return isValidPotion(itemStack);
    }

    @Override
    public boolean isValidIngredient(ItemStack itemStack) {
        return isValidPotion(itemStack);
    }

    private boolean isValidPotion(ItemStack itemStack) {
        return itemStack.getItem() instanceof PotionItem && canMixPotion(itemStack);
    }

    @Override
    public Pair<Ingredient, Integer> getBaseIngredient() {
        return Pair.of(getAllPotionsIngredient(), 1);
    }

    private void setIsMixtureTag(ItemStack itemStack) {
        CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> tag.putBoolean(IS_MIXTURE_TAG_NAME, true));
    }

    private boolean canMixPotion(ItemStack itemStack) {
        return !itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(IS_MIXTURE_TAG_NAME);
    }

    private Ingredient getPotionItemIngredient(PotionItem baseItem) {
        return cachedPotionItemIngredients.computeIfAbsent(
                baseItem, item -> {
                    Collection<Potion> availablePotions = BuiltInRegistries.POTION.stream().toList();
                    ItemStack[] suitablePotionStacks = availablePotions.stream()
                            .filter(potion -> !potion.getEffects().isEmpty())
                            .map(potion -> getPotionStack(item, potion))
                            .toArray(ItemStack[]::new);
                    return Ingredient.of(suitablePotionStacks);
                }
        );
    }

    private Ingredient getAllPotionsIngredient() {
        if (cachedAllPotionsIngredient == null) {
            Collection<Potion> availablePotions = BuiltInRegistries.POTION.stream().toList();
            List<Potion> potionsWithEffects = availablePotions.stream().filter(potion -> !potion.getEffects().isEmpty()).toList();
            List<PotionItem> potionItems = BuiltInRegistries.ITEM.stream()
                    .filter(PotionItem.class::isInstance)
                    .map(PotionItem.class::cast)
                    .toList();
            List<ItemStack> suitablePotionStacks = new ArrayList<>();
            for (Potion potion : potionsWithEffects) {
                for (PotionItem potionItem : potionItems) {
                    suitablePotionStacks.add(getPotionStack(potionItem, potion));
                }
            }
            cachedAllPotionsIngredient = Ingredient.of(suitablePotionStacks.toArray(new ItemStack[0]));
        }
        return cachedAllPotionsIngredient;
    }

    private static @NotNull ItemStack getPotionStack(PotionItem baseItem, Potion potion) {
        ItemStack itemStack = new ItemStack(baseItem);
        PotionHelper.setPotion(itemStack, potion);
        return itemStack;
    }

    @Override
    public Map<Ingredient, Integer> getAdditionalIngredients(ItemStack baseItem) {
        if (baseItem.isEmpty()) {
            return Collections.singletonMap(getAllPotionsIngredient(), 1);
        }
        Item item = baseItem.getItem();
        if (!(item instanceof PotionItem potionItem)) {
            return Collections.singletonMap(getAllPotionsIngredient(), 1);
        }
        return Collections.singletonMap(getPotionItemIngredient(potionItem), 1);
    }

    @Override
    public Component getShortDescription() {
        return Component.translatable(getDescriptionId());
    }

    @Override
    public @NotNull ItemStack getResult(WorkbenchContainer workbenchContainer) {
        ItemStack potionStack1 = workbenchContainer.getBaseItem();
        ItemStack potionStack2 = workbenchContainer.getItem(1);
        ItemStack resultItemStack = new ItemStack(potionStack1.getItem());
        setMixtureEffects(potionStack1, potionStack2, resultItemStack);
        setMixtureColor(potionStack1, potionStack2, resultItemStack);
        setMixtureName(potionStack1, resultItemStack);
        setIsMixtureTag(resultItemStack);
        return resultItemStack;
    }

    private void setMixtureEffects(ItemStack potionStack1, ItemStack potionStack2, ItemStack resultItemStack) {
        List<MobEffectInstance> mobEffectInstances = new ArrayList<>();
        mobEffectInstances.addAll(PotionHelper.getEffects(potionStack1));
        mobEffectInstances.addAll(PotionHelper.getEffects(potionStack2));
        PotionHelper.setEffects(resultItemStack, mobEffectInstances);
    }

    private void setMixtureColor(ItemStack potionStack1, ItemStack potionStack2, ItemStack resultItemStack) {
        int potionColor = mixHexColors(PotionHelper.getColor(potionStack1), PotionHelper.getColor(potionStack2));
        PotionHelper.setColor(resultItemStack, potionColor);
    }

    private void setMixtureName(ItemStack potionStack1, ItemStack resultItemStack) {
        String descriptionId = potionStack1.getItem().getDescriptionId() + ".mixture";
        MutableComponent itemStackName = Component.translatable(descriptionId);
        resultItemStack.set(DataComponents.CUSTOM_NAME, itemStackName);
    }

    private int mixHexColors(int color1, int color2) {
        return ((color1 ^ color2) & 0xFEFEFE) >> 1 + (color1 & color2);
    }

    @Override
    public int requiredBaseItemAmount() {
        return 1;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return PSTRecipeSerializers.WORKBENCH_POTION_MIXING.get();
    }

    public static class Serializer implements RecipeSerializer<WorkbenchPotionMixingRecipe> {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "potion_mixing");
        private static final MapCodec<WorkbenchPotionMixingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.BOOL.fieldOf("requires_passive_skill")
                        .forGetter(WorkbenchPotionMixingRecipe::hasPassiveSkillRequirement)
        ).apply(instance, requiresSkill -> new WorkbenchPotionMixingRecipe(ID, requiresSkill)));
        private static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchPotionMixingRecipe> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, WorkbenchPotionMixingRecipe::hasPassiveSkillRequirement,
                        requiresSkill -> new WorkbenchPotionMixingRecipe(ID, requiresSkill));

        @Override
        public MapCodec<WorkbenchPotionMixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WorkbenchPotionMixingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
