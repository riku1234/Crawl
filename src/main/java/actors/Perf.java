package actors;

import akka.actor.UntypedActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by gsm on 9/26/15.
 */
public class Perf extends UntypedActor {
    int[] io_success; int[] io_failure;
    int[] io_success_prev; int[] io_failure_prev;
    int numMatchesComplete;

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            if(((String) message).startsWith("Setup")) {
                int num_tors = Integer.parseInt(((String) message).split("-")[1].trim());
                io_success = new int[num_tors];
                io_failure = new int[num_tors];

                io_success_prev = new int[num_tors];
                io_failure_prev = new int[num_tors];

                numMatchesComplete = 0;
                getContext().system().scheduler().scheduleOnce(
                        Duration.create(5000, TimeUnit.MILLISECONDS),
                        getSelf(), "Tick", getContext().dispatcher(), null);
            }
            else if(((String) message).startsWith("Success")) {
                int port_num = Integer.parseInt(((String) message).split("-")[1].trim());
                io_success[port_num - 9051]++;
            }
            else if(((String) message).startsWith("Failure")) {
                int port_num = Integer.parseInt(((String) message).split("-")[1].trim());
                io_failure[port_num - 9051]++;
            }
            else if(message.equals("MatchComplete")) {
                numMatchesComplete++;
            }
            else if(message.equals("Tick")) {
                System.out.println("Matches Complete = " + numMatchesComplete);
                System.out.println("IO Stats .... ");
                for(int i =0;i<io_success.length;i++) {
                    System.out.println("Port = " + (9051 + i) + " Success = " + io_success[i] + "(+" + (io_success[i] - io_success_prev[i]) + ")" + " Failure = " + io_failure[i] + "(+" + (io_failure[i] - io_failure_prev[i]) + ")");
                    io_success_prev[i] = io_success[i]; io_failure_prev[i] = io_failure[i];
                }
                getContext().system().scheduler().scheduleOnce(
                        Duration.create(20000, TimeUnit.MILLISECONDS),
                        getSelf(), "Tick", getContext().dispatcher(), null);
            }
        }
    }
}
