package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import command.Commands;
import crawl.Crawl;
import fourfourtwo.Persistence;

import java.io.IOException;

//import java.util.concurrent.Future;

/**
 * Created by gsm on 9/12/15.
 */
public class Tracker extends UntypedActor {
    private final Commands commands = new Commands();
    private final Crawl crawl = new Crawl();
    private long startTime = -1;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public void onReceive(Object message) throws Exception {
        if(message instanceof String) {
            if(message.equals("Setup")) {
                log.info("Setup message received by Tracker " + getSelf().path() + " Asking Distributor for Next Match.");
                startTime = System.currentTimeMillis();
                getSender().tell("NextMatch", getSelf());
            }
        }
        else if(message instanceof Commands.ShotsCommand) {
            log.info("Shots Command received by Tracker " + getSelf().path() + " Num Messages Remaining = " + ((Commands.ShotsCommand) message).playerDetails.matchGlobals.numMessagesRemaining);
            //System.out.println("Adding Shots for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.ShotsCommand) message).playerDetails.team_name + " Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Shots = " + ((Commands.ShotsCommand) message).shots.size());
            if (!Persistence.addShots(((Commands.ShotsCommand) message).shots, ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ShotsCommand) message).playerDetails.team_name, ((Commands.ShotsCommand) message).playerDetails.FFT_player_id, ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add shots Failed in Persistence.");
            }
            ((Commands.ShotsCommand) message).playerDetails.matchGlobals.setNumMessagesRemaining(((Commands.ShotsCommand) message).playerDetails.matchGlobals.getNumMessagesRemaining() - 1);
            ((Commands.ShotsCommand) message).playerDetails.numShotsComplete++;
            if(((Commands.ShotsCommand) message).playerDetails.numShotsComplete == 4) {
                //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "4th Shot Command Received for Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Starting 5th.\n");
                Distributor.ioRouter.route(commands.new ShotsCommand(((Commands.ShotsCommand) message).playerDetails, 5), getSelf());
            }
            else if(((Commands.ShotsCommand) message).playerDetails.numShotsComplete == 5) {
                //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "5th Shot Command Received for Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Starting Others.\n");
                Distributor.ioRouter.route(commands.new PenaltiesCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
                Distributor.ioRouter.route(commands.new FreekickshotsCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
            }
            if (((Commands.ShotsCommand) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.PenaltiesCommand) {
            //System.out.println("Adding Penalties for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.PenaltiesCommand) message).playerDetails.team_name + " Player = " + ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id + " Penalties = " + ((Commands.PenaltiesCommand) message).penalties.size());
            if (!Persistence.addPenalties(((Commands.PenaltiesCommand) message).penalties, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.PenaltiesCommand) message).playerDetails.team_name, ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add penalties Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;

            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.FreekickshotsCommand) {
            //System.out.println("Adding FKShots for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.FreekickshotsCommand) message).playerDetails.team_name + " Player = " + ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id + " FKShots = " + ((Commands.FreekickshotsCommand) message).freekickshots.size());
            if (!Persistence.addFKShots(((Commands.FreekickshotsCommand) message).freekickshots, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.FreekickshotsCommand) message).playerDetails.team_name, ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add FKshots Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.PassesCommand) {
            //System.out.println("Adding Passes for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.PassesCommand) message).playerDetails.team_name + " Player = " + ((Commands.PassesCommand) message).playerDetails.FFT_player_id + " Passes = " + ((Commands.PassesCommand) message).passes.size());
            ((Commands.PassesCommand) message).playerDetails.numPassesComplete++;
            if(((Commands.PassesCommand) message).playerDetails.numPassesComplete == 3) {
                //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "3rd Pass Command Received for Player = " + ((Commands.PassesCommand) message).playerDetails.FFT_player_id + " Starting 4th.\n");
                Distributor.ioRouter.route(commands.new PassesCommand(((Commands.PassesCommand) message).playerDetails, 4), getSelf());
            }
            if(!Persistence.addPasses(((Commands.PassesCommand) message).passes, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.PassesCommand) message).playerDetails.team_name, ((Commands.PassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add passes Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.AssistsCommand) {
            if(!Persistence.addAssists(((Commands.AssistsCommand) message).assists, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.AssistsCommand) message).playerDetails.team_name, ((Commands.AssistsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add assists Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.ReceivedPassesCommand) {
            if(!Persistence.addReceivedPasses(((Commands.ReceivedPassesCommand) message).receivedpasses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ReceivedPassesCommand) message).playerDetails.team_name, ((Commands.ReceivedPassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add received passes Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.ChancesCreatedCommand) {
            if (!Persistence.addChancesCreated(((Commands.ChancesCreatedCommand) message).chancescreated, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ChancesCreatedCommand) message).playerDetails.team_name, ((Commands.ChancesCreatedCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add chances created Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.LongPassesCommand) {
            if (!Persistence.addLongPasses(((Commands.LongPassesCommand) message).longpasses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.LongPassesCommand) message).playerDetails.team_name, ((Commands.LongPassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add long passes Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.ShortPassesCommand) {
            if (!Persistence.addShortPasses(((Commands.ShortPassesCommand) message).shortpasses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ShortPassesCommand) message).playerDetails.team_name, ((Commands.ShortPassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add short passes Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.CrossesCommand) {
            if (!Persistence.addCrosses(((Commands.CrossesCommand) message).crosses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.CrossesCommand) message).playerDetails.team_name, ((Commands.CrossesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add crosses Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.TakeOnsCommand) {
            if (!Persistence.addTakeOns(((Commands.TakeOnsCommand) message).takeons, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.TakeOnsCommand) message).playerDetails.team_name, ((Commands.TakeOnsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add takeons Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.CornersCommand) {
            if (!Persistence.addCorners(((Commands.CornersCommand) message).corners, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.CornersCommand) message).playerDetails.team_name, ((Commands.CornersCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add corners Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.OffsidePassesCommand) {
            if (!Persistence.addOffsidePasses(((Commands.OffsidePassesCommand) message).offsidepasses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.OffsidePassesCommand) message).playerDetails.team_name, ((Commands.OffsidePassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add offside passes Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.BallRecoveriesCommand) {
            if (!Persistence.addBallRecoveries(((Commands.BallRecoveriesCommand) message).ballrecoveries, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.BallRecoveriesCommand) message).playerDetails.team_name, ((Commands.BallRecoveriesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add ball recoveries Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.TacklesCommand) {
            if (!Persistence.addTackles(((Commands.TacklesCommand) message).tackles, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.TacklesCommand) message).playerDetails.team_name, ((Commands.TacklesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add tackles Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.InterceptionsCommand) {
            if (!Persistence.addInterceptions(((Commands.InterceptionsCommand) message).interceptions, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.InterceptionsCommand) message).playerDetails.team_name, ((Commands.InterceptionsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add interceptions Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.BlocksCommand) {
            if (!Persistence.addBlocks(((Commands.BlocksCommand) message).blocks, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.BlocksCommand) message).playerDetails.team_name, ((Commands.BlocksCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add blocks Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.ClearancesCommand) {
            if (!Persistence.addClearances(((Commands.ClearancesCommand) message).clearances, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ClearancesCommand) message).playerDetails.team_name, ((Commands.ClearancesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add clearances Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.AerialDuelsCommand) {
            if (!Persistence.addAerialDuels(((Commands.AerialDuelsCommand) message).aerialduels, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.AerialDuelsCommand) message).playerDetails.team_name, ((Commands.AerialDuelsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add aerial duels Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.BlockedCrossesCommand) {
            if (!Persistence.addBlockedCrosses(((Commands.BlockedCrossesCommand) message).blockedcrosses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.BlockedCrossesCommand) message).playerDetails.team_name, ((Commands.BlockedCrossesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add blocked crosses Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.DefensiveErrorsCommand) {
            if (!Persistence.addDefensiveErrors(((Commands.DefensiveErrorsCommand) message).defensiveerrors, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.DefensiveErrorsCommand) message).playerDetails.team_name, ((Commands.DefensiveErrorsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add defensive errors Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else if(message instanceof Commands.FoulsCommand) {
            if (!Persistence.addFouls(((Commands.FoulsCommand) message).fouls, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.FoulsCommand) message).playerDetails.team_name, ((Commands.FoulsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason()))
                crawl.cleanTerminate("Add fouls Failed in Persistence.");
            ((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining--;
            if (((Commands.Global) message).playerDetails.matchGlobals.numMessagesRemaining == 0)
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
        }
        else {
            crawl.cleanTerminate("Strange Error - 100");
        }
    }

    private void exitMatch(Commands.MatchGlobals matchGlobals) throws IOException {
        //System.out.println("Game: " + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " details saved.");

        if (!matchGlobals.getFFT_Match_ID().equals("") && !Persistence.gameSaved(matchGlobals.getFFT_Match_ID()))
            crawl.cleanTerminate("Game " + matchGlobals.getFFT_Match_ID() + " Could not be Saved.");

        int num_substitutions_home = matchGlobals.homeSubstitutions.size();
        int num_substitutions_away = matchGlobals.awaySubstitutions.size();

        if(num_substitutions_home > 0) {
            matchGlobals.homeSubstitutions.forEach((key, value) -> crawl.addSubstitutions(key, value));
        }

        if(num_substitutions_away > 0) {
            matchGlobals.awaySubstitutions.forEach((key, value) -> crawl.addSubstitutions(key, value));
        }
        long endTime = System.currentTimeMillis();
        log.info("Match " + matchGlobals.getGameLink() + " Details saved. Time taken = " + (endTime - startTime));
        startTime = System.currentTimeMillis();
        getContext().parent().tell("NextMatch", getSelf());
    }
}
