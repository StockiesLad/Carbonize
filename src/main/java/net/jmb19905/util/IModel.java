package net.jmb19905.util;

import com.google.gson.JsonElement;
import net.minecraft.data.client.TextureMap;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface IModel {
    Identifier carbonize$upload(Identifier id, TextureMap textures, BiConsumer<Identifier, Supplier<JsonElement>> modelCollector);
}
