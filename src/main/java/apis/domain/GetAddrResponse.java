package apis.domain;

import apis.static_structures.KnownNodesList;

import java.util.List;

public class GetAddrResponse extends Response{
    List<KnownNodesList.Host> knownHosts;

    public GetAddrResponse(List<KnownNodesList.Host> knownHosts) {
        this.knownHosts = knownHosts;
    }
}
