package actors;

import akka.actor.UntypedActor;
import command.Commands;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by gsm on 9/23/15.
 */
public class IO extends UntypedActor{

    private Document getDocument(String link) {
        try {
            Document document = Jsoup.connect(link).timeout(10000).get();
            //Info.perfActor.tell("Success", getSelf());
            return document;
        } catch(IOException e) {
            //Info.perfActor.tell("Failure", getSelf());
            return null;
        }
    }

    public void onReceive(Object message) throws Exception {
        ((Commands.Global) message).document = getDocument(((Commands.Global) message).commandLink);
        if(((Commands.Global) message).document == null)
            getSelf().tell(message, getSender());
        else
            Info.workerrouter.route(message, getSender());
    }
}
