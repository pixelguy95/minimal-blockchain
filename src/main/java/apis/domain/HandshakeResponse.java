package apis.domain;

import java.util.List;

public class HandshakeResponse {

    public List<String> knownHosts;

    public HandshakeResponse(List<String> hosts) {
        this.knownHosts = hosts;
    }
}
