package command;

import crawl.Crawl;
import org.jsoup.nodes.Document;
import scala.collection.script.Start;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gsm on 9/11/15.
 */

public class Commands {
    public Commands() {

    }

    //private static volatile Commands oneCommands = null;
/*
    public static Commands getCommandsInstance() {
        if(oneCommands == null) {
            synchronized (Commands.class) {
                if(oneCommands == null)
                    oneCommands = new Commands();
            }
        }
        return oneCommands;
    }
*/
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

    public class PenaltiesCommand implements Serializable {
        public PlayerDetails playerDetails;
        public ArrayList<String> penalties;
        public Document document;

        public PenaltiesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.penalties = null;
            this.document = null;
        }
    }

    public class FreekickshotsCommand implements Serializable {
        public PlayerDetails playerDetails;
        public ArrayList<String> freekickshots;
        public Document document;

        public FreekickshotsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.freekickshots = null;
            this.document = null;
        }
    }
    public class ShotsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> shots;
        public Document document;
        public int index;

        public ShotsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.shots = null;
            this.document = null;
            this.index = index;
        }
    }

    public class PassesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> passes;
        public Document document;
        public int index;

        public PassesCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.passes = null;
            this.index = index;
        }
    }

    public class AssistsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> assists;
        public Document document;
        public int index;

        public AssistsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.assists = null;
            this.index = index;
        }
    }

    public class ReceivedPassesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> receivedpasses;
        public Document document;

        public ReceivedPassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.receivedpasses = null;
        }
    }

    public class ChancesCreatedCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> chancescreated;
        public Document document;
        public int index;

        public ChancesCreatedCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.chancescreated = null;
            this.index = index;
        }
    }

    public class LongPassesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public String longpasses;
        public Document document;

        public LongPassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.longpasses = null;
        }
    }

    public class ShortPassesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public String shortpasses;
        public Document document;

        public ShortPassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.shortpasses = null;
        }
    }

    public class CrossesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> crosses;
        public Document document;

        public CrossesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.crosses = null;
        }
    }

    public class TakeOnsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> takeons;
        public Document document;

        public TakeOnsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.takeons = null;
        }
    }

    public class CornersCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> corners;
        public Document document;

        public CornersCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.corners = null;
        }
    }

    public class OffsidePassesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> offsidepasses;
        public Document document;

        public OffsidePassesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.offsidepasses = null;
        }
    }

    public class BallRecoveriesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> ballrecoveries;
        public Document document;

        public BallRecoveriesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.ballrecoveries = null;
        }
    }

    public class TacklesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> tackles;
        public Document document;

        public TacklesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.tackles = null;
        }
    }

    public class InterceptionsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> interceptions;
        public Document document;

        public InterceptionsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.interceptions = null;
        }
    }

    public class BlocksCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> blocks;
        public Document document;

        public BlocksCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.blocks = null;
        }
    }

    public class ClearancesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> clearances;
        public Document document;

        public ClearancesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.clearances = null;
        }
    }

    public class AerialDuelsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> aerialduels;
        public Document document;

        public AerialDuelsCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.aerialduels = null;
        }
    }

    public class BlockedCrossesCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> blockedcrosses;
        public Document document;

        public BlockedCrossesCommand(PlayerDetails playerDetails) {
            this.playerDetails = playerDetails;
            this.blockedcrosses = null;
        }
    }

    public class DefensiveErrorsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> defensiveerrors;
        public Document document;
        public int index;

        public DefensiveErrorsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.defensiveerrors = null;
            this.index = index;
        }
    }

    public class FoulsCommand implements Serializable{
        public PlayerDetails playerDetails;
        public ArrayList<String> fouls;
        public Document document;
        public int index;

        public FoulsCommand(PlayerDetails playerDetails, int index) {
            this.playerDetails = playerDetails;
            this.fouls = null;
            this.index = index;
        }
    }
}