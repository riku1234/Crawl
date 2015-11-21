package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import command.Commands;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by gsm on 9/23/15.
 */
public class IO extends UntypedActor{
    private int torIndex = -1;
    private int socksPort = -1;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private Document getDocument(String link) {

        try {
            URL url = new URL(link);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", this.socksPort));
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            httpURLConnection.setReadTimeout(10000); httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.connect();

            String line = null;
            StringBuffer tmp = new StringBuffer();
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            while ((line = in.readLine()) != null) {
                tmp.append(line);
            }

            Document document = Jsoup.parse(String.valueOf(tmp), link);

            //Document document = Jsoup.connect(link).timeout(10000).get();
            //Info.perfActor.tell("Success", getSelf());
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside IO getDocument. Success. Size = " + document.toString().length()+ "\n");
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
        }
        else if(message instanceof Commands.MatchGlobals) {
            //log.info("Match Globals request received on Port = " + this.socksPort);
            //log.info("GameLink = " + ((Commands.MatchGlobals) message).getGameLink());
            if(((Commands.MatchGlobals) message).getGameDocument() == null) {
                Document gameDocument = getDocument(((Commands.MatchGlobals) message).getGameLink());
                if (gameDocument == null) {
                    //getSelf().tell(message, getSender());
                    Distributor.ioRouter.route(message, getSender());
                    return;
                }
                else {
                    ((Commands.MatchGlobals) message).setGameDocument(gameDocument);
                }
            }
            Document playersDocument = getDocument(((Commands.MatchGlobals) message).getGameLink() + "/player-stats#tabs-wrapper-anchor");
            if(playersDocument == null) {
                //getSelf().tell(message, getSender());
                Distributor.ioRouter.route(message, getSender());
            }
            else {
                ((Commands.MatchGlobals) message).setPlayersDocument(playersDocument);
                getContext().parent().tell(message, getSender());
            }
        }
        else if(message instanceof Commands.PlayerDetails) {
            //log.info("Player Details request received on Port = " + this.socksPort);
            //log.info("Player Link = " + ((Commands.PlayerDetails) message).playerLink);
            Document playerDocument = getDocument(((Commands.PlayerDetails) message).playerLink);
            if(playerDocument == null) {
                //getSelf().tell(message, getSender());
                Distributor.ioRouter.route(message, getSender());
            }
            else {
                ((Commands.PlayerDetails) message).playerDocument = playerDocument;
                getContext().parent().tell(message, getSender());
            }
        }
        else {
            //log.info("Command Link request received on Port = " + this.socksPort);
            ((Commands.Global) message).document = getDocument(((Commands.Global) message).commandLink);
            if (((Commands.Global) message).document == null) {
                //getSelf().tell(message, getSender());
                Distributor.ioRouter.route(message, getSender());
            }
            else {
                Distributor.childRouter.route(message, getSender());
            }
        }
    }
}
