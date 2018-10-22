package node;

import com.google.gson.*;

import java.util.Base64;

public class SpecialJSONSerializer {
    public static Gson getInstance() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getUrlEncoder().withoutPadding().encodeToString(src)));
        builder.registerTypeAdapter(byte[].class, (JsonDeserializer<byte[]>) (json, typeOfT, context) -> Base64.getUrlDecoder().decode(json.getAsString()));
        return builder.create();
    }
}
