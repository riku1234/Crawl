import actors.Distributor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import crawl.Crawl;
import defs.commands.StartCommand;
import fourfourtwo.Persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/26/16.
 */
public class Main {
    public static void main(String args[]) throws IOException, ParseException {

        /*
        Format:
            1. Games{Save games to file} results_page{List of results pages} prepend{prepend for saving games}
            2. Data{Collect Data} prefixes{where files are saved from 1, in prefix,num_matches format}
                num_trackers num_child num_io num_tor
         */

        if (args[0].equals("Games")) {
            String results_page_file = String.valueOf(args[1]);
            String prepend = String.valueOf(args[2]);
            if (!prepend.endsWith("/")) {
                prepend = prepend + "/";
            }

            BufferedReader br = new BufferedReader(new FileReader(results_page_file));
            ArrayList<String> FFTResultsPage = new ArrayList<>();
            String result_page;
            while ((result_page = br.readLine()) != null) {
                FFTResultsPage.add(result_page);
            }

            new Crawl().addGameDetails(FFTResultsPage, prepend);
        } else if (args[0].equals("Data")) {

            String prefix_page = String.valueOf(args[1]);
            BufferedReader br = new BufferedReader(new FileReader(prefix_page));
            String[] lines = (String[]) br.lines().toArray();

            String[] prefixes = new String[lines.length];
            int[] num_matches = new int[lines.length];

            for (int i = 0; i < lines.length; i++) {
                prefixes[i] = lines[i].split(",")[0];
                num_matches[i] = Integer.parseInt(lines[i].split(",")[1]);
            }

            int num_trackers = Integer.parseInt(args[2]);
            int num_child = Integer.parseInt(args[3]);
            int num_io = Integer.parseInt(args[4]);
            int num_tor = Integer.parseInt(args[5]);

            Persistence.createTables();
            final ActorSystem actorSystem = ActorSystem.create("Actor-System");
            final ActorRef distributor = actorSystem.actorOf(Props.create(Distributor.class).withDispatcher("DistributorDispatcher"), "Distributor");
            distributor.tell(new StartCommand(prefixes, num_matches, num_trackers, num_child, num_io, num_tor), null);
        }
    }
}
