package node.tasks;

import apis.domain.Host;
import apis.domain.responses.GetAddrResponse;
import apis.static_structures.KnownNodesList;
import utils.RESTUtils;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Every 5 seconds check known nodes list and make sure that they are still upp
 * Check the other nodes by fetching their node lists and checking if theirs match
 * Try to keep the connected nodes to about 8 or so
 *
 * Remove nodes that are no longer responding.
 */
public class NetworkMaintainer extends AbstractTask{

    public NetworkMaintainer(AtomicBoolean keepAlive) {
        super(keepAlive);
    }

    @Override
    public void run() {

        while (keepAlive.get()) {
            sleepWrapper(5000);

            HashSet<Host> deadNodes = new HashSet<>();
            HashSet<Host> knownNodes = KnownNodesList.getKnownNodes();
            knownNodes.stream().forEach(host->{

                if(!RESTUtils.exists(host)) {
                    deadNodes.add(host);
                    return;
                }

                List<Host> nodesConnectedToThisNode = RESTUtils.get(host, "getaddr", GetAddrResponse.class).knownHosts;

                nodesConnectedToThisNode.stream().forEach(ncttn->{
                    if(!knownNodes.contains(ncttn)) {
                        KnownNodesList.addNode(ncttn);
                    }
                });

            });

            KnownNodesList.removeAllNodes(deadNodes);
        }
    }
}
