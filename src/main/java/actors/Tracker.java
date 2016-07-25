package actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import crawl.Crawl;
import defs.Global;
import defs.MatchGlobals;
import defs.commands.*;
import fourfourtwo.Persistence;

import java.io.IOException;

//import java.util.concurrent.Future;

/**
 * Created by gsm on 9/12/15.
 */
public class Tracker extends UntypedActor {
    private final Crawl crawl = new Crawl();
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private long startTime = -1;
    private int index = -1;

    public void onReceive(Object message) throws Exception {
        if (index != -1 && Distributor.perfActor != null)
            Distributor.perfActor.tell("Tracker-" + index, getSelf());

        if(message instanceof String) {
            if (((String) message).startsWith("Setup")) {
                this.index = Integer.parseInt(((String) message).split("-")[1]);
                log.info("Setup message received by Tracker " + getSelf().path() + " Asking Distributor for Next Match.");
                startTime = System.currentTimeMillis();
                getSender().tell("NextMatch", getSelf());
            }
        } else if (message instanceof ShotsCommand) {
            //log.info("Shots Command received by Tracker " + getSelf().path() + " Num Messages Remaining = " + ((ShotsCommand) message).playerDetails.matchGlobals.numMessagesRemaining);
            //System.out.println("Adding Shots for Match=" + ((Global) message).playerDetails.matchGlobals.FFT_Match_ID + " Team = " + ((ShotsCommand) message).playerDetails.team_name + " Player = " + ((ShotsCommand) message).playerDetails.FFT_player_id + " Shots = " + ((ShotsCommand) message).shots.size());
            if (!Persistence.addShots(((ShotsCommand) message).shots, ((ShotsCommand) message).playerDetails.matchGlobals.FFT_Match_ID, ((ShotsCommand) message).playerDetails.team_name, ((ShotsCommand) message).playerDetails.FFT_player_id, ((ShotsCommand) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add shots Failed in Persistence.", ((ShotsCommand) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }

            if (((ShotsCommand) message).index < 5) {
                System.out.println("Received Shots Command for index = " + ((ShotsCommand) message).index);
                Distributor.ioRouter.route(new ShotsCommand(((ShotsCommand) message).playerDetails, ((ShotsCommand) message).index + 1), getSender());
            } else {
                Distributor.ioRouter.route(new PenaltiesCommand(((ShotsCommand) message).playerDetails), getSelf());
            }
        } else if (message instanceof PenaltiesCommand) {
            //System.out.println("Adding Penalties for Match=" + ((Global) message).playerDetails.matchGlobals.FFT_Match_ID + " Team = " + ((PenaltiesCommand) message).playerDetails.team_name + " Player = " + ((PenaltiesCommand) message).playerDetails.FFT_player_id + " Penalties = " + ((PenaltiesCommand) message).penalties.size());
            if (!Persistence.addPenalties(((PenaltiesCommand) message).penalties, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((PenaltiesCommand) message).playerDetails.team_name, ((PenaltiesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add penalties Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new FreekickshotsCommand(((PenaltiesCommand) message).playerDetails), getSelf());
        } else if (message instanceof FreekickshotsCommand) {
            //System.out.println("Adding FKShots for Match=" + ((Global) message).playerDetails.matchGlobals.FFT_Match_ID + " Team = " + ((FreekickshotsCommand) message).playerDetails.team_name + " Player = " + ((FreekickshotsCommand) message).playerDetails.FFT_player_id + " FKShots = " + ((FreekickshotsCommand) message).freekickshots.size());
            if (!Persistence.addFKShots(((FreekickshotsCommand) message).freekickshots, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((FreekickshotsCommand) message).playerDetails.team_name, ((FreekickshotsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add FKshots Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new PassesCommand(((FreekickshotsCommand) message).playerDetails, 1), getSender());
        } else if (message instanceof PassesCommand) {
            //System.out.println("Adding Passes for Match=" + ((Global) message).playerDetails.matchGlobals.FFT_Match_ID + " Team = " + ((PassesCommand) message).playerDetails.team_name + " Player = " + ((PassesCommand) message).playerDetails.FFT_player_id + " Passes = " + ((PassesCommand) message).passes.size());

            if (!Persistence.addPasses(((PassesCommand) message).passes, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((PassesCommand) message).playerDetails.team_name, ((PassesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add passes Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            if (((PassesCommand) message).index < 4) {
                Distributor.ioRouter.route(new PassesCommand(((PassesCommand) message).playerDetails, ((PassesCommand) message).index + 1), getSender());
            } else {
                Distributor.ioRouter.route(new AssistsCommand(((PassesCommand) message).playerDetails, 1), getSender());
            }
        } else if (message instanceof AssistsCommand) {
            if (!Persistence.addAssists(((AssistsCommand) message).assists, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((AssistsCommand) message).playerDetails.team_name, ((AssistsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add assists Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            if (((AssistsCommand) message).index == 1) {
                Distributor.ioRouter.route(new AssistsCommand(((AssistsCommand) message).playerDetails, 2), getSender());
            } else {
                Distributor.ioRouter.route(new ReceivedPassesCommand(((AssistsCommand) message).playerDetails), getSender());
            }
        } else if (message instanceof ReceivedPassesCommand) {
            if (!Persistence.addReceivedPasses(((ReceivedPassesCommand) message).receivedpasses, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((ReceivedPassesCommand) message).playerDetails.team_name, ((ReceivedPassesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add received passes Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new ChancesCreatedCommand(((ReceivedPassesCommand) message).playerDetails, 1), getSender());
        } else if (message instanceof ChancesCreatedCommand) {
            if (!Persistence.addChancesCreated(((ChancesCreatedCommand) message).chancescreated, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((ChancesCreatedCommand) message).playerDetails.team_name, ((ChancesCreatedCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add chances created Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            if (((ChancesCreatedCommand) message).index == 1) {
                Distributor.ioRouter.route(new ChancesCreatedCommand(((ChancesCreatedCommand) message).playerDetails, 2), getSender());
            } else {
                Distributor.ioRouter.route(new CrossesCommand(((ChancesCreatedCommand) message).playerDetails), getSender());
            }
        } else if (message instanceof CrossesCommand) {
            if (!Persistence.addCrosses(((CrossesCommand) message).crosses, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((CrossesCommand) message).playerDetails.team_name, ((CrossesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add crosses Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new TakeOnsCommand(((CrossesCommand) message).playerDetails), getSender());
        } else if (message instanceof TakeOnsCommand) {
            if (!Persistence.addTakeOns(((TakeOnsCommand) message).takeons, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((TakeOnsCommand) message).playerDetails.team_name, ((TakeOnsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add takeons Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new CornersCommand(((TakeOnsCommand) message).playerDetails), getSender());
        } else if (message instanceof CornersCommand) {
            if (!Persistence.addCorners(((CornersCommand) message).corners, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((CornersCommand) message).playerDetails.team_name, ((CornersCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add corners Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new OffsidePassesCommand(((CornersCommand) message).playerDetails), getSender());
        } else if (message instanceof OffsidePassesCommand) {
            if (!Persistence.addOffsidePasses(((OffsidePassesCommand) message).offsidepasses, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((OffsidePassesCommand) message).playerDetails.team_name, ((OffsidePassesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add offside passes Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new BallRecoveriesCommand(((OffsidePassesCommand) message).playerDetails), getSender());
        } else if (message instanceof BallRecoveriesCommand) {
            if (!Persistence.addBallRecoveries(((BallRecoveriesCommand) message).ballrecoveries, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((BallRecoveriesCommand) message).playerDetails.team_name, ((BallRecoveriesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add ball recoveries Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new TacklesCommand(((BallRecoveriesCommand) message).playerDetails), getSender());
        } else if (message instanceof TacklesCommand) {
            if (!Persistence.addTackles(((TacklesCommand) message).tackles, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((TacklesCommand) message).playerDetails.team_name, ((TacklesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add tackles Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new InterceptionsCommand(((TacklesCommand) message).playerDetails), getSender());
        } else if (message instanceof InterceptionsCommand) {
            if (!Persistence.addInterceptions(((InterceptionsCommand) message).interceptions, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((InterceptionsCommand) message).playerDetails.team_name, ((InterceptionsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add interceptions Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new BlocksCommand(((InterceptionsCommand) message).playerDetails), getSender());
        } else if (message instanceof BlocksCommand) {
            if (!Persistence.addBlocks(((BlocksCommand) message).blocks, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((BlocksCommand) message).playerDetails.team_name, ((BlocksCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add blocks Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new ClearancesCommand(((BlocksCommand) message).playerDetails), getSender());
        } else if (message instanceof ClearancesCommand) {
            if (!Persistence.addClearances(((ClearancesCommand) message).clearances, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((ClearancesCommand) message).playerDetails.team_name, ((ClearancesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add clearances Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new AerialDuelsCommand(((ClearancesCommand) message).playerDetails), getSender());
        } else if (message instanceof AerialDuelsCommand) {
            if (!Persistence.addAerialDuels(((AerialDuelsCommand) message).aerialduels, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((AerialDuelsCommand) message).playerDetails.team_name, ((AerialDuelsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add aerial duels Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new BlockedCrossesCommand(((AerialDuelsCommand) message).playerDetails), getSender());
        } else if (message instanceof BlockedCrossesCommand) {
            if (!Persistence.addBlockedCrosses(((BlockedCrossesCommand) message).blockedcrosses, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((BlockedCrossesCommand) message).playerDetails.team_name, ((BlockedCrossesCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add blocked crosses Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            Distributor.ioRouter.route(new DefensiveErrorsCommand(((BlockedCrossesCommand) message).playerDetails, 1), getSender());
        } else if (message instanceof DefensiveErrorsCommand) {
            if (!Persistence.addDefensiveErrors(((DefensiveErrorsCommand) message).defensiveerrors, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((DefensiveErrorsCommand) message).playerDetails.team_name, ((DefensiveErrorsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add defensive errors Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            if (((DefensiveErrorsCommand) message).index == 1) {
                Distributor.ioRouter.route(new DefensiveErrorsCommand(((DefensiveErrorsCommand) message).playerDetails, 2), getSender());
            } else {
                Distributor.ioRouter.route(new FoulsCommand(((DefensiveErrorsCommand) message).playerDetails, 1), getSender());
            }
        } else if (message instanceof FoulsCommand) {
            if (!Persistence.addFouls(((FoulsCommand) message).fouls, ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, ((FoulsCommand) message).playerDetails.team_name, ((FoulsCommand) message).playerDetails.FFT_player_id, ((Global) message).playerDetails.matchGlobals.season)) {
                crawl.cleanTerminate("Add fouls Failed in Persistence.", ((Global) message).playerDetails.matchGlobals.FFT_Match_ID, getContext().parent());
                return;
            }
            if (((FoulsCommand) message).index == 1) {
                Distributor.ioRouter.route(new FoulsCommand(((FoulsCommand) message).playerDetails, 2), getSender());
            } else {
                exitMatch(((Global) message).playerDetails.matchGlobals);
            }
        }
        else {
            crawl.cleanTerminate("Strange Error - 100");
        }
    }

    private void exitMatch(MatchGlobals matchGlobals) throws IOException {
        //System.out.println("Game: " + ((Global) message).playerDetails.matchGlobals.FFT_Match_ID + " details saved.");

        if (!matchGlobals.FFT_Match_ID.equals("") && !Persistence.gameSaved(matchGlobals.FFT_Match_ID))
            crawl.cleanTerminate("Game " + matchGlobals.FFT_Match_ID + " Could not be Saved.");

        int num_substitutions_home = matchGlobals.homeSubstitutions.size();
        int num_substitutions_away = matchGlobals.awaySubstitutions.size();

        if(num_substitutions_home > 0) {
            matchGlobals.homeSubstitutions.forEach((key, value) -> crawl.addSubstitutions(key, value));
        }

        if(num_substitutions_away > 0) {
            matchGlobals.awaySubstitutions.forEach((key, value) -> crawl.addSubstitutions(key, value));
        }
        long endTime = System.currentTimeMillis();
        log.info("Match " + matchGlobals.gameLink + " Details saved. Time taken = " + (endTime - startTime));
        startTime = System.currentTimeMillis();
        if(Distributor.perfActor != null)
            Distributor.perfActor.tell("MatchComplete", getSelf());
        getContext().parent().tell("NextMatch", getSelf());
    }
}
