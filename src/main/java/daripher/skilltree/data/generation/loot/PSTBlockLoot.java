package daripher.skilltree.data.generation.loot;

import daripher.skilltree.init.PSTBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class PSTBlockLoot extends BlockLootSubProvider {
    protected PSTBlockLoot(HolderLookup.Provider lookupProvider) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), lookupProvider);
    }

    @Override
    protected void generate() {
        dropSelf(PSTBlocks.WORKBENCH.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return PSTBlocks.REGISTRY.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
    }
}
