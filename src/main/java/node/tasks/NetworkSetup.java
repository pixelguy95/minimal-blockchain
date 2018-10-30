package node.tasks;

import apis.domain.Host;
import apis.domain.requests.AddrRequest;
import apis.domain.requests.HandshakeRequest;
import apis.domain.responses.AddrResponse;
import apis.domain.responses.GetAddrResponse;
import apis.domain.responses.HandshakeResponse;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import node.Config;
import utils.RESTUtils;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkSetup extends AbstractTask {

    private KnownNodesList knownNodesList;
    private Blockchain blockchain;
    private Config config;

    public NetworkSetup(AtomicBoolean keepAlive, Config config, KnownNodesList knownNodesList, Blockchain blockchain) {
        super(keepAlive);
        this.config = config;
        this.knownNodesList = knownNodesList;
        this.blockchain = blockchain;
    }

    @Override
    public void run() {
        knownNodesList.getKnownNodes().stream().forEach(n-> System.out.println(n.ip + ":" + n.port));
        HandshakeRequest hsr = config.generateHandShakeRequest(knownNodesList.getKnownNodes().iterator().next().asURL(), blockchain.getBestHeight());
        HandshakeResponse response = RESTUtils.post(knownNodesList.getKnownNodes().iterator().next(), "handshake", HandshakeResponse.class, hsr);

        if(response.error) {
            System.err.println(response.errorMessage);
            System.exit(1);
        }

        LinkedList<Host> nodeQueue = new LinkedList<>();
        nodeQueue.addAll(knownNodesList.getKnownNodes());

        Host me = new Host(config.outwardIP, config.port);

        while(!nodeQueue.isEmpty()) {
            Host next = nodeQueue.poll();

            if(me.equals(next) || !RESTUtils.exists(next)) {
                System.err.println("FAULTY NODE GIVEN, IGNORED " + next.ip + ":" + next.port + " " + me.ip + ":" + me.port);
                continue;
            }

            RESTUtils.post(next, "addr", AddrResponse.class, new AddrRequest(config.outwardIP, config.port));
            GetAddrResponse gres = RESTUtils.get(next, "addr", GetAddrResponse.class);

            gres.knownHosts.stream().forEach(h-> System.out.println(h.asURL()));

            for(Host h : gres.knownHosts) {
                if(!knownNodesList.getKnownNodes().contains(h) && !me.equals(h)) {
                    nodeQueue.add(h);
                    knownNodesList.addNode(h);
                }
            }
        }
    }
}
