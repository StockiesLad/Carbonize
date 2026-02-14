package net.jmb19905.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RestartRequired;
import net.jmb19905.core.CarbonizeConstants;

@SuppressWarnings("unused")
@Modmenu(modId = CarbonizeConstants.MOD_ID)
@Config(name = CarbonizeConstants.MOD_ID, wrapperName = "CarbonizeConfig")
public class CarbonizeConfigModel {
    @RestartRequired
    public boolean moreBurnableBlocks = true;

    @RestartRequired
    public boolean burnableContainers = false;
    public boolean charcoalPile = true;
    public int charcoalPileMinimumCount = 8;
    public boolean burnCrafting = true;
    public boolean createAsh = true;
    public boolean increasedFireSpreadRange = true;
}
