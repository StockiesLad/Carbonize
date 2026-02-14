package net.jmb19905.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.jmb19905.model.CharringWoodBlockModel;

import static net.jmb19905.core.CarbonizeConstants.MOD_ID;

public class CarbonizeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register((model, bakeContext) -> {
			if (bakeContext.id().getNamespace().equals(MOD_ID) && bakeContext.id().getPath().contains("charring_wood")) {
				return new CharringWoodBlockModel(model);
			} else return model;
		}));
	}
}