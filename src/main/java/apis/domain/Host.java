package apis.domain;

import apis.static_structures.KnownNodesList;

import java.io.Serializable;
import java.util.Objects;

public class Host implements Serializable {
    public String ip;
    public int port;

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public Host(String ipAndPort) {
        this.ip = ipAndPort.split(":")[0];
        this.port = Integer.parseInt(ipAndPort.split(":")[1]);
    }

    public String asURL() {
        return "http://"+ip+":"+port;
    }

    public boolean equals(Object o) {
        return ((Host)o).ip.equals(this.ip) && ((Host)o).port == this.port;
    }

    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
