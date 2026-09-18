package daripher.skilltree.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockItem extends BlockItem {
    public ModBlockItem(DeferredBlock<Block> blockRegistryObject) {
        super(blockRegistryObject.get(), new Properties());
    }
}
