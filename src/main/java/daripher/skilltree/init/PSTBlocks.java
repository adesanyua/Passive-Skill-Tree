package daripher.skilltree.init;

import daripher.skilltree.SkillTreeMod;
import daripher.skilltree.block.WorkbenchBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PSTBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(SkillTreeMod.MOD_ID);

    // crafting stations
    public static final DeferredBlock<Block> WORKBENCH = REGISTRY.register("workbench", WorkbenchBlock::new);
}
