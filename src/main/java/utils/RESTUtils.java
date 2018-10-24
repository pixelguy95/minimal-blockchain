package utils;

import apis.domain.Host;
import apis.domain.requests.Request;
import apis.domain.responses.Response;
import apis.static_structures.KnownNodesList;
import com.google.gson.Gson;
import node.SpecialJSONSerializer;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RESTUtils {

    public static <T extends Response> T get(String url, String endpoint, Class<T> returnType, List<String> args) {

        System.out.println(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
        try {
            ClientResource c = new ClientResource(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
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

        System.out.println(host.asURL() + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
        try {
            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
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

        System.out.println(host.asURL() + "/" + endpoint);
        try {
            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint);
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
        System.out.println(url + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
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
        System.out.println(host.asURL() + "/" + endpoint + args.stream().map(e->"/".concat(e)).collect(Collectors.joining()));
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
        System.out.println(host.asURL() + "/" + endpoint);
        try {
            ClientResource c = new ClientResource(host.asURL() + "/" + endpoint);
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
            ClientResource c = new ClientResource(host.asURL()+"/version");
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
