package actors;

import akka.actor.UntypedActor;
import command.Commands;
import crawl.Crawl;

/**
 * Created by gsm on 9/11/15.
 */

public class Child extends UntypedActor{
    Crawl crawl = new Crawl();

    public void onReceive(Object message) throws Exception {
        if(message instanceof Commands.ShotsCommand) {
            ((Commands.ShotsCommand) message).shots = crawl.shotsDetails(((Commands.ShotsCommand) message).document, ((Commands.ShotsCommand) message).index);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.PenaltiesCommand) {
            ((Commands.PenaltiesCommand) message).penalties = crawl.penaltyDetails(((Commands.PenaltiesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.FreekickshotsCommand) {
            ((Commands.FreekickshotsCommand) message).freekickshots = crawl.freekickShotsDetails(((Commands.FreekickshotsCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.PassesCommand) {
            ((Commands.PassesCommand) message).passes = crawl.passDetails(((Commands.PassesCommand) message).document, ((Commands.PassesCommand) message).index);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.AssistsCommand) {
            ((Commands.AssistsCommand) message).assists = crawl.assistDetails(((Commands.AssistsCommand) message).document, ((Commands.AssistsCommand) message).index);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.ReceivedPassesCommand) {
            ((Commands.ReceivedPassesCommand) message).receivedpasses = crawl.receivedPassDetails(((Commands.ReceivedPassesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.ChancesCreatedCommand) {
            ((Commands.ChancesCreatedCommand) message).chancescreated = crawl.chancesCreatedDetails(((Commands.ChancesCreatedCommand) message).document, ((Commands.ChancesCreatedCommand) message).index);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CHANCESCREATED. Index = " + ((Commands.ChancesCreatedCommand) message).index + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Chances = " + ((Commands.ChancesCreatedCommand) message).chancescreated.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.LongPassesCommand) {
            ((Commands.LongPassesCommand) message).longpasses = crawl.getLongPassesDetails(((Commands.LongPassesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. LONGPASSES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " LongPasses = " + ((Commands.LongPassesCommand) message).longpasses + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.ShortPassesCommand) {
            ((Commands.ShortPassesCommand) message).shortpasses = crawl.getShortPassesDetails(((Commands.ShortPassesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. SHORTPASSES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " ShortPasses = " + ((Commands.ShortPassesCommand) message).shortpasses + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.CrossesCommand) {
            ((Commands.CrossesCommand) message).crosses = crawl.crossesDetails(((Commands.CrossesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CROSSES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Crosses = " + ((Commands.CrossesCommand) message).crosses.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.TakeOnsCommand) {
            ((Commands.TakeOnsCommand) message).takeons = crawl.takeOnsDetails(((Commands.TakeOnsCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. TAKEONS. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " TakeOns = " + ((Commands.TakeOnsCommand) message).takeons.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.CornersCommand) {
            ((Commands.CornersCommand) message).corners = crawl.cornersDetails(((Commands.CornersCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CORNERS. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Corners = " + ((Commands.CornersCommand) message).corners.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.OffsidePassesCommand) {
            ((Commands.OffsidePassesCommand) message).offsidepasses = crawl.offsidePassesDetails(((Commands.OffsidePassesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. OFFSIDEPASSES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " OffsidePasses = " + ((Commands.OffsidePassesCommand) message).offsidepasses.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.BallRecoveriesCommand) {
            ((Commands.BallRecoveriesCommand) message).ballrecoveries = crawl.ballRecoveriesDetails(((Commands.BallRecoveriesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. BALLRECOVERIES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " BallRecoveries = " + ((Commands.BallRecoveriesCommand) message).ballrecoveries.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.TacklesCommand) {
            ((Commands.TacklesCommand) message).tackles = crawl.tacklesDetails(((Commands.TacklesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. TACKLES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Tackles = " + ((Commands.TacklesCommand) message).tackles.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.InterceptionsCommand) {
            ((Commands.InterceptionsCommand) message).interceptions = crawl.interceptionsDetails(((Commands.InterceptionsCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. INTERCEPTIONS. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Interceptions = " + ((Commands.InterceptionsCommand) message).interceptions.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.BlocksCommand) {
            ((Commands.BlocksCommand) message).blocks = crawl.blocksDetails(((Commands.BlocksCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. BLOCKS. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Blocks = " + ((Commands.BlocksCommand) message).blocks.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.ClearancesCommand) {
            ((Commands.ClearancesCommand) message).clearances = crawl.clearancesDetails(((Commands.ClearancesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. CLEARANCES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Clearances = " + ((Commands.ClearancesCommand) message).clearances.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.AerialDuelsCommand) {
            ((Commands.AerialDuelsCommand) message).aerialduels = crawl.aerialDuelsDetails(((Commands.AerialDuelsCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. AERIALDUELS. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Aerial Duels = " + ((Commands.AerialDuelsCommand) message).aerialduels.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.BlockedCrossesCommand) {
            ((Commands.BlockedCrossesCommand) message).blockedcrosses = crawl.blockedCrossesDetails(((Commands.BlockedCrossesCommand) message).document);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. BLOCKEDCROSSES. " + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " BlockedCrosses = " + ((Commands.BlockedCrossesCommand) message).blockedcrosses.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.DefensiveErrorsCommand) {
            ((Commands.DefensiveErrorsCommand) message).defensiveerrors = crawl.defensiveErrorsDetails(((Commands.DefensiveErrorsCommand) message).document, ((Commands.DefensiveErrorsCommand) message).index);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. DEFENSIVEERRORS. Index = " + ((Commands.DefensiveErrorsCommand) message).index + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " DefensiveErrors = " + ((Commands.DefensiveErrorsCommand) message).defensiveerrors.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.FoulsCommand) {
            ((Commands.FoulsCommand) message).fouls = crawl.foulsDetails(((Commands.FoulsCommand) message).document, ((Commands.FoulsCommand) message).index);
            //Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside onReceive of Child. FOULS. Index = " + ((Commands.FoulsCommand) message).index + "PlayerLink = " + ((Commands.Global) message).playerDetails.playerLink + " Player ID = " + ((Commands.Global) message).playerDetails.FFT_player_id + " Team = " + ((Commands.Global) message).playerDetails.team_name + " Fouls = " + ((Commands.FoulsCommand) message).fouls.size() + "\n");
            getSender().tell(message, getSelf());
        }
        else {
            System.out.println("Strange Error - 100");
            getContext().parent().tell("NextMatch", getSender());
        }
    }
}
