package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class PSTBrewingRecipes {
    @SubscribeEvent
    public static void addRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.FIRE_RESISTANCE, Items.FERMENTED_SPIDER_EYE, PSTPotions.LIQUID_FIRE_1.getDelegate());
        event.getBuilder().addMix(PSTPotions.LIQUID_FIRE_1.getDelegate(), Items.GLOWSTONE_DUST, PSTPotions.LIQUID_FIRE_2.getDelegate());
    }
}
