package daripher.skilltree.data.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Type;

public final class ComponentJsonSerializer implements JsonSerializer<MutableComponent>, JsonDeserializer<MutableComponent> {
    @Override
    public MutableComponent deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow(JsonParseException::new).copy();
    }

    @Override
    public JsonElement serialize(MutableComponent component, Type type, JsonSerializationContext context) {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component)
                .getOrThrow(JsonParseException::new);
    }
}
