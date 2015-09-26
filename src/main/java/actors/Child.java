package actors;

import akka.actor.UntypedActor;
import akka.dispatch.Dispatcher;
import command.Commands;
import crawl.Crawl;
import fourfourtwo.Persistence;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gsm on 9/11/15.
 */

public class Child extends UntypedActor{
    Crawl crawl = new Crawl();
    //Commands commands = new Commands();

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
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.LongPassesCommand) {
            ((Commands.LongPassesCommand) message).longpasses = crawl.getLongPassesDetails(((Commands.LongPassesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.ShortPassesCommand) {
            ((Commands.ShortPassesCommand) message).shortpasses = crawl.getShortPassesDetails(((Commands.ShortPassesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.CrossesCommand) {
            ((Commands.CrossesCommand) message).crosses = crawl.crossesDetails(((Commands.CrossesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.TakeOnsCommand) {
            ((Commands.TakeOnsCommand) message).takeons = crawl.takeOnsDetails(((Commands.TakeOnsCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.CornersCommand) {
            ((Commands.CornersCommand) message).corners = crawl.cornersDetails(((Commands.CornersCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.OffsidePassesCommand) {
            ((Commands.OffsidePassesCommand) message).offsidepasses = crawl.offsidePassesDetails(((Commands.OffsidePassesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.BallRecoveriesCommand) {
            ((Commands.BallRecoveriesCommand) message).ballrecoveries = crawl.ballRecoveriesDetails(((Commands.BallRecoveriesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.TacklesCommand) {
            ((Commands.TacklesCommand) message).tackles = crawl.tacklesDetails(((Commands.TacklesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.InterceptionsCommand) {
            ((Commands.InterceptionsCommand) message).interceptions = crawl.interceptionsDetails(((Commands.InterceptionsCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.BlocksCommand) {
            ((Commands.BlocksCommand) message).blocks = crawl.blocksDetails(((Commands.BlocksCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.ClearancesCommand) {
            ((Commands.ClearancesCommand) message).clearances = crawl.clearancesDetails(((Commands.ClearancesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.AerialDuelsCommand) {
            ((Commands.AerialDuelsCommand) message).aerialduels = crawl.aerialDuelsDetails(((Commands.AerialDuelsCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.BlockedCrossesCommand) {
            ((Commands.BlockedCrossesCommand) message).blockedcrosses = crawl.blockedCrossesDetails(((Commands.BlockedCrossesCommand) message).document);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.DefensiveErrorsCommand) {
            ((Commands.DefensiveErrorsCommand) message).defensiveerrors = crawl.defensiveErrorsDetails(((Commands.DefensiveErrorsCommand) message).document, ((Commands.DefensiveErrorsCommand) message).index);
            getSender().tell(message, getSelf());
        }
        else if(message instanceof Commands.FoulsCommand) {
            ((Commands.FoulsCommand) message).fouls = crawl.foulsDetails(((Commands.FoulsCommand) message).document, ((Commands.FoulsCommand) message).index);
            getSender().tell(message, getSelf());
        }
        else {
            crawl.cleanTerminate("Strange Error - 100");
        }
    }
}
