package daripher.skilltree.recipe.workbench;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.skilltree.data.serializers.SerializationHelper;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.bonus.item.ItemBonus;
import daripher.skilltree.skill.bonus.predicate.item.ItemStackPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class WorkbenchRecipeCodecs {
    static final Codec<ItemStackPredicate> ITEM_PREDICATE_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                JsonObject root = new JsonObject();
                root.add("base_item_condition", toJson(dynamic));
                return SerializationHelper.deserializeItemPredicate(root, "base_item_condition");
            },
            predicate -> {
                JsonObject root = new JsonObject();
                SerializationHelper.serializeItemPredicate(root, predicate, "base_item_condition");
                return new Dynamic<>(JsonOps.INSTANCE, root.get("base_item_condition"));
            }
    );
    static final Codec<ItemBonus<?>> ITEM_BONUS_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                JsonObject root = new JsonObject();
                root.add("item_bonus", toJson(dynamic));
                return SerializationHelper.deserializeItemBonus(root);
            },
            bonus -> {
                JsonObject root = new JsonObject();
                SerializationHelper.serializeItemBonus(root, bonus);
                return new Dynamic<>(JsonOps.INSTANCE, root.get("item_bonus"));
            }
    );
    static final StreamCodec<RegistryFriendlyByteBuf, ItemStackPredicate> ITEM_PREDICATE_STREAM_CODEC =
            StreamCodec.of(NetworkHelper::writeItemPredicate, NetworkHelper::readItemPredicate);
    static final StreamCodec<RegistryFriendlyByteBuf, ItemBonus<?>> ITEM_BONUS_STREAM_CODEC =
            StreamCodec.of(NetworkHelper::writeItemBonus, NetworkHelper::readItemBonus);

    private WorkbenchRecipeCodecs() {
    }

    static Map<Ingredient, Integer> toMap(List<IngredientAmount> ingredients) {
        Map<Ingredient, Integer> result = new LinkedHashMap<>();
        ingredients.forEach(entry -> result.put(entry.ingredient(), entry.amount()));
        return result;
    }

    static List<IngredientAmount> toList(Map<Ingredient, Integer> ingredients) {
        return ingredients.entrySet().stream().map(entry -> new IngredientAmount(entry.getKey(), entry.getValue())).toList();
    }

    private static JsonElement toJson(Dynamic<?> dynamic) {
        return dynamic.convert(JsonOps.INSTANCE).getValue();
    }

    record IngredientAmount(Ingredient ingredient, int amount) {
        static final Codec<IngredientAmount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientAmount::ingredient),
                Codec.INT.fieldOf("required_amount").forGetter(IngredientAmount::amount)
        ).apply(instance, IngredientAmount::new));
        static final StreamCodec<RegistryFriendlyByteBuf, IngredientAmount> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, IngredientAmount::ingredient,
                ByteBufCodecs.VAR_INT, IngredientAmount::amount,
                IngredientAmount::new
        );
    }
}
