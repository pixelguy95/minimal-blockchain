package apis;

import apis.domain.Host;
import apis.domain.requests.AddrRequest;
import apis.domain.requests.HandshakeRequest;
import apis.domain.responses.AddrResponse;
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

    /**
     * TODO: Get known nodes from the key value db. Add connector to list, return list
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public static HandshakeResponse handShake(Request request, Response response) {
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
    public static String leave(Request request, Response response) {
        //KnownNodesList.removeNode();
        return "Good bye";
    }

    public static AddrResponse addr(Request request, Response response) {
        AddrRequest hsr = SpecialJSONSerializer.getInstance().fromJson(request.body(), AddrRequest.class);
        KnownNodesList.addNode(new Host(hsr.address, hsr.port));
        return new AddrResponse();
    }

    public static GetAddrResponse getAddresses(Request request, Response response) {
        return new GetAddrResponse(new ArrayList<>(KnownNodesList.getKnownNodes()));
    }
}
