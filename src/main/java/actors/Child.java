package actors;

import akka.actor.UntypedActor;
import crawl.Crawl;
import defs.commands.*;

/**
 * Created by gsm on 9/11/15.
 */

public class Child extends UntypedActor{
    Crawl crawl = new Crawl();
    private int index = -1;
    public void onReceive(Object message) throws Exception {
        if (index != -1 && Distributor.perfActor != null)
            Distributor.perfActor.tell("Child-" + index, getSelf());
        if (message instanceof String) {
            this.index = Integer.parseInt(((String) message).split("-")[1]);
        } else if (message instanceof ShotsCommand) {
            ((ShotsCommand) message).shots = crawl.shotsDetails(((ShotsCommand) message).document, ((ShotsCommand) message).index);
            getSender().tell(message, getSelf());
        } else if (message instanceof PenaltiesCommand) {
            ((PenaltiesCommand) message).penalties = crawl.penaltyDetails(((PenaltiesCommand) message).document);
            getSender().tell(message, getSelf());
        } else if (message instanceof FreekickshotsCommand) {
            ((FreekickshotsCommand) message).freekickshots = crawl.freekickShotsDetails(((FreekickshotsCommand) message).document);
            getSender().tell(message, getSelf());
        } else if (message instanceof PassesCommand) {
            ((PassesCommand) message).passes = crawl.passDetails(((PassesCommand) message).document, ((PassesCommand) message).index);
            getSender().tell(message, getSelf());
        } else if (message instanceof AssistsCommand) {
            ((AssistsCommand) message).assists = crawl.assistDetails(((AssistsCommand) message).document, ((AssistsCommand) message).index);
            getSender().tell(message, getSelf());
        } else if (message instanceof ReceivedPassesCommand) {
            ((ReceivedPassesCommand) message).receivedpasses = crawl.receivedPassDetails(((ReceivedPassesCommand) message).document);
            getSender().tell(message, getSelf());
        } else if (message instanceof ChancesCreatedCommand) {
            ((ChancesCreatedCommand) message).chancescreated = crawl.chancesCreatedDetails(((ChancesCreatedCommand) message).document, ((ChancesCreatedCommand) message).index);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CHANCESCREATED. Index = " + ((ChancesCreatedCommand) message).index + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Chances = " + ((ChancesCreatedCommand) message).chancescreated.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof CrossesCommand) {
            ((CrossesCommand) message).crosses = crawl.crossesDetails(((CrossesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CROSSES. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Crosses = " + ((CrossesCommand) message).crosses.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof TakeOnsCommand) {
            ((TakeOnsCommand) message).takeons = crawl.takeOnsDetails(((TakeOnsCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. TAKEONS. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " TakeOns = " + ((TakeOnsCommand) message).takeons.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof CornersCommand) {
            ((CornersCommand) message).corners = crawl.cornersDetails(((CornersCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CORNERS. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Corners = " + ((CornersCommand) message).corners.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof OffsidePassesCommand) {
            ((OffsidePassesCommand) message).offsidepasses = crawl.offsidePassesDetails(((OffsidePassesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. OFFSIDEPASSES. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " OffsidePasses = " + ((OffsidePassesCommand) message).offsidepasses.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof BallRecoveriesCommand) {
            ((BallRecoveriesCommand) message).ballrecoveries = crawl.ballRecoveriesDetails(((BallRecoveriesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. BALLRECOVERIES. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " BallRecoveries = " + ((BallRecoveriesCommand) message).ballrecoveries.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof TacklesCommand) {
            ((TacklesCommand) message).tackles = crawl.tacklesDetails(((TacklesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. TACKLES. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Tackles = " + ((TacklesCommand) message).tackles.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof InterceptionsCommand) {
            ((InterceptionsCommand) message).interceptions = crawl.interceptionsDetails(((InterceptionsCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. INTERCEPTIONS. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Interceptions = " + ((InterceptionsCommand) message).interceptions.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof BlocksCommand) {
            ((BlocksCommand) message).blocks = crawl.blocksDetails(((BlocksCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. BLOCKS. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Blocks = " + ((BlocksCommand) message).blocks.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof ClearancesCommand) {
            ((ClearancesCommand) message).clearances = crawl.clearancesDetails(((ClearancesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CLEARANCES. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Clearances = " + ((ClearancesCommand) message).clearances.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof AerialDuelsCommand) {
            ((AerialDuelsCommand) message).aerialduels = crawl.aerialDuelsDetails(((AerialDuelsCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. AERIALDUELS. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Aerial Duels = " + ((AerialDuelsCommand) message).aerialduels.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof BlockedCrossesCommand) {
            ((BlockedCrossesCommand) message).blockedcrosses = crawl.blockedCrossesDetails(((BlockedCrossesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. BLOCKEDCROSSES. " + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " BlockedCrosses = " + ((BlockedCrossesCommand) message).blockedcrosses.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof DefensiveErrorsCommand) {
            ((DefensiveErrorsCommand) message).defensiveerrors = crawl.defensiveErrorsDetails(((DefensiveErrorsCommand) message).document, ((DefensiveErrorsCommand) message).index);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. DEFENSIVEERRORS. Index = " + ((DefensiveErrorsCommand) message).index + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " DefensiveErrors = " + ((DefensiveErrorsCommand) message).defensiveerrors.size() + "\n");
            getSender().tell(message, getSelf());
        } else if (message instanceof FoulsCommand) {
            ((FoulsCommand) message).fouls = crawl.foulsDetails(((FoulsCommand) message).document, ((FoulsCommand) message).index);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. FOULS. Index = " + ((FoulsCommand) message).index + "PlayerLink = " + ((Global) message).playerDetails.playerLink + " Player ID = " + ((Global) message).playerDetails.FFT_player_id + " Team = " + ((Global) message).playerDetails.team_name + " Fouls = " + ((FoulsCommand) message).fouls.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else {
            crawl.cleanTerminate("Strange Error - 100");
        }
    }
}
