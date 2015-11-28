package actors;

import akka.actor.UntypedActor;
import org.json.simple.JSONObject;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by gsm on 9/26/15.
 */
public class Perf extends UntypedActor {
    private final long start_time = System.currentTimeMillis();
    int[] io_success;
    int[] io_failure;
    int[] io_success_prev;
    int[] io_failure_prev;
    int numMatchesComplete;
    JSONObject sysConfObject = null;
    private int num_child_messages = 0;
    private int num_distributor_messages = 0;
    private int num_tracker_messages = 0;

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            if(message.equals("Distributor"))
                num_distributor_messages++;
            else if(message.equals("Child"))
                num_child_messages++;
            else if(message.equals("Tracker"))
                num_tracker_messages++;
            else if(((String) message).startsWith("Setup")) {
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

                if (sysConfObject != null)
                    System.out.println("Cores = " + (int) sysConfObject.get("CORES") + "IO Workers = " + (int) sysConfObject.get("NUM_IO_WORKERS") + " Child Workers = " + (int) sysConfObject.get("NUM_CHILD_WORKERS") + " Trackers = " + (int) sysConfObject.get("NUM_TRACKER_WORKERS") + " Tor Proxies = " + (int) sysConfObject.get("NUM_TOR_PROXIES"));

                System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                System.out.println("Current Session Duration = " + ((System.currentTimeMillis() - start_time) / 1000) + "seconds");
                System.out.println("Session: Distributor - " + num_distributor_messages + " Child - " + num_child_messages + " Tracker - " + num_tracker_messages);
                num_distributor_messages = 0; num_child_messages = 0; num_tracker_messages = 0;
                System.out.println("Matches Complete = " + numMatchesComplete);
                System.out.println("IO Stats .... ");
                long num_documents_success = 0; long num_documents_failure = 0;
                long num_documents_success_session = 0; long num_documents_failure_session = 0;
                for(int i =0;i<io_success.length;i++) {
                    num_documents_success += io_success[i];
                    num_documents_failure += io_failure[i];
                    num_documents_success_session += (io_success[i] - io_success_prev[i]);
                    num_documents_failure_session += (io_failure[i] - io_failure_prev[i]);
                    System.out.println("Port = " + (9051 + i) + " Success = " + io_success[i] + "(+" + (io_success[i] - io_success_prev[i]) + ")" + " Failure = " + io_failure[i] + "(+" + (io_failure[i] - io_failure_prev[i]) + ")");
                    io_success_prev[i] = io_success[i]; io_failure_prev[i] = io_failure[i];
                }
                System.out.println("Total: Success = " + num_documents_success + " Failure = " + num_documents_failure);
                System.out.println("Session: Success = " + num_documents_success_session + " Failure = " + num_documents_failure_session);
                if(numMatchesComplete != 0)
                    System.out.println("Average time of Completion of a match = " + ((System.currentTimeMillis() - start_time) / numMatchesComplete) + "ms.");
                System.out.println("###############################################################################");
                getContext().system().scheduler().scheduleOnce(
                        Duration.create(20000, TimeUnit.MILLISECONDS),
                        getSelf(), "Tick", getContext().dispatcher(), null);
            }
        } else if (message instanceof JSONObject) {
            this.sysConfObject = (JSONObject) message;
        }
    }
}
