package apis;

import apis.domain.HandshakeResponse;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;

public class HandshakeAPI {

    /**
     * TODO: Get known nodes from the key value db. Add connector to list, return list
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public static HandshakeResponse newConnection(Request request, Response response) {
        return new HandshakeResponse(Arrays.asList(request.ip()));
    }

    /**
     * TODO: Remove the requester from the known nodes db
     * @param request
     * @param response
     * @return
     */
    public static String leave(Request request, Response response) {

        return "Good bye";
    }
}
