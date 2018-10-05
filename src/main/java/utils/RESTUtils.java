package utils;

import apis.domain.HandshakeResponse;
import apis.domain.Response;
import com.google.gson.Gson;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class RESTUtils {

    public static <T extends Response> T get(String url, String endpoint, Class<T> returnType, List<String> args) {

        System.out.println(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
        try {
            ClientResource c = new ClientResource(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
            Writer writer = new StringWriter();
            c.get().write(writer);
            return new Gson().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
