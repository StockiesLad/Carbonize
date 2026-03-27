package net.jmb19905.mixin;

import com.google.gson.JsonElement;
import net.jmb19905.util.IModel;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.TextureMap;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mixin(Model.class)
public abstract class ModelMixin implements IModel {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow @Final private Optional<String> variant;

    @Shadow
    public abstract Identifier upload(Identifier id, TextureMap textures, BiConsumer<Identifier, Supplier<JsonElement>> modelCollector);

    @Override
    public Identifier carbonize$upload(Identifier id, TextureMap textures, BiConsumer<Identifier, Supplier<JsonElement>> modelCollector) {
        return this.upload(new Identifier(id.getNamespace() + ":block/" + id.getPath() + variant.orElse("")), textures, modelCollector);
    }
}
