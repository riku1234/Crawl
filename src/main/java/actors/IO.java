package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import defs.Global;
import defs.MatchGlobals;
import defs.PlayerDetails;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by gsm on 9/23/15.
 */
public class IO extends UntypedActor{
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private int torIndex = -1;
    private int socksPort = -1;

    private String getHTTPResponse(String url) {

        try {
            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode != 200)
                return null;

            return String.valueOf(response);
        } catch (Exception e) {
            return null;
        }
    }

    private Document getDocument(String link) {

        try {

            String buffer = this.getHTTPResponse(link);

            if (buffer == null)
                throw new IOException();

            Document document = Jsoup.parse(buffer, link);

            if(document.toString().length() < 150000) {
                System.out.println("FLAG -------- Size less. " + document.toString().length());
            }
            if(document.toString().length() < 100000) {
                System.out.println("Size less than 100000. Re-loading. Size = " + document.toString().length());
                throw new IOException();
            }
            //log.info("Document returned from Proxy Port = " + this.socksPort);
            if(Distributor.perfActor != null)
                Distributor.perfActor.tell("Success-" + socksPort, getSelf());

            return document;
        } catch(IOException e) {
            //log.error("Document Exception ... ");
            if(Distributor.perfActor != null)
                Distributor.perfActor.tell("Failure-" + socksPort, getSelf());
            return null;
        }
    }

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            System.out.println("Actor " + this.toString() + " receiving message " + message);
            this.torIndex = Integer.parseInt(((String) message).split("-")[1]);
            this.socksPort = 9051 + this.torIndex;
        } else if (message instanceof MatchGlobals) {
            //log.info("Match Globals request received on Port = " + this.socksPort);
            //log.info("GameLink = " + ((MatchGlobals) message).getGameLink());
            if (((MatchGlobals) message).gameDocument == null) {
                Document gameDocument = getDocument(((MatchGlobals) message).gameLink);
                if (gameDocument == null) {
                    //getSelf().tell(message, getSender());
                    Distributor.ioRouter.route(message, getSender());
                    return;
                }
                else {
                    ((MatchGlobals) message).gameDocument = gameDocument;
                }
            }
            Document playersDocument = getDocument(((MatchGlobals) message).gameLink + "/player-stats#tabs-wrapper-anchor");
            if(playersDocument == null) {
                //getSelf().tell(message, getSender());
                Distributor.ioRouter.route(message, getSender());
            }
            else {
                ((MatchGlobals) message).playersDocument = playersDocument;
                getContext().parent().tell(message, getSender());
            }
        } else if (message instanceof PlayerDetails) {
            //log.info("Player Details request received on Port = " + this.socksPort);
            //log.info("Player Link = " + ((PlayerDetails) message).playerLink);
            Document playerDocument = getDocument(((PlayerDetails) message).playerLink);
            if(playerDocument == null) {
                //getSelf().tell(message, getSender());
                Distributor.ioRouter.route(message, getSender());
            }
            else {
                ((PlayerDetails) message).playerDocument = playerDocument;
                getContext().parent().tell(message, getSender());
            }
        }
        else {
            //log.info("Command Link request received on Port = " + this.socksPort);
            ((Global) message).document = getDocument(((Global) message).commandLink);
            if (((Global) message).document == null) {
                //getSelf().tell(message, getSender());
                Distributor.ioRouter.route(message, getSender());
            }
            else {
                Distributor.childRouter.route(message, getSender());
            }
        }
    }
}
