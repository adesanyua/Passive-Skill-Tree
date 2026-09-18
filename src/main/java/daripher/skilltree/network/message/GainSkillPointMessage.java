package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.config.ServerConfig;
import daripher.skilltree.exp.ExpHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GainSkillPointMessage implements CustomPacketPayload {
    public static final Type<GainSkillPointMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "gain_skill_point"));
    public static final StreamCodec<FriendlyByteBuf, GainSkillPointMessage> STREAM_CODEC =
            StreamCodec.ofMember(GainSkillPointMessage::encode, GainSkillPointMessage::decode);

    @Override
    public Type<GainSkillPointMessage> type() {
        return TYPE;
    }

    public static GainSkillPointMessage decode(FriendlyByteBuf buf) {
        return new GainSkillPointMessage();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void receive(GainSkillPointMessage message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        IPlayerSkills capability = PlayerSkillsProvider.get(player);
        int skills = capability.getPlayerSkills().size();
        int points = capability.getSkillPoints();
        int level = skills + points;
        if (level >= ServerConfig.max_skill_points) {
            return;
        }
        int cost = ServerConfig.getSkillPointCost(level);
        if (ExpHelper.getPlayerExp(player) < cost) {
            return;
        }
        player.giveExperiencePoints(-cost);
        capability.grantSkillPoints(1);
        PacketDistributor.sendToPlayer(player, new SyncPlayerSkillsMessage(player));
    }
}
