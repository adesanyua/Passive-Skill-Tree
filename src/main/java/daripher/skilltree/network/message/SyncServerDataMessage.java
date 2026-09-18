package daripher.skilltree.network.message;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.data.reloader.SkillTreesReloader;
import daripher.skilltree.data.reloader.SkillsReloader;
import daripher.skilltree.network.NetworkHelper;
import daripher.skilltree.skill.PassiveSkill;
import daripher.skilltree.skill.PassiveSkillTree;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class SyncServerDataMessage implements CustomPacketPayload {
    public static final Type<SyncServerDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SkillTreeMod.MOD_ID, "sync_server_data"));
    public static final StreamCodec<FriendlyByteBuf, SyncServerDataMessage> STREAM_CODEC =
            StreamCodec.ofMember(SyncServerDataMessage::encode, SyncServerDataMessage::decode);

    @Override
    public Type<SyncServerDataMessage> type() {
        return TYPE;
    }

    private final List<PassiveSkill> skills;
    private final List<PassiveSkillTree> trees;

    public SyncServerDataMessage() {
        this(List.copyOf(SkillsReloader.getSkills().values()), List.copyOf(SkillTreesReloader.getSkillTrees().values()));
    }

    private SyncServerDataMessage(List<PassiveSkill> skills, List<PassiveSkillTree> trees) {
        this.skills = skills;
        this.trees = trees;
    }

    public static SyncServerDataMessage decode(FriendlyByteBuf buf) {
        return new SyncServerDataMessage(NetworkHelper.readPassiveSkills(buf), NetworkHelper.readPassiveSkillTrees(buf));
    }

    public static void receive(SyncServerDataMessage message, IPayloadContext context) {
        SkillsReloader.getSkills().clear();
        message.skills.forEach(skill -> SkillsReloader.getSkills().put(skill.getId(), skill));
        SkillTreesReloader.getSkillTrees().clear();
        message.trees.forEach(tree -> SkillTreesReloader.getSkillTrees().put(tree.getId(), tree));
    }

    public void encode(FriendlyByteBuf buf) {
        NetworkHelper.writePassiveSkills(buf, skills);
        NetworkHelper.writePassiveSkillTrees(buf, trees);
    }
}
