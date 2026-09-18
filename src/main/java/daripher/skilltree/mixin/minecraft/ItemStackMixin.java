package daripher.skilltree.mixin.minecraft;

import daripher.skilltree.skill.bonus.handler.ItemDurabilityLossPreventionBonusHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"), cancellable = true)
    private void preventDurabilityLoss(int amount, ServerLevel level, @Nullable LivingEntity entity,
                                      Consumer<Item> onBreak, CallbackInfo callbackInfo) {
        if (amount <= 0 || !(entity instanceof ServerPlayer playerUsingItem)) {
            return;
        }
        @SuppressWarnings("DataFlowIssue") ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isDamageableItem() && !playerUsingItem.hasInfiniteMaterials()
                && ItemDurabilityLossPreventionBonusHandler.shouldPreventItemDurabilityLoss(playerUsingItem, itemStack, playerUsingItem.getRandom())) {
            callbackInfo.cancel();
        }
    }
}
