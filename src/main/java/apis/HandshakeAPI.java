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

    public static HandshakeResponse newConnection(Request request, Response response) throws IOException {

        URL url = new URL("http://checkip.amazonaws.com/");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        return new HandshakeResponse(Arrays.asList(br.readLine()));
    }
}
