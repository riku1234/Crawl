package command;

import akka.routing.Router;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gsm on 9/11/15.
 */

public class Commands implements Serializable{
    public Commands() {

    }

    public class RemoteSetup implements Serializable {
        public Router workerRouter;

        public RemoteSetup(Router workerRouter) {
            this.workerRouter = workerRouter;
        }
    }

    public class Global implements Serializable {
        public Commands.PlayerDetails playerDetails;
        public Document document;
        public String commandLink;
    }

    public class PlayerDetails implements Serializable{
        public String playerLink;
        public String FFT_player_id;
        public String team_name;
        public int numShotsComplete;
        public int numPassesComplete;

        public PlayerDetails(String playerLink, String FFT_player_id, String team_name) {
            this.team_name = team_name;
            this.FFT_player_id = FFT_player_id;
            this.playerLink = playerLink;
            this.numShotsComplete = 0;
            this.numPassesComplete = 0;
        }
    }

    public class StartCommand implements Serializable{
        public int j;

        public StartCommand(int j) {
            this.j = j;
        }
    }

    public class SkipGameCommand implements Serializable{
        public SkipGameCommand() {

        }
    }

    public class PenaltiesCommand extends Global implements Serializable {
        //public PlayerDetails playerDetails;
        public ArrayList<String> penalties;
        //public Document document;

        public PenaltiesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.penalties = null;
            this.document = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_09#tabs-wrapper-anchor";
        }
    }

    public class FreekickshotsCommand extends Global implements Serializable {
        //public PlayerDetails playerDetails;
        public ArrayList<String> freekickshots;
        //public Document document;

        public FreekickshotsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.freekickshots = null;
            this.document = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_07#tabs-wrapper-anchor";
        }
    }
    public class ShotsCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> shots;
        //public Document document;
        public int index;

        public ShotsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.shots = null;
            this.document = null;
            this.index = index;
            switch(index) {
                case 1:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_12#tabs-wrapper-anchor";
                    break;
                case 2:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_13#tabs-wrapper-anchor";
                    break;
                case 3:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_11#tabs-wrapper-anchor";
                    break;
                case 4:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_14#tabs-wrapper-anchor";
                    break;
                case 5:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_08#tabs-wrapper-anchor";
                    break;
            }
        }
    }

    public class PassesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> passes;
        //public Document document;
        public int index;

        public PassesCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.passes = null;
            this.index = index;
            switch (index) {
                case 1:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_08#tabs-wrapper-anchor";
                    break;
                case 2:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0802#tabs-wrapper-anchor";
                    break;
                case 3:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0801#tabs-wrapper-anchor";
                    break;
                case 4:
                    this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_113#tabs-wrapper-anchor";
                    break;
            }
        }
    }

    public class AssistsCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> assists;
        //public Document document;
        public int index;

        public AssistsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.assists = null;
            this.index = index;
            if(index == 1)
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0901#tabs-wrapper-anchor";
            else
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0902#tabs-wrapper-anchor";
        }
    }

    public class ReceivedPassesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> receivedpasses;
        //public Document document;

        public ReceivedPassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.receivedpasses = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_07#tabs-wrapper-anchor";
        }
    }

    public class ChancesCreatedCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> chancescreated;
        //public Document document;
        public int index;

        public ChancesCreatedCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.chancescreated = null;
            this.index = index;
            if(index == 1)
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_1101#tabs-wrapper-anchor";
            else
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_1102#tabs-wrapper-anchor";
        }
    }

    public class LongPassesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public String longpasses;
        //public Document document;

        public LongPassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.longpasses = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_12#tabs-wrapper-anchor";
        }
    }

    public class ShortPassesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public String shortpasses;
        //public Document document;

        public ShortPassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.shortpasses = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_13#tabs-wrapper-anchor";
        }
    }

    public class CrossesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> crosses;
        //public Document document;

        public CrossesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.crosses = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "2_ATTACK_01#tabs-wrapper-anchor";
        }
    }

    public class TakeOnsCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> takeons;
        //public Document document;

        public TakeOnsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.takeons = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "2_ATTACK_02#tabs-wrapper-anchor";
        }
    }

    public class CornersCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> corners;
        //public Document document;

        public CornersCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.corners = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "2_ATTACK_03#tabs-wrapper-anchor";
        }
    }

    public class OffsidePassesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> offsidepasses;
        //public Document document;

        public OffsidePassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.offsidepasses = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "2_ATTACK_04#tabs-wrapper-anchor";
        }
    }

    public class BallRecoveriesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> ballrecoveries;
        //public Document document;

        public BallRecoveriesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.ballrecoveries = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "2_ATTACK_05#tabs-wrapper-anchor";
        }
    }

    public class TacklesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> tackles;
        //public Document document;

        public TacklesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.tackles = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_01#tabs-wrapper-anchor";
        }
    }

    public class InterceptionsCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> interceptions;
        //public Document document;

        public InterceptionsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.interceptions = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_02#tabs-wrapper-anchor";
        }
    }

    public class BlocksCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> blocks;
        //public Document document;

        public BlocksCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.blocks = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_03#tabs-wrapper-anchor";
        }
    }

    public class ClearancesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> clearances;
        //public Document document;

        public ClearancesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.clearances = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_04#tabs-wrapper-anchor";
        }
    }

    public class AerialDuelsCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> aerialduels;
        //public Document document;

        public AerialDuelsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.aerialduels = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_06#tabs-wrapper-anchor";
        }
    }

    public class BlockedCrossesCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> blockedcrosses;
        //public Document document;

        public BlockedCrossesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.blockedcrosses = null;
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_07#tabs-wrapper-anchor";
        }
    }

    public class DefensiveErrorsCommand extends Global implements Serializable{
        //public PlayerDetails playerDetails;
        public ArrayList<String> defensiveerrors;
        //public Document document;
        public int index;

        public DefensiveErrorsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.defensiveerrors = null;
            this.index = index;
            if(index == 1)
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_10#tabs-wrapper-anchor";
            else
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_09#tabs-wrapper-anchor";
        }
    }

    public class FoulsCommand extends Global implements Serializable{
        ////public PlayerDetails playerDetails;
        public ArrayList<String> fouls;
        //public Document document;
        public int index;

        public FoulsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.fouls = null;
            this.index = index;
            if(index == 1)
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "4_FOULS_01#tabs-wrapper-anchor";
            else
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "4_FOULS_02#tabs-wrapper-anchor";
        }
    }

    public class WorkerRoute implements Serializable {
        public Object object;

        public WorkerRoute(Object object) {
            this.object = object;
        }
    }
}