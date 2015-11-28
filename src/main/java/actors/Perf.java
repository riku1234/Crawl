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
    private int[] num_child_messages;
    private int num_distributor_messages;
    private int[] num_tracker_messages;

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            if(message.equals("Distributor"))
                num_distributor_messages++;
            else if (((String) message).startsWith("Child"))
                num_child_messages[Integer.parseInt(((String) message).split("-")[1])]++;
            else if (((String) message).startsWith("Tracker"))
                num_tracker_messages[Integer.parseInt(((String) message).split("-")[1])]++;
            else if(((String) message).startsWith("Setup")) {
                String[] splits = ((String) message).split("-");

                int num_tors = Integer.parseInt(splits[1]);
                int num_child = Integer.parseInt(splits[2]);
                int num_trackers = Integer.parseInt(splits[3]);

                io_success = new int[num_tors];
                io_failure = new int[num_tors];

                io_success_prev = new int[num_tors];
                io_failure_prev = new int[num_tors];

                num_child_messages = new int[num_child];
                num_tracker_messages = new int[num_trackers];

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

                System.out.println("\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                if (sysConfObject != null)
                    System.out.println("Cores = " + (int) sysConfObject.get("CORES") + "IO Workers = " + (int) sysConfObject.get("NUM_IO_WORKERS") + " Child Workers = " + (int) sysConfObject.get("NUM_CHILD_WORKERS") + " Trackers = " + (int) sysConfObject.get("NUM_TRACKER_WORKERS") + " Tor Proxies = " + (int) sysConfObject.get("NUM_TOR_PROXIES"));
                System.out.println("Current Session Duration = " + ((System.currentTimeMillis() - start_time) / 1000) + "seconds");
                System.out.println("Session: ");
                System.out.println("Distributor - " + num_distributor_messages);
                num_distributor_messages = 0;
                for (int i = 0; i < num_child_messages.length; i++) {
                    System.out.println(" Child - " + i + " = " + num_child_messages[i]);
                    num_child_messages[i] = 0;
                }
                for (int i = 0; i < num_tracker_messages.length; i++) {
                    System.out.println(" Tracker - " + i + " = " + num_tracker_messages[i]);
                    num_tracker_messages[i] = 0;
                }

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
