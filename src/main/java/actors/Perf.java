package actors;

import akka.actor.UntypedActor;

/**
 * Created by gsm on 9/26/15.
 */
public class Perf extends UntypedActor {
    public static int success_count = 0; public static int failure_count = 0;
    public void onReceive(Object message) throws Exception {
        String msg = (String) message;
        if(msg.equals("Success"))
            success_count++;
        else if(msg.equals("Failure"))
            failure_count++;
    }
}
