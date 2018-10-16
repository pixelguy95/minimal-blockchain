package apis.domain;

import java.util.List;

public class HandshakeRequest extends Request {

    public static final String NODE_NETWORK = "NODE_NETWORK";

    public long version;
    public List<String> services;
    public long time;
    public String youAddr;
    public String myAddr;
    public int myPort;
    public String subVersion;
    public long bestHeight;

    public HandshakeRequest(long version, List<String> services, long time, String youAddr, String myAddr, int myPort, String subVersion, long bestHeight) {
        this.version = version;
        this.services = services;
        this.time = time;
        this.youAddr = youAddr;
        this.myAddr = myAddr;
        this.myPort = myPort;
        this.subVersion = subVersion;
        this.bestHeight = bestHeight;
    }
}
