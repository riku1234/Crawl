package actors;

import akka.actor.*;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import akka.util.Timeout;
import com.sun.java.swing.plaf.windows.TMSchema;
import command.Commands;
import crawl.Crawl;
import fourfourtwo.Persistence;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.gracefulStop;

//import java.util.concurrent.Future;

/**
 * Created by gsm on 9/12/15.
 */
public class Tracker extends UntypedActor {
    private final Commands commands = new Commands();
    private final Crawl crawl = new Crawl();
    private long startTime = -1;

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            if(message.equals("Setup")) {
                getSender().tell("NextMatch", getSelf());
            }
        }
        else if(message instanceof Commands.ShotsCommand) {
            //System.out.println("Adding Shots for Match=" + Info.FFT_match_id + " Team = " + ((Commands.ShotsCommand) message).playerDetails.team_name + " Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Shots = " + ((Commands.ShotsCommand) message).shots.size());
            if (!Persistence.addShots(((Commands.ShotsCommand) message).shots, ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ShotsCommand) message).playerDetails.team_name, ((Commands.ShotsCommand) message).playerDetails.FFT_player_id, ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getSeason())) {
                System.out.println("Add shots Failed in Persistence.");
                System.exit(1);
            }
            ((Commands.ShotsCommand) message).playerDetails.matchGlobals.setNumMessagesRemaining(((Commands.ShotsCommand) message).playerDetails.matchGlobals.getNumMessagesRemaining() - 1);
            ((Commands.ShotsCommand) message).playerDetails.numShotsComplete++;
            if(((Commands.ShotsCommand) message).playerDetails.numShotsComplete == 4) {
                Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "4th Shot Command Received for Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Starting 5th.\n");
                Info.iorouter.route(commands.new ShotsCommand(((Commands.ShotsCommand) message).playerDetails, 5), getSelf());
            }
            else if(((Commands.ShotsCommand) message).playerDetails.numShotsComplete == 5) {
                Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "5th Shot Command Received for Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Starting Others.\n");
                Info.iorouter.route(commands.new PenaltiesCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
                Info.iorouter.route(commands.new FreekickshotsCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
            }
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.PenaltiesCommand) {
            //System.out.println("Adding Penalties for Match=" + Info.FFT_match_id + " Team = " + ((Commands.PenaltiesCommand) message).playerDetails.team_name + " Player = " + ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id + " Penalties = " + ((Commands.PenaltiesCommand) message).penalties.size());
            if (!Persistence.addPenalties(((Commands.PenaltiesCommand) message).penalties, Info.FFT_match_id, ((Commands.PenaltiesCommand) message).playerDetails.team_name, ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add penalties Failed in Persistence.");
            Info.numMessages--;

            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.FreekickshotsCommand) {
            //System.out.println("Adding FKShots for Match=" + Info.FFT_match_id + " Team = " + ((Commands.FreekickshotsCommand) message).playerDetails.team_name + " Player = " + ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id + " FKShots = " + ((Commands.FreekickshotsCommand) message).freekickshots.size());
            if (!Persistence.addFKShots(((Commands.FreekickshotsCommand) message).freekickshots, Info.FFT_match_id, ((Commands.FreekickshotsCommand) message).playerDetails.team_name, ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id, Info.season))
                crawl.cleanTerminate("Add FKshots Failed in Persistence.");
            Info.numMessages--;
            if (Info.numMessages == 0)
                restart();
        }
        else if(message instanceof Commands.PassesCommand) {
            //System.out.println("Adding Passes for Match=" + Info.FFT_match_id + " Team = " + ((Commands.PassesCommand) message).playerDetails.team_name + " Player = " + ((Commands.PassesCommand) message).playerDetails.FFT_player_id + " Passes = " + ((Commands.PassesCommand) message).passes.size());
            ((Commands.PassesCommand) message).playerDetails.numPassesComplete++;
            if(((Commands.PassesCommand) message).playerDetails.numPassesComplete == 3) {
                Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "3rd Pass Command Received for Player = " + ((Commands.PassesCommand) message).playerDetails.FFT_player_id + " Starting 4th.\n");
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
        else if(message instanceof Commands.WorkerRoute) {
            Info.workerrouter.route(((Commands.WorkerRoute) message).object, getSelf());
        }
        else {
            crawl.cleanTerminate("Strange Error - 100");
        }
    }

    private void restart() throws IOException {
        Info.numMessages = 0;
        //System.out.println("Game: " + Info.FFT_match_id + " details saved.");

        if (!Info.FFT_match_id.equals("") && !Persistence.gameSaved(Info.FFT_match_id))
            crawl.cleanTerminate("Game Could not be Saved.");
        Info.FFT_match_id = "";
        Info.match_date = null;
        if(!Info.homeRedCards.isEmpty() || !Info.awayRedCards.isEmpty())
            System.out.println("Red Cards is not empty. Some Error.");

        Info.homeRedCards = new HashMap<>(); Info.awayRedCards = new HashMap<>();
        if(Info.fileWriter != null) {
            Info.fileWriter.write("\n" + "END\n");
            Info.fileWriter.flush();
            Info.fileWriter.close();
            Info.fileWriter = null;

        }

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

            currentIndex++;
            long curTime = System.currentTimeMillis();
            System.out.println("Time Taken = " + (curTime - startTime));
            startTime = curTime;

            Info.fileWriter = new FileWriter(Info.prepend + "LOGS-" + currentIndex, true);
            Info.fileWriter.write("\n" + "START\n");
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
