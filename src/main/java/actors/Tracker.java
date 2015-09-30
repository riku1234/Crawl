package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.AskTimeoutException;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import command.Commands;

//import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import crawl.Crawl;
import fourfourtwo.Persistence;
import scala.concurrent.duration.Duration;
import scala.concurrent.Future;
import static akka.pattern.Patterns.gracefulStop;
import scala.concurrent.Await;

/**
 * Created by gsm on 9/12/15.
 */
public class Tracker extends UntypedActor {
    int currentIndex = 0;
    private ActorRef parent = null;
    Commands commands = new Commands();
    Crawl crawl = new Crawl();
    static long startTime = -1;

    public void onReceive(Object message) throws Exception {
        if(message instanceof Commands.StartCommand) {
            System.out.println("Tracker Started ... Dispatcher = " + getContext().dispatcher().toString());

            List<Routee> workerroutees = new ArrayList<Routee>();
            for(int i=0;i<10;i++) {
                ActorRef child = getContext().actorOf(Props.create(Child.class).withDispatcher("ChildDispatcher"), "Child" + i);
                getContext().watch(child);
                workerroutees.add(new ActorRefRoutee(child));
            }
            Info.workerrouter = new Router(new SmallestMailboxRoutingLogic(), workerroutees);
            List<Routee> ioroutees = new ArrayList<>();
            for(int i=0;i<10;i++) {
                ActorRef iochild = getContext().actorOf(Props.create(IO.class).withDispatcher("IODispatcher"), "IO" + i);
                getContext().watch(iochild);
                ioroutees.add(new ActorRefRoutee(iochild));
            }
            Info.iorouter = new Router(new SmallestMailboxRoutingLogic(), ioroutees);
            //Info.perfActor = getContext().actorOf(Props.create(Perf.class).withDispatcher("PerfDispatcher"), "Perf");
            currentIndex = ((Commands.StartCommand)message).j;
            startTime = System.currentTimeMillis();
            //Info.iorouter.route(message, getSelf());
            getParent().tell(message, getSelf());
        }
        else if(message instanceof Commands.SkipGameCommand) {
            System.out.println("GameLink " + currentIndex + " Skipped.");
            restart();
        }
        else if(message instanceof Commands.ShotsCommand) {
            if (!Persistence.addShots(((Commands.ShotsCommand) message).shots, Info.FFT_match_id, ((Commands.ShotsCommand) message).playerDetails.team_name, ((Commands.ShotsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add shots Failed in Persistence.");
            Info.numMessages--;
            ((Commands.ShotsCommand) message).playerDetails.numShotsComplete++;
            if(((Commands.ShotsCommand) message).playerDetails.numShotsComplete == 4) {
                Info.iorouter.route(commands.new ShotsCommand(((Commands.ShotsCommand) message).playerDetails, 5), getSelf());
            }
            else if(((Commands.ShotsCommand) message).playerDetails.numShotsComplete == 5) {
                Info.iorouter.route(commands.new PenaltiesCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
                Info.iorouter.route(commands.new FreekickshotsCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
            }
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.PenaltiesCommand) {
            if (!Persistence.addPenalties(((Commands.PenaltiesCommand) message).penalties, Info.FFT_match_id, ((Commands.PenaltiesCommand) message).playerDetails.team_name, ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add penalties Failed in Persistence.");
            Info.numMessages--;

            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.FreekickshotsCommand) {
            if (!Persistence.addFKShots(((Commands.FreekickshotsCommand) message).freekickshots, Info.FFT_match_id, ((Commands.FreekickshotsCommand) message).playerDetails.team_name, ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add FKshots Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.PassesCommand) {
            ((Commands.PassesCommand) message).playerDetails.numPassesComplete++;
            if(((Commands.PassesCommand) message).playerDetails.numPassesComplete == 3) {
                Info.iorouter.route(commands.new PassesCommand(((Commands.PassesCommand) message).playerDetails, 4), getSelf());
            }
            if(!Persistence.addPasses(((Commands.PassesCommand) message).passes, Info.FFT_match_id, ((Commands.PassesCommand) message).playerDetails.team_name, ((Commands.PassesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add passes Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.AssistsCommand) {
            if(!Persistence.addAssists(((Commands.AssistsCommand) message).assists, Info.FFT_match_id, ((Commands.AssistsCommand) message).playerDetails.team_name, ((Commands.AssistsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add assists Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.ReceivedPassesCommand) {
            if(!Persistence.addReceivedPasses(((Commands.ReceivedPassesCommand) message).receivedpasses, Info.FFT_match_id, ((Commands.ReceivedPassesCommand) message).playerDetails.team_name, ((Commands.ReceivedPassesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add received passes Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.ChancesCreatedCommand) {
            if (!Persistence.addChancesCreated(((Commands.ChancesCreatedCommand) message).chancescreated, Info.FFT_match_id, ((Commands.ChancesCreatedCommand) message).playerDetails.team_name, ((Commands.ChancesCreatedCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add chances created Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.LongPassesCommand) {
            if (!Persistence.addLongPasses(((Commands.LongPassesCommand) message).longpasses, Info.FFT_match_id, ((Commands.LongPassesCommand) message).playerDetails.team_name, ((Commands.LongPassesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add long passes Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.ShortPassesCommand) {
            if (!Persistence.addShortPasses(((Commands.ShortPassesCommand) message).shortpasses, Info.FFT_match_id, ((Commands.ShortPassesCommand) message).playerDetails.team_name, ((Commands.ShortPassesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add short passes Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.CrossesCommand) {
            if (!Persistence.addCrosses(((Commands.CrossesCommand) message).crosses, Info.FFT_match_id, ((Commands.CrossesCommand) message).playerDetails.team_name, ((Commands.CrossesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add crosses Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.TakeOnsCommand) {
            if (!Persistence.addTakeOns(((Commands.TakeOnsCommand) message).takeons, Info.FFT_match_id, ((Commands.TakeOnsCommand) message).playerDetails.team_name, ((Commands.TakeOnsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add takeons Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.CornersCommand) {
            if (!Persistence.addCorners(((Commands.CornersCommand) message).corners, Info.FFT_match_id, ((Commands.CornersCommand) message).playerDetails.team_name, ((Commands.CornersCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add corners Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.OffsidePassesCommand) {
            if (!Persistence.addOffsidePasses(((Commands.OffsidePassesCommand) message).offsidepasses, Info.FFT_match_id, ((Commands.OffsidePassesCommand) message).playerDetails.team_name, ((Commands.OffsidePassesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add offside passes Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.BallRecoveriesCommand) {
            if (!Persistence.addBallRecoveries(((Commands.BallRecoveriesCommand) message).ballrecoveries, Info.FFT_match_id, ((Commands.BallRecoveriesCommand) message).playerDetails.team_name, ((Commands.BallRecoveriesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add ball recoveries Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.TacklesCommand) {
            if (!Persistence.addTackles(((Commands.TacklesCommand) message).tackles, Info.FFT_match_id, ((Commands.TacklesCommand) message).playerDetails.team_name, ((Commands.TacklesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add tackles Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.InterceptionsCommand) {
            if (!Persistence.addInterceptions(((Commands.InterceptionsCommand) message).interceptions, Info.FFT_match_id, ((Commands.InterceptionsCommand) message).playerDetails.team_name, ((Commands.InterceptionsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add interceptions Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.BlocksCommand) {
            if (!Persistence.addBlocks(((Commands.BlocksCommand) message).blocks, Info.FFT_match_id, ((Commands.BlocksCommand) message).playerDetails.team_name, ((Commands.BlocksCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add blocks Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.ClearancesCommand) {
            if (!Persistence.addClearances(((Commands.ClearancesCommand) message).clearances, Info.FFT_match_id, ((Commands.ClearancesCommand) message).playerDetails.team_name, ((Commands.ClearancesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add clearances Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.AerialDuelsCommand) {
            if (!Persistence.addAerialDuels(((Commands.AerialDuelsCommand) message).aerialduels, Info.FFT_match_id, ((Commands.AerialDuelsCommand) message).playerDetails.team_name, ((Commands.AerialDuelsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add aerial duels Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.BlockedCrossesCommand) {
            if (!Persistence.addBlockedCrosses(((Commands.BlockedCrossesCommand) message).blockedcrosses, Info.FFT_match_id, ((Commands.BlockedCrossesCommand) message).playerDetails.team_name, ((Commands.BlockedCrossesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add blocked crosses Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.DefensiveErrorsCommand) {
            if (!Persistence.addDefensiveErrors(((Commands.DefensiveErrorsCommand) message).defensiveerrors, Info.FFT_match_id, ((Commands.DefensiveErrorsCommand) message).playerDetails.team_name, ((Commands.DefensiveErrorsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add defensive errors Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.FoulsCommand) {
            if (!Persistence.addFouls(((Commands.FoulsCommand) message).fouls, Info.FFT_match_id, ((Commands.FoulsCommand) message).playerDetails.team_name, ((Commands.FoulsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add fouls Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else {
            crawl.cleanTerminate("Strange Error - 100");
        }
    }

    private void restart() {
        Info.numMessages = 0;
        //System.out.println("Perf: Success = " + Perf.success_count + " Failure = " + Perf.failure_count);
        //Perf.success_count = 0; Perf.failure_count = 0;
        if(currentIndex == Info.numFiles - 1) {
            System.out.println("All the Details have been saved. Exiting Actor System.");
            //getParent().tell(Commands.getCommandsInstance().new QuitCommand(), getSelf());
            try {
                Future<Boolean> stopped = gracefulStop(getSelf(), Duration.create(60, TimeUnit.SECONDS));
                Await.result(stopped, Duration.create(60, TimeUnit.SECONDS));
                System.exit(0);
            } catch (Exception e) {
                System.out.println("Actors could not be killed within the timeout. Exiting.");
                System.exit(0);
            }
        }
        else {
            System.out.println("Game: " + Info.FFT_match_id + " details saved.");

            if (!Info.FFT_match_id.equals("") && !Persistence.gameSaved(Info.FFT_match_id))
                crawl.cleanTerminate("Game Could not be Saved.");
            Info.FFT_match_id = "";
            Info.match_date = null;
            currentIndex++;
            long curTime = System.currentTimeMillis();
            System.out.println("Time Taken = " + (curTime - startTime));
            startTime = curTime;
            //Info.iorouter.route(new Commands().new StartCommand(currentIndex), getSelf());
            getParent().tell(new Commands().new StartCommand(currentIndex), getSelf());
        }
    }

    private ActorRef getParent() {
        if(parent == null)
            parent = getContext().actorOf(Props.create(Parent.class).withDispatcher("ParentDispatcher"), "Parent");
        return parent;
    }
}
