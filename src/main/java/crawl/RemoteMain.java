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

        for(int i=0;i<10;i++) {
            ActorRef child = actorSystem.actorOf(Props.create(IO.class).withDispatcher("RemoteIODispatcher"), "RemoteIO" + i);
            System.out.println("Remote Actor " + child.toString() + " created.");
            //remoteioroutees.add(new ActorRefRoutee(child));
        }
    }
}
