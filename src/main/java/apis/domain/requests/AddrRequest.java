package apis.domain.requests;

public class AddrRequest extends Request{
    public String address;
    public int port;

    public AddrRequest(String address, int port) {
        this.address = address;
        this.port = port;
    }
}
