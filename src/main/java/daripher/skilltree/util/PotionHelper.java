package daripher.skilltree.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PotionHelper {
    private PotionHelper() {
    }

    public static PotionContents getContents(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    public static List<MobEffectInstance> getEffects(ItemStack stack) {
        List<MobEffectInstance> effects = new ArrayList<>();
        getContents(stack).getAllEffects().forEach(effects::add);
        return effects;
    }

    public static void setPotion(ItemStack stack, Potion potion) {
        setPotion(stack, BuiltInRegistries.POTION.wrapAsHolder(potion));
    }

    public static void setPotion(ItemStack stack, Holder<Potion> potion) {
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    }

    public static void setEffects(ItemStack stack, List<MobEffectInstance> effects) {
        PotionContents contents = getContents(stack);
        stack.set(DataComponents.POTION_CONTENTS,
                new PotionContents(contents.potion(), contents.customColor(), List.copyOf(effects)));
    }

    public static int getColor(ItemStack stack) {
        return getContents(stack).getColor();
    }

    public static void setColor(ItemStack stack, int color) {
        PotionContents contents = getContents(stack);
        stack.set(DataComponents.POTION_CONTENTS,
                new PotionContents(contents.potion(), Optional.of(color), contents.customEffects()));
    }
}
