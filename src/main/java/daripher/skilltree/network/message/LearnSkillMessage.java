package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.skill.PassiveSkill;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LearnSkillMessage implements CustomPacketPayload {
    public static final Type<LearnSkillMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "learn_skill"));
    public static final StreamCodec<FriendlyByteBuf, LearnSkillMessage> STREAM_CODEC =
            StreamCodec.ofMember(LearnSkillMessage::encode, LearnSkillMessage::decode);

    @Override
    public Type<LearnSkillMessage> type() {
        return TYPE;
    }

    private ResourceLocation skillId;

    public LearnSkillMessage(PassiveSkill passiveSkill) {
        skillId = passiveSkill.getId();
    }

    private LearnSkillMessage() {
    }

    public static LearnSkillMessage decode(FriendlyByteBuf buf) {
        LearnSkillMessage message = new LearnSkillMessage();
        message.skillId = ResourceLocation.parse(buf.readUtf());
        return message;
    }

    public static void receive(LearnSkillMessage message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        IPlayerSkills capability = PlayerSkillsProvider.get(player);
        PassiveSkill skill = SkillsReloader.getSkillById(message.skillId);
        if (skill == null) {
            return;
        }
        if (capability.learnSkill(skill)) {
            skill.learn(player, true);
        }
        PacketDistributor.sendToPlayer(player, new SyncPlayerSkillsMessage(player));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(skillId.toString());
    }
}
