package node;

import node.domain.KnownNodesList;
import org.apache.commons.cli.*;

public class Config {

    public static int port = 30109;
    public static String dbFolder = ".local-persistence";
    public static boolean isInitial = false;

    public static void parse(String[] args) {

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

            if(line.hasOption("n")) {
                isInitial = false;
                KnownNodesList.addNode(new KnownNodesList.Host(line.getOptionValue("n")));
            }

            if(line.hasOption("p")) {
                port = Integer.parseInt(line.getOptionValue("p"));
            }

            if(line.hasOption("f")) {
                dbFolder = line.getOptionValue("f");
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static Options getCLIParseOptions() {
        Options options = new Options();
        options.addOption( "i", "initial", false, "if this is the initial node in your " +
                "blockchain give this option\n\n" +
                "the node will be started with no other known nodes and only the genesis block in the chain.\n" +
                "be careful with this in the future this might purge all previous settings");

        options.addOption( "h", "help", false, "prints this");

        options.addOption( "p", "port", true, "port used by the node (default 30109)");

        options.addOption( "n", "node", true, "will add this node to the list of known " +
                "nodes, great for brand new nodes. if this is set the initial arg will be ignored");

        options.addOption( "f", "db-folder", true, "the folder used for persistence, " +
                "will default to .local-persistence");

        return options;
    }
}
