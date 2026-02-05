package net.jmb19905.block;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;

import java.util.function.Supplier;

public class UnregisteredFireBlock extends FireBlock implements Unregistered {
    public final Supplier<AbstractFireBlock> parentSupplier;

    public UnregisteredFireBlock(Settings settings, Supplier<AbstractFireBlock> parentSupplier) {
        super(settings);
        this.parentSupplier = parentSupplier;
    }

    public static void registerDefaultFlammables() {
        var soulFireBlock = (ISoulFireAccess) Blocks.SOUL_FIRE;

        soulFireBlock.carbonize$registerFlammableBlock(Blocks.CRIMSON_PLANKS, 50, 50);
        soulFireBlock.carbonize$registerFlammableBlock(Blocks.CRIMSON_HYPHAE, 50, 50);
        soulFireBlock.carbonize$registerFlammableBlock(Blocks.WARPED_PLANKS, 50, 50);
    }
}
