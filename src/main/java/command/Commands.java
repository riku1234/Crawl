package command;

import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by gsm on 9/11/15.
 */

public class Commands implements Serializable{
    public Commands() {

    }

    public class MatchGlobals implements Serializable {
        public Long leagueID;
        public int numMessagesRemaining;
        public HashMap<String, String> homeSubstitutions;
        public HashMap<String, String> awaySubstitutions;
        private String gameLink;
        private String FFT_Match_ID;
        private String season;
        private Date gameDate;
        private Document gameDocument;
        private Document playersDocument;
        private String stadium;
        private String home_team_name;
        private String away_team_name;
        private double home_possession;
        private double away_possession;
        private HashMap<String, String> home_red_cards;
        private HashMap<String, String> away_red_cards;
        private String fullTimeScore;

        public MatchGlobals(String gameLink, Long leagueID, String FFT_Match_ID, String season, Date gameDate, String stadium) {
            this.gameLink = gameLink;
            this.leagueID = leagueID;
            this.FFT_Match_ID = FFT_Match_ID;
            this.season = season;
            this.gameDate = gameDate;
            this.stadium = stadium;
            this.gameDocument = null; this.playersDocument = null;
            this.home_team_name = ""; this.away_team_name = ""; this.home_possession = 0; this.away_possession = 0;
            this.home_red_cards = new HashMap<>(); this.away_red_cards = new HashMap<>();
            this.fullTimeScore = "";
            this.numMessagesRemaining = 0;
            this.homeSubstitutions = new HashMap<>();
            this.awaySubstitutions = new HashMap<>();
        }

        public Document getGameDocument() {
            return this.gameDocument;
        }

        public void setGameDocument(Document gameDocument) {
            this.gameDocument = gameDocument;
        }

        public Document getPlayersDocument() {
            return this.playersDocument;
        }

        public void setPlayersDocument(Document playersDocument) {
            this.playersDocument = playersDocument;
        }

        public String getGameLink() {
            return this.gameLink;
        }

        public String getStadium() {
            return this.stadium;
        }

        public void setHome_team_name(String home_team_name) {
            this.home_team_name = home_team_name;
        }

        public void setAway_team_name(String away_team_name) {
            this.away_team_name = away_team_name;
        }

        public void setHome_possession(double home_possession) {
            this.home_possession = home_possession;
        }

        public void setAway_possession(double away_possession) {
            this.away_possession = away_possession;
        }

        public void addHomeRedCard(String name, String time) {
            this.home_red_cards.put(name, time);
        }

        public void addAwayRedCard(String name, String time) {
            this.away_red_cards.put(name, time);
        }

        public void setFullTimeScore(String fullTimeScore) {
            this.fullTimeScore = fullTimeScore;
        }

        public Date getGameDate() {
            return this.gameDate;
        }

        public String getFFT_Match_ID() {
            return this.FFT_Match_ID;
        }

        public String getSeason() {
            return this.season;
        }

        public int getNumMessagesRemaining() {
            return this.numMessagesRemaining;
        }

        public void setNumMessagesRemaining(int numMessagesRemaining) {
            this.numMessagesRemaining = numMessagesRemaining;
        }

        public HashMap<String, String> getHomeRedCards() {
            return home_red_cards;
        }

        public HashMap<String, String> getAwayRedCards() {
            return away_red_cards;
        }
    }

    public class Global implements Serializable {
        public Commands.PlayerDetails playerDetails;
        public Document document;
        public String commandLink;
    }

    public class PlayerDetails implements Serializable{
        public MatchGlobals matchGlobals;
        public String playerLink;
        public String FFT_player_id;
        public String team_name;
        public Document playerDocument;
        public int j;

        public PlayerDetails(MatchGlobals matchGlobals, String playerLink, int j) {
            this.playerLink = playerLink;
            this.matchGlobals = matchGlobals;
            this.j = j;
            this.FFT_player_id = "";
            this.team_name = "";
            this.playerDocument = null;
        }
    }

    public class StartCommand implements Serializable {

        public int num_trackers;
        public int num_child;
        public int num_io;
        public int num_tor;

        public StartCommand(int n_trackers, int n_child, int n_io, int n_tor) {
            this.num_trackers = n_trackers;
            this.num_child = n_child;
            this.num_io = n_io;
            this.num_tor = n_tor;
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