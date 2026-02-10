package net.jmb19905.core;

import net.jmb19905.config.CarbonizeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarbonCore {
    public static final CarbonizeConfig CONFIG = CarbonizeConfig.createAndLoad();
    public static final String MOD_ID = "carbonize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

}
