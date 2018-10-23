package node;

import apis.domain.Host;
import apis.domain.requests.HandshakeRequest;
import apis.static_structures.Blockchain;
import apis.static_structures.KnownNodesList;
import db.DBSingletons;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Config {

    //TODO: load version number etc from file or something.
    // The data for the handshake, node specific.
    public final int version = 1;
    public final String subVersion = "minimal-blockchain-core0.0.1";
    public final List<String> services = Arrays.asList(HandshakeRequest.NODE_NETWORK);

    public int port = 30109;
    public String dbFolder = ".local-persistence";
    public boolean isInitial = false;

    public String outwardIP;
    public Host initialConnection;
    public boolean allowOrphanBlocks = false;

    public Config(String args[]) {
        URL url = null;
        try {
            url = new URL("http://checkip.amazonaws.com/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            outwardIP = br.readLine();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse( getCLIParseOptions(), args );

            if(line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar [JAR-FILE] [OPTIONS]", getCLIParseOptions() );
                System.exit(0);
            }

            if( line.hasOption( "i" ) ) {
                isInitial = true;
            }

            if(line.hasOption("f")) {
                dbFolder = line.getOptionValue("f");
            }

            if(line.hasOption("n")) {
                isInitial = false;
                initialConnection = new Host(line.getOptionValue("n").replace("localhost", outwardIP));
            }

            if(line.hasOption("p")) {
                port = Integer.parseInt(line.getOptionValue("p"));
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private Options getCLIParseOptions() {
        Options options = new Options();
        options.addOption( "i", "initialNode", false, "if this is the initialNode node in your " +
                "blockchain give this option\n\n" +
                "the node will be started with no other known nodes and only the genesis block in the chain.\n" +
                "be careful with this in the future this might purge all previous settings");

        options.addOption( "h", "help", false, "prints this");

        options.addOption( "p", "port", true, "port used by the node (default 30109)");

        options.addOption( "n", "node", true, "will add this node to the list of known " +
                "nodes, great for brand new nodes. if this is set the initialNode arg will be ignored");

        options.addOption( "f", "db-folder", true, "the folder used for persistence, " +
                "will default to .local-persistence");

        return options;
    }

    public HandshakeRequest generateHandShakeRequest(String youAddr, long bestHeight) {
        return new HandshakeRequest(version, services, System.currentTimeMillis(), youAddr, outwardIP, (short) port, subVersion, bestHeight);
    }

}
