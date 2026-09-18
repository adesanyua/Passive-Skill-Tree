package daripher.skilltree.skill.bonus.handler;

import com.mojang.datafixers.util.Either;
import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.skill.SkillBonusProvider;
import daripher.skilltree.skill.bonus.player.PreventItemUsageBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public class ItemUsagePreventionBonusHandler {
    // recursion protection
    private static boolean isProcessingRejection;

    @SubscribeEvent
    public static void preventItemUsage(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack mainHandItem = player.getMainHandItem();
        if (shouldPreventItemUsage(player, mainHandItem)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void preventRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        preventItemUsage(event);
    }

    @SubscribeEvent
    public static void preventRightClickItem(PlayerInteractEvent.RightClickItem event) {
        preventItemUsage(event);
    }

    @SubscribeEvent
    public static void preventEntityInteraction(PlayerInteractEvent.EntityInteract event) {
        preventItemUsage(event);
    }

    @SubscribeEvent
    public static void preventSpecificEntityInteraction(PlayerInteractEvent.EntityInteractSpecific event) {
        preventItemUsage(event);
    }

    @SubscribeEvent
    public static void preventLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        preventItemUsage(event);
    }

    private static void preventItemUsage(PlayerInteractEvent event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        if (shouldPreventItemUsage(player, itemStack)) {
            if (event instanceof PlayerInteractEvent.RightClickBlock interaction) {
                interaction.setCancellationResult(InteractionResult.FAIL);
            } else if (event instanceof PlayerInteractEvent.RightClickItem interaction) {
                interaction.setCancellationResult(InteractionResult.FAIL);
            } else if (event instanceof PlayerInteractEvent.EntityInteract interaction) {
                interaction.setCancellationResult(InteractionResult.FAIL);
            } else if (event instanceof PlayerInteractEvent.EntityInteractSpecific interaction) {
                interaction.setCancellationResult(InteractionResult.FAIL);
            }
            if (event instanceof ICancellableEvent cancellable) {
                cancellable.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void preventItemEquipping(LivingEquipmentChangeEvent event) {
        if (isProcessingRejection) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!event.getSlot().isArmor()) {
            return;
        }
        ItemStack newArmor = event.getTo();
        if (newArmor.isEmpty()) {
            return;
        }
        if (shouldPreventItemUsage(player, newArmor)) {
            try {
                isProcessingRejection = true;
                if (!player.getInventory().add(newArmor.copy())) {
                    player.drop(newArmor.copy(), false);
                }
                player.setItemSlot(event.getSlot(), ItemStack.EMPTY);
            } finally {
                isProcessingRejection = false;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addPreventedUsageTooltip(RenderTooltipEvent.GatherComponents event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack itemStack = event.getItemStack();
        if (shouldPreventItemUsage(player, itemStack)) {
            Component tooltip = Component.translatable("item.cant_use.info").withStyle(ChatFormatting.RED);
            event.getTooltipElements().add(Either.left(tooltip));
        }
    }

    public static boolean shouldPreventItemUsage(Player player, ItemStack itemStack) {
        List<PreventItemUsageBonus> skillBonuses = SkillBonusProvider.getSkillBonuses(player, PreventItemUsageBonus.class);
        for (PreventItemUsageBonus bonus : skillBonuses) {
            if (bonus.getItemCondition().test(itemStack)) {
                return true;
            }
        }
        return false;
    }
}
