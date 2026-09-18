package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.capability.skill.IPlayerSkills;
import daripher.skilltree.capability.skill.PlayerSkillsProvider;
import daripher.skilltree.client.screen.SkillTreeScreen;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.skill.PassiveSkill;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SyncPlayerSkillsMessage implements CustomPacketPayload {
    public static final Type<SyncPlayerSkillsMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "sync_player_skills"));
    public static final StreamCodec<FriendlyByteBuf, SyncPlayerSkillsMessage> STREAM_CODEC =
            StreamCodec.ofMember(SyncPlayerSkillsMessage::encode, SyncPlayerSkillsMessage::decode);

    @Override
    public Type<SyncPlayerSkillsMessage> type() {
        return TYPE;
    }

    private List<ResourceLocation> learnedSkills = new ArrayList<>();
    private int skillPoints;

    private SyncPlayerSkillsMessage() {
    }

    public SyncPlayerSkillsMessage(Player player) {
        IPlayerSkills skillsCapability = PlayerSkillsProvider.get(player);
        learnedSkills = skillsCapability.getPlayerSkills().stream().map(PassiveSkill::getId).toList();
        skillPoints = skillsCapability.getSkillPoints();
    }

    public static SyncPlayerSkillsMessage decode(FriendlyByteBuf buf) {
        SyncPlayerSkillsMessage result = new SyncPlayerSkillsMessage();
        int learnedSkillsCount = buf.readInt();
        for (int i = 0; i < learnedSkillsCount; i++) {
            result.learnedSkills.add(ResourceLocation.parse(buf.readUtf()));
        }
        result.skillPoints = buf.readInt();
        return result;
    }

    public static void receive(SyncPlayerSkillsMessage message, IPayloadContext context) {
        handlePacket(message);
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void handlePacket(SyncPlayerSkillsMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        IPlayerSkills capability = PlayerSkillsProvider.get(minecraft.player);
        capability.getPlayerSkills().clear();
        message.learnedSkills.stream().map(SkillsReloader::getSkillById).filter(Objects::nonNull)
                .forEach(capability.getPlayerSkills()::add);
        capability.setSkillPoints(message.skillPoints);
        if (minecraft.screen instanceof SkillTreeScreen screen) {
            screen.updateSkillPoints(capability.getSkillPoints());
            screen.init();
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(learnedSkills.size());
        learnedSkills.stream().map(ResourceLocation::toString).forEach(buf::writeUtf);
        buf.writeInt(skillPoints);
    }
}
