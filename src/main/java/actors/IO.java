package actors;

import akka.actor.UntypedActor;
import command.Commands;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by gsm on 9/23/15.
 */
public class IO extends UntypedActor{
    private Commands commands = new Commands();

    private Document getDocument(String link, Boolean forceful) {
        try {
            Document document = Jsoup.connect(link).timeout(10000).get();
            Info.perfActor.tell("Success", getSelf());
            return document;
        } catch(IOException e) {
            Info.perfActor.tell("Failure", getSelf());
            if(forceful)
                return getDocument(link, true);
            else
                return null;
        }
    }

    public void onReceive(Object message) throws Exception {
        if(message instanceof Commands.StartCommand) {

        }
        if(message instanceof Commands.ShotsCommand) {
            //System.out.println("Child Called ... Dispatcher = " + getContext().dispatcher().toString());
            switch(((Commands.ShotsCommand) message).index) {
                case 1:
                    ((Commands.ShotsCommand) message).document = getDocument(((Commands.ShotsCommand) message).playerDetails.playerLink.substring(0, ((Commands.ShotsCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_12#tabs-wrapper-anchor", false);
                    break;
                case 2:
                    ((Commands.ShotsCommand) message).document = getDocument(((Commands.ShotsCommand) message).playerDetails.playerLink.substring(0, ((Commands.ShotsCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_13#tabs-wrapper-anchor", false);
                    break;
                case 3:
                    ((Commands.ShotsCommand) message).document = getDocument(((Commands.ShotsCommand) message).playerDetails.playerLink.substring(0, ((Commands.ShotsCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_11#tabs-wrapper-anchor", false);
                    break;
                case 4:
                    ((Commands.ShotsCommand) message).document = getDocument(((Commands.ShotsCommand) message).playerDetails.playerLink.substring(0, ((Commands.ShotsCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_14#tabs-wrapper-anchor", false);
                    break;
                case 5:
                    ((Commands.ShotsCommand) message).document = getDocument(((Commands.ShotsCommand) message).playerDetails.playerLink.substring(0, ((Commands.ShotsCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_08#tabs-wrapper-anchor", false);
                    break;
            }

            if(((Commands.ShotsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());

        }
        else if(message instanceof Commands.PenaltiesCommand) {
            ((Commands.PenaltiesCommand) message).document = getDocument(((Commands.PenaltiesCommand) message).playerDetails.playerLink.substring(0, ((Commands.PenaltiesCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_09#tabs-wrapper-anchor", false);
            if(((Commands.PenaltiesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.FreekickshotsCommand) {
            ((Commands.FreekickshotsCommand) message).document = getDocument(((Commands.FreekickshotsCommand) message).playerDetails.playerLink.substring(0, ((Commands.FreekickshotsCommand) message).playerDetails.playerLink.length() - 30) + "0_SHOT_07#tabs-wrapper-anchor", false);
            if(((Commands.FreekickshotsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.PassesCommand) {
            switch (((Commands.PassesCommand) message).index) {
                case 1:
                    ((Commands.PassesCommand) message).document = getDocument(((Commands.PassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.PassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_08#tabs-wrapper-anchor", false);
                    break;
                case 2:
                    ((Commands.PassesCommand) message).document = getDocument(((Commands.PassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.PassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_0802#tabs-wrapper-anchor", false);
                    break;
                case 3:
                    ((Commands.PassesCommand) message).document = getDocument(((Commands.PassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.PassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_0801#tabs-wrapper-anchor", false);
                    break;
                case 4:
                    ((Commands.PassesCommand) message).document = getDocument(((Commands.PassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.PassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_113#tabs-wrapper-anchor", false);
                    break;
            }

            if(((Commands.PassesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.AssistsCommand) {
            if(((Commands.AssistsCommand) message).index == 1)
                ((Commands.AssistsCommand) message).document = getDocument(((Commands.AssistsCommand) message).playerDetails.playerLink.substring(0, ((Commands.AssistsCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_0901#tabs-wrapper-anchor", false);
            else
                ((Commands.AssistsCommand) message).document = getDocument(((Commands.AssistsCommand) message).playerDetails.playerLink.substring(0, ((Commands.AssistsCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_0902#tabs-wrapper-anchor", false);

            if(((Commands.AssistsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.ReceivedPassesCommand) {
            ((Commands.ReceivedPassesCommand) message).document = getDocument(((Commands.ReceivedPassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.ReceivedPassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_07#tabs-wrapper-anchor", false);
            if(((Commands.ReceivedPassesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.ChancesCreatedCommand) {
            if(((Commands.ChancesCreatedCommand) message).index == 1)
                ((Commands.ChancesCreatedCommand) message).document = getDocument(((Commands.ChancesCreatedCommand) message).playerDetails.playerLink.substring(0, ((Commands.ChancesCreatedCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_1101#tabs-wrapper-anchor", false);
            else
                ((Commands.ChancesCreatedCommand) message).document = getDocument(((Commands.ChancesCreatedCommand) message).playerDetails.playerLink.substring(0, ((Commands.ChancesCreatedCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_1102#tabs-wrapper-anchor", false);
            if(((Commands.ChancesCreatedCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.LongPassesCommand) {
            ((Commands.LongPassesCommand) message).document = getDocument(((Commands.LongPassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.LongPassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_12#tabs-wrapper-anchor", false);
            if(((Commands.LongPassesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.ShortPassesCommand) {
            ((Commands.ShortPassesCommand) message).document = getDocument(((Commands.ShortPassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.ShortPassesCommand) message).playerDetails.playerLink.length() - 30) + "1_PASS_13#tabs-wrapper-anchor", false);
            if(((Commands.ShortPassesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.CrossesCommand) {
            ((Commands.CrossesCommand) message).document = getDocument(((Commands.CrossesCommand) message).playerDetails.playerLink.substring(0, ((Commands.CrossesCommand) message).playerDetails.playerLink.length() - 30) + "2_ATTACK_01#tabs-wrapper-anchor", false);
            if(((Commands.CrossesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.TakeOnsCommand) {
            ((Commands.TakeOnsCommand) message).document = getDocument(((Commands.TakeOnsCommand) message).playerDetails.playerLink.substring(0, ((Commands.TakeOnsCommand) message).playerDetails.playerLink.length() - 30) + "2_ATTACK_02#tabs-wrapper-anchor", false);
            if(((Commands.TakeOnsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.CornersCommand) {
            ((Commands.CornersCommand) message).document = getDocument(((Commands.CornersCommand) message).playerDetails.playerLink.substring(0, ((Commands.CornersCommand) message).playerDetails.playerLink.length() - 30) + "2_ATTACK_03#tabs-wrapper-anchor", false);
            if(((Commands.CornersCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.OffsidePassesCommand) {
            ((Commands.OffsidePassesCommand) message).document = getDocument(((Commands.OffsidePassesCommand) message).playerDetails.playerLink.substring(0, ((Commands.OffsidePassesCommand) message).playerDetails.playerLink.length() - 30) + "2_ATTACK_04#tabs-wrapper-anchor", false);
            if(((Commands.OffsidePassesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.BallRecoveriesCommand) {
            ((Commands.BallRecoveriesCommand) message).document = getDocument(((Commands.BallRecoveriesCommand) message).playerDetails.playerLink.substring(0, ((Commands.BallRecoveriesCommand) message).playerDetails.playerLink.length() - 30) + "2_ATTACK_05#tabs-wrapper-anchor", false);
            if(((Commands.BallRecoveriesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.TacklesCommand) {
            ((Commands.TacklesCommand) message).document = getDocument(((Commands.TacklesCommand) message).playerDetails.playerLink.substring(0, ((Commands.TacklesCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_01#tabs-wrapper-anchor", false);
            if(((Commands.TacklesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.InterceptionsCommand) {
            ((Commands.InterceptionsCommand) message).document = getDocument(((Commands.InterceptionsCommand) message).playerDetails.playerLink.substring(0, ((Commands.InterceptionsCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_02#tabs-wrapper-anchor", false);
            if(((Commands.InterceptionsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.BlocksCommand) {
            ((Commands.BlocksCommand) message).document = getDocument(((Commands.BlocksCommand) message).playerDetails.playerLink.substring(0, ((Commands.BlocksCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_03#tabs-wrapper-anchor", false);
            if(((Commands.BlocksCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.ClearancesCommand) {
            ((Commands.ClearancesCommand) message).document = getDocument(((Commands.ClearancesCommand) message).playerDetails.playerLink.substring(0, ((Commands.ClearancesCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_04#tabs-wrapper-anchor", false);
            if(((Commands.ClearancesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.AerialDuelsCommand) {
            ((Commands.AerialDuelsCommand) message).document = getDocument(((Commands.AerialDuelsCommand) message).playerDetails.playerLink.substring(0, ((Commands.AerialDuelsCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_06#tabs-wrapper-anchor", false);
            if(((Commands.AerialDuelsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.BlockedCrossesCommand) {
            ((Commands.BlockedCrossesCommand) message).document = getDocument(((Commands.BlockedCrossesCommand) message).playerDetails.playerLink.substring(0, ((Commands.BlockedCrossesCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_07#tabs-wrapper-anchor", false);
            if(((Commands.BlockedCrossesCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.DefensiveErrorsCommand) {
            if(((Commands.DefensiveErrorsCommand) message).index == 1)
                ((Commands.DefensiveErrorsCommand) message).document = getDocument(((Commands.DefensiveErrorsCommand) message).playerDetails.playerLink.substring(0, ((Commands.DefensiveErrorsCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_10#tabs-wrapper-anchor", false);
            else
                ((Commands.DefensiveErrorsCommand) message).document = getDocument(((Commands.DefensiveErrorsCommand) message).playerDetails.playerLink.substring(0, ((Commands.DefensiveErrorsCommand) message).playerDetails.playerLink.length() - 30) + "3_DEFENCE_09#tabs-wrapper-anchor", false);

            if(((Commands.DefensiveErrorsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
        else if(message instanceof Commands.FoulsCommand) {
            if(((Commands.FoulsCommand) message).index == 1)
                ((Commands.FoulsCommand) message).document = getDocument(((Commands.FoulsCommand) message).playerDetails.playerLink.substring(0, ((Commands.FoulsCommand) message).playerDetails.playerLink.length() - 30) + "4_FOULS_01#tabs-wrapper-anchor", false);
            else
                ((Commands.FoulsCommand) message).document = getDocument(((Commands.FoulsCommand) message).playerDetails.playerLink.substring(0, ((Commands.FoulsCommand) message).playerDetails.playerLink.length() - 30) + "4_FOULS_02#tabs-wrapper-anchor", false);

            if(((Commands.FoulsCommand) message).document == null)
                getSelf().tell(message, getSender());
            else
                Info.workerrouter.route(message, getSender());
        }
    }
}
