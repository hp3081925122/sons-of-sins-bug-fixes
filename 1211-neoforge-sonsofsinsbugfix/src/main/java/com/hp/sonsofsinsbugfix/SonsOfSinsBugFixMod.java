package com.hp.sonsofsinsbugfix;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.event.SonsOfSinsBugFixEvents;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Sons of Sins Bug Fixes（NeoForge 1.21.1）主入口。
 */
@Mod(SonsOfSinsBugFixMod.MOD_ID)
public final class SonsOfSinsBugFixMod {
    public static final String MOD_ID = "sonsofsinsbugfix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SonsOfSinsBugFixMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, SonsOfSinsBugFixConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, SonsOfSinsBugFixConfig.CLIENT_SPEC);
        NeoForge.EVENT_BUS.register(new SonsOfSinsBugFixEvents());
        LOGGER.info("Sons of Sins Bug Fixes (1.21.1 NeoForge) initialized");
    }
}
