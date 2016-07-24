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
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private long startTime = -1;
    private int index = -1;
    private int num_messages_remaining = 0;

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
        }
        else if(message instanceof Commands.ShotsCommand) {
            //log.info("Shots Command received by Tracker " + getSelf().path() + " Num Messages Remaining = " + ((Commands.ShotsCommand) message).playerDetails.matchGlobals.numMessagesRemaining);
            //System.out.println("Adding Shots for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.ShotsCommand) message).playerDetails.team_name + " Player = " + ((Commands.ShotsCommand) message).playerDetails.FFT_player_id + " Shots = " + ((Commands.ShotsCommand) message).shots.size());
            if (!Persistence.addShots(((Commands.ShotsCommand) message).shots, ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ShotsCommand) message).playerDetails.team_name, ((Commands.ShotsCommand) message).playerDetails.FFT_player_id, ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add shots Failed in Persistence.", ((Commands.ShotsCommand) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }

            if (((Commands.ShotsCommand) message).index < 5) {
                Distributor.ioRouter.route(commands.new ShotsCommand(((Commands.ShotsCommand) message).playerDetails, ((Commands.ShotsCommand) message).index + 1), getSender());
            } else {
                Distributor.ioRouter.route(commands.new PenaltiesCommand(((Commands.ShotsCommand) message).playerDetails), getSelf());
            }
        }
        else if(message instanceof Commands.PenaltiesCommand) {
            //System.out.println("Adding Penalties for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.PenaltiesCommand) message).playerDetails.team_name + " Player = " + ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id + " Penalties = " + ((Commands.PenaltiesCommand) message).penalties.size());
            if (!Persistence.addPenalties(((Commands.PenaltiesCommand) message).penalties, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.PenaltiesCommand) message).playerDetails.team_name, ((Commands.PenaltiesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add penalties Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new FreekickshotsCommand(((Commands.PenaltiesCommand) message).playerDetails), getSelf());
        }
        else if(message instanceof Commands.FreekickshotsCommand) {
            //System.out.println("Adding FKShots for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.FreekickshotsCommand) message).playerDetails.team_name + " Player = " + ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id + " FKShots = " + ((Commands.FreekickshotsCommand) message).freekickshots.size());
            if (!Persistence.addFKShots(((Commands.FreekickshotsCommand) message).freekickshots, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.FreekickshotsCommand) message).playerDetails.team_name, ((Commands.FreekickshotsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add FKshots Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new PassesCommand(((Commands.FreekickshotsCommand) message).playerDetails, 1), getSender());
        }
        else if(message instanceof Commands.PassesCommand) {
            //System.out.println("Adding Passes for Match=" + ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID() + " Team = " + ((Commands.PassesCommand) message).playerDetails.team_name + " Player = " + ((Commands.PassesCommand) message).playerDetails.FFT_player_id + " Passes = " + ((Commands.PassesCommand) message).passes.size());

            if (!Persistence.addPasses(((Commands.PassesCommand) message).passes, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.PassesCommand) message).playerDetails.team_name, ((Commands.PassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add passes Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            if (((Commands.PassesCommand) message).index < 4) {
                Distributor.ioRouter.route(commands.new PassesCommand(((Commands.PassesCommand) message).playerDetails, ((Commands.PassesCommand) message).index + 1), getSender());
            } else {
                Distributor.ioRouter.route(commands.new AssistsCommand(((Commands.PassesCommand) message).playerDetails, 1), getSender());
            }
        }
        else if(message instanceof Commands.AssistsCommand) {
            if (!Persistence.addAssists(((Commands.AssistsCommand) message).assists, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.AssistsCommand) message).playerDetails.team_name, ((Commands.AssistsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add assists Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            if (((Commands.AssistsCommand) message).index == 1) {
                Distributor.ioRouter.route(commands.new AssistsCommand(((Commands.AssistsCommand) message).playerDetails, 2), getSender());
            } else {
                Distributor.ioRouter.route(commands.new ReceivedPassesCommand(((Commands.AssistsCommand) message).playerDetails), getSender());
            }
        }
        else if(message instanceof Commands.ReceivedPassesCommand) {
            if (!Persistence.addReceivedPasses(((Commands.ReceivedPassesCommand) message).receivedpasses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ReceivedPassesCommand) message).playerDetails.team_name, ((Commands.ReceivedPassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add received passes Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new ChancesCreatedCommand(((Commands.ReceivedPassesCommand) message).playerDetails, 1), getSender());
        }
        else if(message instanceof Commands.ChancesCreatedCommand) {
            if (!Persistence.addChancesCreated(((Commands.ChancesCreatedCommand) message).chancescreated, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ChancesCreatedCommand) message).playerDetails.team_name, ((Commands.ChancesCreatedCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add chances created Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            if (((Commands.ChancesCreatedCommand) message).index == 1) {
                Distributor.ioRouter.route(commands.new ChancesCreatedCommand(((Commands.ChancesCreatedCommand) message).playerDetails, 2), getSender());
            } else {
                Distributor.ioRouter.route(commands.new CrossesCommand(((Commands.ChancesCreatedCommand) message).playerDetails), getSender());
            }
        }
        else if(message instanceof Commands.CrossesCommand) {
            if (!Persistence.addCrosses(((Commands.CrossesCommand) message).crosses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.CrossesCommand) message).playerDetails.team_name, ((Commands.CrossesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add crosses Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new TakeOnsCommand(((Commands.CrossesCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.TakeOnsCommand) {
            if (!Persistence.addTakeOns(((Commands.TakeOnsCommand) message).takeons, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.TakeOnsCommand) message).playerDetails.team_name, ((Commands.TakeOnsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add takeons Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new CornersCommand(((Commands.TakeOnsCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.CornersCommand) {
            if (!Persistence.addCorners(((Commands.CornersCommand) message).corners, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.CornersCommand) message).playerDetails.team_name, ((Commands.CornersCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add corners Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new OffsidePassesCommand(((Commands.CornersCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.OffsidePassesCommand) {
            if (!Persistence.addOffsidePasses(((Commands.OffsidePassesCommand) message).offsidepasses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.OffsidePassesCommand) message).playerDetails.team_name, ((Commands.OffsidePassesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add offside passes Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new BallRecoveriesCommand(((Commands.OffsidePassesCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.BallRecoveriesCommand) {
            if (!Persistence.addBallRecoveries(((Commands.BallRecoveriesCommand) message).ballrecoveries, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.BallRecoveriesCommand) message).playerDetails.team_name, ((Commands.BallRecoveriesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add ball recoveries Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new TacklesCommand(((Commands.BallRecoveriesCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.TacklesCommand) {
            if (!Persistence.addTackles(((Commands.TacklesCommand) message).tackles, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.TacklesCommand) message).playerDetails.team_name, ((Commands.TacklesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add tackles Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new InterceptionsCommand(((Commands.TacklesCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.InterceptionsCommand) {
            if (!Persistence.addInterceptions(((Commands.InterceptionsCommand) message).interceptions, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.InterceptionsCommand) message).playerDetails.team_name, ((Commands.InterceptionsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add interceptions Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new BlocksCommand(((Commands.InterceptionsCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.BlocksCommand) {
            if (!Persistence.addBlocks(((Commands.BlocksCommand) message).blocks, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.BlocksCommand) message).playerDetails.team_name, ((Commands.BlocksCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add blocks Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new ClearancesCommand(((Commands.BlocksCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.ClearancesCommand) {
            if (!Persistence.addClearances(((Commands.ClearancesCommand) message).clearances, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.ClearancesCommand) message).playerDetails.team_name, ((Commands.ClearancesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add clearances Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new AerialDuelsCommand(((Commands.ClearancesCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.AerialDuelsCommand) {
            if (!Persistence.addAerialDuels(((Commands.AerialDuelsCommand) message).aerialduels, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.AerialDuelsCommand) message).playerDetails.team_name, ((Commands.AerialDuelsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add aerial duels Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new BlockedCrossesCommand(((Commands.AerialDuelsCommand) message).playerDetails), getSender());
        }
        else if(message instanceof Commands.BlockedCrossesCommand) {
            if (!Persistence.addBlockedCrosses(((Commands.BlockedCrossesCommand) message).blockedcrosses, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.BlockedCrossesCommand) message).playerDetails.team_name, ((Commands.BlockedCrossesCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add blocked crosses Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            Distributor.ioRouter.route(commands.new DefensiveErrorsCommand(((Commands.BlockedCrossesCommand) message).playerDetails, 1), getSender());
        }
        else if(message instanceof Commands.DefensiveErrorsCommand) {
            if (!Persistence.addDefensiveErrors(((Commands.DefensiveErrorsCommand) message).defensiveerrors, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.DefensiveErrorsCommand) message).playerDetails.team_name, ((Commands.DefensiveErrorsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add defensive errors Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            if (((Commands.DefensiveErrorsCommand) message).index == 1) {
                Distributor.ioRouter.route(commands.new DefensiveErrorsCommand(((Commands.DefensiveErrorsCommand) message).playerDetails, 2), getSender());
            } else {
                Distributor.ioRouter.route(commands.new FoulsCommand(((Commands.DefensiveErrorsCommand) message).playerDetails, 1), getSender());
            }
        }
        else if(message instanceof Commands.FoulsCommand) {
            if (!Persistence.addFouls(((Commands.FoulsCommand) message).fouls, ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), ((Commands.FoulsCommand) message).playerDetails.team_name, ((Commands.FoulsCommand) message).playerDetails.FFT_player_id, ((Commands.Global) message).playerDetails.matchGlobals.getSeason())) {
                crawl.cleanTerminate("Add fouls Failed in Persistence.", ((Commands.Global) message).playerDetails.matchGlobals.getFFT_Match_ID(), getContext().parent());
                return;
            }
            if (((Commands.FoulsCommand) message).index == 1) {
                Distributor.ioRouter.route(commands.new FoulsCommand(((Commands.FoulsCommand) message).playerDetails, 2), getSender());
            } else {
                exitMatch(((Commands.Global) message).playerDetails.matchGlobals);
            }
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
        if(Distributor.perfActor != null)
            Distributor.perfActor.tell("MatchComplete", getSelf());
        getContext().parent().tell("NextMatch", getSelf());
    }
}
