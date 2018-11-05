package node;

import apis.domain.responses.GetTransactionResponse;
import com.google.gson.*;
import domain.transaction.CoinbaseTransaction;
import domain.transaction.Transaction;

import java.lang.reflect.Type;
import java.util.Base64;

public class SpecialJSONSerializer {
    public static Gson getInstance() {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(GetTransactionResponse.class, transactionDeserializer());

        builder.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getUrlEncoder().withoutPadding().encodeToString(src)));
        builder.registerTypeAdapter(byte[].class, (JsonDeserializer<byte[]>) (json, typeOfT, context) -> Base64.getUrlDecoder().decode(json.getAsString()));
        return builder.create();
    }

    private static JsonDeserializer<GetTransactionResponse> transactionDeserializer() {
        return (JsonDeserializer<GetTransactionResponse>) (json, typeOfT, context) -> {

            JsonObject whole = json.getAsJsonObject();

            if(whole.get("error").getAsBoolean()) {
                return (GetTransactionResponse) new GetTransactionResponse(null).setError(whole.get("errorMessage").getAsString());
            }

            JsonObject transactionObject = whole.get("transaction").getAsJsonObject();

            if(transactionObject.has("randPart")) {
                return new GetTransactionResponse(getInstance().fromJson(transactionObject.toString(), CoinbaseTransaction.class));
            } else {
                return new GetTransactionResponse(getInstance().fromJson(transactionObject.toString(), Transaction.class));
            }
        };
    }
}
