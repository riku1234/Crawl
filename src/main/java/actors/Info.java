package actors;

import akka.actor.ActorRef;
import akka.routing.Router;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gsm on 9/12/15.
 */
public class Info {
    public static String prepend = "2012_1825/";
    public static int numFiles = 1825;
    //public static volatile Document playerDocument = null;
    public static volatile int numMessages;
    public static String season=""; public static String FFT_match_id = ""; public static Date match_date;

    public static Router workerrouter;
    public static Router iorouter;
    public static ActorRef perfActor;
}
