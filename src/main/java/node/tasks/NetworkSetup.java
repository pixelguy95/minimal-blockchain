package node.tasks;

import apis.domain.Host;
import apis.domain.requests.AddrRequest;
import apis.domain.requests.HandshakeRequest;
import apis.domain.responses.AddrResponse;
import apis.domain.responses.GetAddrResponse;
import apis.domain.responses.HandshakeResponse;
import apis.static_structures.KnownNodesList;
import node.Config;
import utils.RESTUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkSetup implements Runnable {
    @Override
    public void run() {
        KnownNodesList.getKnownNodes().stream().forEach(n-> System.out.println(n.ip + ":" + n.port));
        HandshakeRequest hsr = Config.generateHandShakeRequest(KnownNodesList.getKnownNodes().iterator().next().asURL());
        HandshakeResponse response = RESTUtils.post(KnownNodesList.getKnownNodes().iterator().next(), "handshake", HandshakeResponse.class, hsr);

        if(response.error) {
            System.err.println(response.errorMessage);
            System.exit(1);
        }

        LinkedList<Host> nodeQueue = new LinkedList<>();
        nodeQueue.add(KnownNodesList.getKnownNodes().iterator().next());

        Host me = new Host(Config.outwardIP, Config.port);

        while(!nodeQueue.isEmpty()) {
            Host next = nodeQueue.poll();

            if(me.equals(next) || !RESTUtils.exists(next)) {
                System.err.println("FAULTY NODE GIVEN, IGNORED");
                break;
            }

            RESTUtils.post(next, "addr", AddrResponse.class, new AddrRequest(Config.outwardIP, Config.port));
            GetAddrResponse gres = RESTUtils.get(next, "getaddr", GetAddrResponse.class);

            gres.knownHosts.stream().forEach(h-> System.out.println(h.asURL()));

            for(Host h : gres.knownHosts) {
                if(!KnownNodesList.getKnownNodes().contains(h) && !me.equals(h)) {
                    nodeQueue.add(h);
                    KnownNodesList.getKnownNodes().add(h);
                }
            }
        }


        System.out.println("Nodes added, network size: " + (KnownNodesList.getKnownNodes().size() + 1));
    }
}
