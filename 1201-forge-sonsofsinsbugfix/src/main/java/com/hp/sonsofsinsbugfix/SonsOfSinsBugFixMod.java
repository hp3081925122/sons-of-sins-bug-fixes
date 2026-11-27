package com.hp.sonsofsinsbugfix;

import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.event.SonsOfSinsBugFixEvents;
import com.mojang.logging.LogUtils;
import net.mcreator.sonsofsins.jei_recipes.EthericBloodImbibationRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

/**
 * Sons of Sins Bug Fixes（Forge 1.20.1）主入口。
 */
@Mod(SonsOfSinsBugFixMod.MOD_ID)
public final class SonsOfSinsBugFixMod {
    public static final String MOD_ID = "sonsofsinsbugfix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SonsOfSinsBugFixMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SonsOfSinsBugFixConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SonsOfSinsBugFixConfig.CLIENT_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onRegister);

        MinecraftForge.EVENT_BUS.register(new SonsOfSinsBugFixEvents());
        LOGGER.info("Sons of Sins Bug Fixes (1.20.1 Forge) initialized");
    }

    /**
     * 补齐原模组 1.20.1 SonsOfSinsModRecipeTypes 漏注册的 ForgeRegistries.RECIPE_TYPES 键，
     * 解决 KubeJS / EMI / 配方统计模组查询 EthericBloodImbibationRecipe.Type 返回 null 的问题。
     */
    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_TYPES)) {
            ResourceLocation id = new ResourceLocation("sons_of_sins", "etheric_blood_imbibation");
            if (!ForgeRegistries.RECIPE_TYPES.containsKey(id)) {
                event.register(ForgeRegistries.Keys.RECIPE_TYPES, helper ->
                        helper.register(id, EthericBloodImbibationRecipe.Type.INSTANCE));
            }
        }
    }
}
