package apis;

import apis.domain.Host;
import apis.domain.requests.AddrRequest;
import apis.domain.requests.HandshakeRequest;
import apis.domain.responses.AddrResponse;
import apis.domain.responses.BooleanResponse;
import apis.domain.responses.GetAddrResponse;
import apis.domain.responses.HandshakeResponse;
import apis.static_structures.KnownNodesList;
import com.google.gson.Gson;
import node.SpecialJSONSerializer;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;

public class HandshakeAPI {

    private KnownNodesList knownNodesList;

    public HandshakeAPI(KnownNodesList knownNodesList) {
        this.knownNodesList = knownNodesList;
    }

    /**
     * TODO: Get known nodes from the key value db. Add connector to list, return list
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public HandshakeResponse handShake(Request request, Response response) {
        HandshakeRequest hsr = SpecialJSONSerializer.getInstance().fromJson(request.body(), HandshakeRequest.class);

        //TODO: verify version number etc...
        return new HandshakeResponse();
    }

    /**
     * TODO: Remove the requester from the known nodes db
     * @param request
     * @param response
     * @return
     */
    public BooleanResponse leave(Request request, Response response) {
        knownNodesList.removeNode(new Host(request.ip(), Integer.valueOf(request.params("port"))));
        return new BooleanResponse();
    }

    public AddrResponse addr(Request request, Response response) {
        AddrRequest hsr = SpecialJSONSerializer.getInstance().fromJson(request.body(), AddrRequest.class);
        knownNodesList.addNode(new Host(hsr.address, hsr.port));
        return new AddrResponse();
    }

    public GetAddrResponse getAddresses(Request request, Response response) {
        return new GetAddrResponse(new ArrayList<>(knownNodesList.getKnownNodes()));
    }
}
