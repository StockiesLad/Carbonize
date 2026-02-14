package net.jmb19905.core;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jmb19905.block.charcoal.BurningSet;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.jmb19905.core.CarbonizeCommon.*;

public class CarbonizeItemGroup {
    public static final ItemGroup ITEM_GROUP;
    public static final RegistryKey<ItemGroup> KEY;

    static {
        ITEM_GROUP = FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.carbonize.charcoals"))
                .icon(CHARCOAL_SET.charringWood.asItem()::getDefaultStack)
                .entries((displayContext, entries) -> {
                    entries.add(WOOD_STACK);
                    entries.add(CarbonizeCommon.ASH_BLOCK);
                    entries.add(CarbonizeCommon.ASH_LAYER);
                    entries.add(CarbonizeCommon.ASH);
                    BurningSet.iterateBlocks(((set, block) -> entries.add(block)));
                })
                .build();
        KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of("carbonize", "charcoals"));
        Registry.register(Registries.ITEM_GROUP, KEY, ITEM_GROUP);

    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            content.add(WOOD_STACK);
            BurningSet.iterateBlocks((set, block) -> content.add(block));
            content.add(ASH_LAYER);
            content.add(ASH_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content ->
                BurningSet.iterateSets(set -> content.add(set.charringWood)));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> content.add(ASH));

    }
}
