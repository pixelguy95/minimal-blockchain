package cores;

import apis.domain.requests.HandshakeRequest;
import apis.domain.responses.HandshakeResponse;
import utils.RESTUtils;

import java.io.IOException;
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
