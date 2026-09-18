package daripher.skilltree.init.predicate;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.tooltip.TooltipHelper;
import daripher.skilltree.init.PSTRegistries;
import daripher.skilltree.skill.bonus.predicate.item.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Objects;

public class PSTItemPredicates {
    public static final ResourceLocation REGISTRY_ID = ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "item_conditions");
    public static final DeferredRegister<ItemStackPredicate.Serializer> REGISTRY = DeferredRegister.create(REGISTRY_ID, SkillTreeMod.MOD_ID);

    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> NONE = REGISTRY.register("none", NoneItemStackPredicate.Serializer::new);
    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> POTIONS = REGISTRY.register("potion", PotionStackPredicate.Serializer::new);
    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> FOOD = REGISTRY.register("food", FoodStackPredicate.Serializer::new);
    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> ITEM_ID = REGISTRY.register("item_id", ItemIdPredicate.Serializer::new);
    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> ENCHANTED = REGISTRY.register("enchanted", EnchantedStackPredicate.Serializer::new);
    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> TAG = REGISTRY.register("tag", ItemTagPredicate.Serializer::new);
    public static final DeferredHolder<ItemStackPredicate.Serializer, ItemStackPredicate.Serializer> EQUIPMENT_TYPE = REGISTRY.register("equipment_type", EquipmentPredicate.Serializer::new);

    public static List<ItemStackPredicate> conditionsList() {
        return PSTRegistries.ITEM_CONDITIONS.stream().map(ItemStackPredicate.Serializer::createDefaultInstance).toList();
    }

    public static String getName(ItemStackPredicate condition) {
        ResourceLocation id = PSTRegistries.ITEM_CONDITIONS.getKey(condition.getSerializer());
        return TooltipHelper.idToName(Objects.requireNonNull(id).getPath());
    }
}
