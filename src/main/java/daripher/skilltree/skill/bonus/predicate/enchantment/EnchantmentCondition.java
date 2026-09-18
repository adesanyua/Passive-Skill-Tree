package daripher.skilltree.skill.bonus.predicate.enchantment;

import daripher.skilltree.init.PSTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Objects;

public interface EnchantmentCondition {
    boolean met(TagKey<Item> supportedItems);

    default String getDescriptionId() {
        ResourceLocation id = PSTRegistries.ENCHANTMENT_CONDITIONS.getKey(getSerializer());
        Objects.requireNonNull(id);
        return "enchantment_condition.%s.%s".formatted(id.getNamespace(), id.getPath());
    }

    EnchantmentCondition.Serializer getSerializer();

    interface Serializer extends daripher.skilltree.data.serializers.Serializer<EnchantmentCondition> {
        EnchantmentCondition createDefaultInstance();
    }
}
