package apis.domain.responses;

import apis.domain.Host;
import apis.static_structures.KnownNodesList;

import java.util.List;

public class GetAddrResponse extends Response{
    public List<Host> knownHosts;

    public GetAddrResponse(List<Host> knownHosts) {
        this.knownHosts = knownHosts;
    }
}
