package apis.domain.responses;

import java.util.List;

public class GetAllBlockHashesResponse extends Response {
    public List<String> hashes;

    public GetAllBlockHashesResponse(List<String> hashes) {
        this.hashes = hashes;
    }
}
