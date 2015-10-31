package actors;

import akka.actor.UntypedActor;
import command.Commands;
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

    private Document getDocument(String link) {

        try {
            Document document = Jsoup.connect(link).timeout(10000).get();
            //Info.perfActor.tell("Success", getSelf());
            Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside IO getDocument. Success. Size = " + document.toString().length()+ "\n");
            if(document.toString().length() < 150000) {
                System.out.println("FLAG -------- Size less. " + document.toString().length());
            }
            if(document.toString().length() < 100000) {
                System.out.println("Size less than 100000. Re-loading. Size = " + document.toString().length());
                throw new IOException();
            }
            return document;
        } catch(IOException e) {
            //Info.perfActor.tell("Failure", getSelf());
            return null;
        }
    }

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            System.out.println("Actor " + this.toString() + " receiving message " + message);
        }
        else if(message instanceof Commands.RemoteSetup) {
            Info.workerrouter = ((Commands.RemoteSetup) message).workerRouter;
            System.out.println("Worker Router setup in Remote System");
        }
        else {
            ((Commands.Global) message).document = getDocument(((Commands.Global) message).commandLink);
            if (((Commands.Global) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
    }
}
