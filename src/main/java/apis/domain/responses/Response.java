package apis.domain.responses;

public abstract class Response {
    public boolean error = false;
    public String errorMessage = "";

    public Response setError(String error) {
        this.error = true;
        this.errorMessage = error;

        return this;
    }
}
