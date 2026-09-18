package daripher.skilltree.compat.legendarytabs;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.client.screen.SkillTreeScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID, value = Dist.CLIENT)
public final class LegendaryTabsCompatibility {
    private LegendaryTabsCompatibility() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof SkillTreeScreen)) {
            return;
        }
        // Legendary Tabs renders these listeners again in Render.Post. Remove them
        // after its Init.Post handler so they neither cover nor intercept the tree.
        event.getListenersList().stream()
                .filter(listener -> isTabButton(listener.getClass()))
                .toList()
                .forEach(event::removeListener);
    }

    private static boolean isTabButton(Class<?> type) {
        // Keep this integration optional: no Legendary Tabs classes are linked.
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (current.getName().equals("sfiomn.legendarytabs.client.screens.TabButton")
                    || current.getName().equals("sfiomn.legendarytabs.client.screens.NextTabsButton")) {
                return true;
            }
        }
        return false;
    }
}
