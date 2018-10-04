package cores;

import apis.domain.HandshakeResponse;
import com.google.gson.Gson;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class NodeResponseTest {

    public static void main(String args[]) throws IOException {

        ClientResource c = new ClientResource("http://static.cs.umu.se:30109" + "/handshake/30109");
        Writer writer = new StringWriter();
        c.get().write(writer);
        new Gson().fromJson(writer.toString(), HandshakeResponse.class).knownHosts.stream().forEach(i-> System.out.println(i));
    }
}
