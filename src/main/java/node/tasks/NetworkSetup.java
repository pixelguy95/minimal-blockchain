package node.tasks;

import apis.domain.AddrRequest;
import apis.domain.AddrResponse;
import apis.domain.HandshakeRequest;
import apis.domain.HandshakeResponse;
import apis.static_structures.KnownNodesList;
import node.Config;
import utils.RESTUtils;

import java.util.Arrays;

public class NetworkSetup implements Runnable {
    @Override
    public void run() {
        KnownNodesList.getKnownNodes().stream().forEach(n-> System.out.println(n.ip + ":" + n.port));
        HandshakeRequest hsr = Config.generateHandShakeRequest(KnownNodesList.getKnownNodes().iterator().next().asURL());
        HandshakeResponse response = RESTUtils.post(KnownNodesList.getKnownNodes().iterator().next(), "handshake", HandshakeResponse.class, hsr, Arrays.asList());

        if(response.error) {
            System.err.println(response.errorMessage);
            System.exit(1);
        }

        AddrResponse res = RESTUtils.post(KnownNodesList.getKnownNodes().iterator().next(), "addr", AddrResponse.class, new AddrRequest(Config.outwardIP, Config.port), Arrays.asList());

    }
}
