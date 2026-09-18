package daripher.skilltree.data.generation.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PSTLootTablesProvider extends LootTableProvider {
    public static final Set<ResourceKey<LootTable>> REQUIRED_TABLES = Set.of();

    public PSTLootTablesProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(generator.getPackOutput(), REQUIRED_TABLES, List.of(createBlockLootProvider()), lookupProvider);
    }

    @NotNull
    private static SubProviderEntry createBlockLootProvider() {
        return new SubProviderEntry(PSTBlockLoot::new, LootContextParamSets.BLOCK);
    }
}
