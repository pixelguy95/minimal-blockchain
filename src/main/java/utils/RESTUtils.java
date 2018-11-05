package utils;

import apis.domain.Host;
import apis.domain.requests.Request;
import apis.domain.responses.Response;
import apis.static_structures.KnownNodesList;
import com.google.gson.Gson;
import node.SpecialJSONSerializer;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RESTUtils {

    public static <T extends Response> T get(String url, String endpoint, Class<T> returnType, List<String> args) {

        try {
            Client client = new Client(new Context(), Protocol.HTTP);
            client.getContext().getParameters().add ( "socketTimeout", "3000" );
            ClientResource c = new ClientResource(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
            c.setNext(client);
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.get().write(writer);
            c.release();
            return SpecialJSONSerializer.getInstance().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Response> T get(Host host, String endpoint, Class<T> returnType, List<String> args) {

        try {
            Client client = new Client(new Context(), Protocol.HTTP);
            client.getContext().getParameters().add ( "socketTimeout", "3000" );

            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
            c.setNext(client);
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.get().write(writer);
            c.release();
            return SpecialJSONSerializer.getInstance().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Response> T get(Host host, String endpoint, Class<T> returnType) {

        try {
            Client client = new Client(new Context(), Protocol.HTTP);
            client.getContext().getParameters().add ( "socketTimeout", "3000" );
            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint);
            c.setNext(client);
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.get().write(writer);
            c.release();
            return SpecialJSONSerializer.getInstance().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Response, R extends Request> T post(String url, String endpoint, Class<T> returnType, R body, List<String> args) {
        try {
            ClientResource c = new ClientResource(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.post(SpecialJSONSerializer.getInstance().toJson(body), MediaType.TEXT_ALL).write(writer);
            c.release();
            return SpecialJSONSerializer.getInstance().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Response, R extends Request> T post(Host host, String endpoint, Class<T> returnType, R body, List<String> args) {
        try {
            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.post(SpecialJSONSerializer.getInstance().toJson(body), MediaType.TEXT_ALL).write(writer);
            c.release();
            return SpecialJSONSerializer.getInstance().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Response, R extends Request> T post(Host host, String endpoint, Class<T> returnType, R body) {
        try {
            Client client = new Client(new Context(), Protocol.HTTP);
            client.getContext().getParameters().add ( "socketTimeout", "3000" );

            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint);
            c.setNext(client);
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.post(SpecialJSONSerializer.getInstance().toJson(body), MediaType.TEXT_ALL).write(writer);
            c.release();
            return SpecialJSONSerializer.getInstance().fromJson(writer.toString(), returnType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean exists(Host host) {
        try {
            Client client = new Client(new Context(), Protocol.HTTP);
            client.getContext().getParameters().add ( "socketTimeout", "3000" );

            ClientResource c = new ClientResource(host.asURL()+"/version");
            c.setNext(client);
            c.getLogger().setLevel(Level.OFF);
            Writer writer = new StringWriter();
            c.get().write(writer);
            c.release();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}
