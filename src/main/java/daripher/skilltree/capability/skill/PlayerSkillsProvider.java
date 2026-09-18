package daripher.skilltree.capability.skill;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.network.message.SyncPlayerSkillsMessage;
import daripher.skilltree.network.message.SyncServerDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = SkillTreeMod.MOD_ID)
public final class PlayerSkillsProvider {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SkillTreeMod.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerSkills>> PLAYER_SKILLS =
            ATTACHMENTS.register("player_skills", () -> AttachmentType.serializable(PlayerSkills::new)
                    .copyOnDeath()
                    .build());

    private PlayerSkillsProvider() {
    }

    @SubscribeEvent
    public static void syncSkills(PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new SyncServerDataMessage());
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new SyncPlayerSkillsMessage(event.getEntity()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void restoreSkillsAttributeModifiers(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        get(player).getPlayerSkills().forEach(skill -> skill.learn(player, false));
    }

    @SubscribeEvent
    public static void sendTreeResetMessage(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getEntity().level().isClientSide) {
            return;
        }
        IPlayerSkills capability = get(player);
        if (capability.isTreeReset()) {
            player.sendSystemMessage(Component.translatable("skilltree.message.reset").withStyle(ChatFormatting.YELLOW));
            capability.setTreeReset(false);
        }
    }

    @SubscribeEvent
    public static void syncPlayerSkills(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new SyncPlayerSkillsMessage(player));
    }

    public static IPlayerSkills get(Player player) {
        return player.getData(PLAYER_SKILLS);
    }

    public static boolean hasSkills(Player player) {
        return player.getData(PLAYER_SKILLS) != null;
    }
}
