package cores;

import apis.domain.HandshakeResponse;
import com.google.gson.Gson;
import org.restlet.resource.ClientResource;
import utils.RESTUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

public class NodeResponseTest {

    public static void main(String args[]) throws IOException {

        HandshakeResponse r = RESTUtils.get("http://localhost:30109", "handshake", HandshakeResponse.class, Arrays.asList("30109"));
        r.knownHosts.stream().forEach(e-> System.out.println(e));
    }
}
