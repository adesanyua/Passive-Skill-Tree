package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PSTEnchantmentCategories {
    public static final TagKey<Item> SHIELD = create("enchantable/shield");
    public static final TagKey<Item> POTION = create("enchantable/potion");

    private static TagKey<Item> create(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, path));
    }
}
