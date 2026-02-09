package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.jmb19905.Carbonize;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class CarbonizeItemTagDataGen extends FabricTagProvider.ItemTagProvider {
    public CarbonizeItemTagDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        var consumeIgniters = getOrCreateTagBuilder(Carbonize.CONSUME_IGNITERS);
        consumeIgniters.add(Items.FIRE_CHARGE);

        var damageIgniters = getOrCreateTagBuilder(Carbonize.DAMAGE_IGNITERS);
        damageIgniters.add(Items.FLINT_AND_STEEL);

        var igniters = getOrCreateTagBuilder(Carbonize.IGNITERS);
        igniters.forceAddTag(Carbonize.CONSUME_IGNITERS);
        igniters.forceAddTag(Carbonize.DAMAGE_IGNITERS);
    }
}
