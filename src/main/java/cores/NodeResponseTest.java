package cores;

import apis.domain.HandshakeRequest;
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

        //HandshakeResponse r = RESTUtils.get("http://localhost:30109", "handshake", HandshakeResponse.class, Arrays.asList("30109"));

        HandshakeResponse r = RESTUtils.post("http://localhost:30109",
                "handshake",
                HandshakeResponse.class,
                new HandshakeRequest(1, Arrays.asList(HandshakeRequest.NODE_NETWORK), System.currentTimeMillis(), "localhost:30109","localhost:30110", (short) 30110, "Crap0.0.1", 100), Arrays.asList());

        System.out.println(r.error + " " + r.errorMessage);
    }
}
