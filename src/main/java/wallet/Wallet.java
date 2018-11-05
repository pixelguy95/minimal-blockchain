package wallet;

import apis.domain.Host;
import apis.domain.responses.GetOutputByAddressResponse;
import apis.domain.responses.GetOutputIDsByAddressResponse;
import apis.domain.responses.NewTransactionResponse;
import apis.utils.wrappers.TransactionRESTWrapper;
import apis.utils.wrappers.UTXORESTWrapper;
import domain.transaction.Transaction;
import io.nayuki.bitcoin.crypto.Ripemd160;
import org.apache.commons.cli.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.restlet.resource.ResourceException;
import security.ECKeyManager;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Wallet {


    public static void main(String args[]) {
        new Wallet(args);
    }

    public static final String DEFAULT_KEY_FILE = ".wallet.key";

    public KeyPair pair;
    public List<Host> knownHosts;

    public Host defaultNode1 = new Host("localhost:30109");
    public Host defaultNode2 = new Host("itchy.cs.umu.se:30109");

    public Wallet(String args[]) {

        knownHosts = new ArrayList<>(Arrays.asList(defaultNode1, defaultNode2));

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse( getCLIParseOptions(), args );

            if(line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar [JAR-FILE] [OPTIONS]", getCLIParseOptions() );
                System.exit(0);
            }

            if(line.hasOption("node")) {
                knownHosts.add(0, new Host(line.getOptionValue("node")));
            }

            if(line.hasOption("n")) {
                ECKeyManager.writePairToFile(ECKeyManager.generateNewKeyPair(), line.getOptionValue("n"));
            }

            if(line.hasOption("l")) {
                pair = ECKeyManager.loadPairFromFile(line.getOptionValue("l"));
            } else {
                File f = new File(DEFAULT_KEY_FILE);
                if(f.exists() && f.isFile()) {
                    pair = ECKeyManager.loadPairFromFile(DEFAULT_KEY_FILE);
                } else {
                    ECKeyManager.writePairToFile(ECKeyManager.generateNewKeyPair(), DEFAULT_KEY_FILE);
                    pair = ECKeyManager.loadPairFromFile(DEFAULT_KEY_FILE);
                }
            }

            printAddress();

            if(line.hasOption("f")) {

                for(Host h : knownHosts) {
                    try {
                        GetOutputByAddressResponse gobar = UTXORESTWrapper.getUTXOByPubKey(h, pair.getPublic());

                        long sum = gobar.outputs.stream().mapToLong(o->o.amount).sum();
                        System.out.println(addressString() + " " + sum);

                        break;
                    } catch (ResourceException e) {

                    }
                }
            }

            if(line.hasOption("s")) {

                for(Host h : knownHosts) {
                    try {
                        GetOutputIDsByAddressResponse goibar = UTXORESTWrapper.getUTXOIDsByPubKey(h, pair.getPublic());
                        Transaction t = Transaction.makeTransactionFromOutputs(h, pair, goibar.ids, line.getOptionValues("s")[0], Integer.valueOf(line.getOptionValues("s")[1]));

                        NewTransactionResponse response = TransactionRESTWrapper.sendTransaction(h, t);
                        System.out.println(response.errorMessage);
                        break;
                    } catch (ResourceException e) {

                    }
                }


                //GetOutputByAddressResponse gobar = UTXORESTWrapper.getUTXOByPubKey(h, pair.getPublic());
                //Arrays.stream(line.getOptionValues("s")).forEach(s-> System.out.println(s));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void printAddress() {
        System.out.println(addressString());
    }

    private String addressString() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                Ripemd160.getHash(
                        DigestUtils.sha256(
                                pair.getPublic().getEncoded())));
    }

    private Options getCLIParseOptions() {
        Options options = new Options();

        options.addOption( "h", "help", false, "prints this");

        options.addOption( "node", "node", true, "the node you want to connect to, " +
                "has a few default ones that can be used if not given.");

        options.addOption( "n", "new", true, "create a new key file, give path to the " +
                "file");

        options.addOption( "l", "load", true, "load key file, if not given " +
                DEFAULT_KEY_FILE + " will be used");

        options.addOption( "f", "funds", false, "prints the funds belonging to the given " +
                "key file");

        Option sendOption = new Option("s", "send", true, "use this to send funds to " +
                "address, --send [address] [fund amount]");
        sendOption.setArgs(2);
        options.addOption(sendOption);

        return options;
    }
}
