package apis.domain;

import java.util.List;

public class HandshakeResponse implements Response {

    public List<String> knownHosts;

    public HandshakeResponse(List<String> hosts) {
        this.knownHosts = hosts;
    }
}
