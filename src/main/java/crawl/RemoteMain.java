package crawl;

import actors.IO;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Created by gsm on 10/2/15.
 */
public class RemoteMain {
    public static void main(String[] args) {
        final ActorSystem actorSystem = ActorSystem.create("Remote-Actor-System");
        //List<Routee> remoteioroutees = new ArrayList<Routee>();
        for(int i=0;i<10;i++) {
            ActorRef child = actorSystem.actorOf(Props.create(IO.class).withDispatcher("RemoteIODispatcher"), "RemoteIO" + i);
            //remoteioroutees.add(new ActorRefRoutee(child));
        }
        //Info.workerrouter = new Router(new SmallestMailboxRoutingLogic(), workerroutees);
    }
}
