package apis.domain;

import apis.static_structures.KnownNodesList;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Host implements Serializable {
    public String ip;
    public int port;

    public Host(String ip, int port) {
        InetAddress ipaddress = null;
        try {
            ipaddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.ip = ipaddress.getHostAddress();
        this.port = port;
    }

    public Host(String ipAndPort) {
        InetAddress ipaddress = null;
        try {
            ipaddress = InetAddress.getByName(ipAndPort.split(":")[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.ip = ipaddress.getHostAddress();
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
