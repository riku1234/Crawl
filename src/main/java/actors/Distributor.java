package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import command.Commands;
import crawl.Crawl;
import fourfourtwo.Helper;
import fourfourtwo.Persistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gsm on 11/8/15.
 */
public class Distributor extends UntypedActor {
    private final Commands commands = new Commands();
    private final Crawl crawl = new Crawl();
    private ArrayList<String> blackLists = new ArrayList<String>();

    private String[] prefixes = {"2010_380/", "2011_378/", "2012_1825/", "2013_1824/", "2014_1825/"};
    private int[] numMatches = {380, 378, 1825, 1824, 1825};
    private int currentPrefixIndex = 0; private int currentMatchIndex = 0;

    private int numTrackers = 5; private int numIOWorkers = 5; private int numChildWorkers = 5;
    private ActorRef[] trackers = null;

    public static Router ioRouter = null; public static Router childRouter = null;

    public Distributor() {
        /* Add Blacklist Links */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321663"); /* Giggs appears in Subs more than once */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321693"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321780"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321789"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321801"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321845"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321848"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321887"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321902"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321835"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/322006"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/322005"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/322035"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360664"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360636"); /* No Data */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360805"); /* No Data */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360471"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360503"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360541"); /* Same player appears in 2 subs list */
        blackLists.add("http://www.fourfourtwo.com/statszone/21-2012/matches/459522"); /* No Data */
        blackLists.add("http://www.fourfourtwo.com/statszone/24-2014/matches/752029"); /* Header Problem, less tokens. */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321742"); /* Subs mismatch */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321900"); /* Subs missing */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360526"); /* Subs missing */
        blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360834"); /* Subs missing */
        blackLists.add("http://www.fourfourtwo.com/statszone/22-2012/matches/449613"); /* Subs missing */
        /* End .... */
    }

    public void onReceive(Object message) {
        if(message instanceof Commands.StartCommand) {
            List<Routee> ioRoutees = new ArrayList<>(numIOWorkers);
            for(int i=0;i<numIOWorkers;i++) {
                ActorRef ioWorker = getContext().actorOf(Props.create(IO.class), "IO-" + i);
                getContext().watch(ioWorker);
                ioRoutees.add(new ActorRefRoutee(ioWorker));
            }
            ioRouter = new Router(new SmallestMailboxRoutingLogic(), ioRoutees);

            List<Routee> childRoutees = new ArrayList<>(numChildWorkers);
            for(int i=0;i<numChildWorkers;i++) {
                ActorRef childWorker = getContext().actorOf(Props.create(Child.class), "Child-" + i);
                getContext().watch(childWorker);
                childRoutees.add(new ActorRefRoutee(childWorker));
            }
            childRouter = new Router(new SmallestMailboxRoutingLogic(), childRoutees);

            for(int i=0;i<numTrackers;i++) {
                trackers[i] = getContext().actorOf(Props.create(Tracker.class), "Tracker-" + i);
                getContext().watch(trackers[i]);
                trackers[i].tell("Setup", getSelf());
            }
        }
        else if(message instanceof String) {
            if(message.equals("NextMatch")) {
                this.sendWork(getSender());
            }
            else {
                crawl.cleanTerminate("Strange Error inside onReceive of Distributor. Exiting.");
            }
        }
        else if(message instanceof Commands.MatchGlobals) {
            if(!crawl.populateGameDetails((Commands.MatchGlobals) message)) {
                System.out.println("Skipping Game " + ((Commands.MatchGlobals) message).getGameLink());
                getSelf().tell(message, getSender()); return;
            }

            List<Elements> playerLinks = crawl.getPlayerStatsLink(((Commands.MatchGlobals) message).getPlayersDocument());
            if(playerLinks == null) {
                System.out.println("Skipping Game " + ((Commands.MatchGlobals) message).getGameLink());
                getSelf().tell(message, getSender()); return;
            }

            ((Commands.MatchGlobals) message).setNumMessagesRemaining(33 * (playerLinks.get(0).size() + playerLinks.get(1).size() + playerLinks.get(2).size() + playerLinks.get(4).size()));

            for (int j = 0; j < playerLinks.size(); j++) {
                Boolean noTask = false;
                if(j == 3 || j == 5)
                    noTask = true;

                if (!noTask) {
                    Elements temp = playerLinks.get(j);
                    for (int k = 0; k < temp.size(); k++) {
                        String playerLink = temp.get(k).attr("abs:href");
                        //this.getPlayerDetails(playerLink, j, sender);
                        Commands.PlayerDetails playerDetails = commands.new PlayerDetails((Commands.MatchGlobals)message, playerLink, j);
                        ioRouter.route(playerDetails, getSender());
                    }
                }
            }

            for (int j = 0; j < playerLinks.get(2).size(); j++) {
                String homeTeamSubInLink = playerLinks.get(2).get(j).attr("abs:href");
                String homeTeamSubOutLink = playerLinks.get(3).get(j).attr("abs:href");

                ((Commands.MatchGlobals) message).homeSubstitutions.put(homeTeamSubInLink, homeTeamSubOutLink);
            }

            for (int j = 0; j < playerLinks.get(4).size(); j++) {
                String awayTeamSubInLink = playerLinks.get(4).get(j).attr("abs:href");
                String awayTeamSubOutLink = playerLinks.get(5).get(j).attr("abs:href");

                ((Commands.MatchGlobals) message).awaySubstitutions.put(awayTeamSubInLink, awayTeamSubOutLink);
            }
        }
        else if(message instanceof Commands.PlayerDetails) {
            if(!this.getPlayerDetails((Commands.PlayerDetails) message, getSender())) {
                crawl.cleanTerminate("Skipping game = " + ((Commands.PlayerDetails) message).matchGlobals.getFFT_Match_ID());
            }
        }
    }

    private void sendWork(ActorRef tracker) {
        if(currentMatchIndex >= numMatches[currentPrefixIndex]) {
            currentMatchIndex = 0;
            currentPrefixIndex++;
        }
        if(currentPrefixIndex >= prefixes.length) {
            System.out.println("Nothing more to do. ");
            getContext().unwatch(tracker); getContext().stop(tracker);
        }
        else {
            String prefix = prefixes[currentPrefixIndex];
            try {
                JSONParser jsonParser = new JSONParser();

                JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(prefix + currentMatchIndex));
                DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Helper.getLocale((Long) jsonObject.get("LeagueID")));
                String gameLink = (String) jsonObject.get("GameLink");
                String stadium = (String) jsonObject.get("Stadium");
                String[] splits = gameLink.split("/");
                String FFT_Match_ID = splits[splits.length - 1];
                if (blackLists.contains(gameLink)) {
                    //System.out.println("Game " + gameLink + " is Blacklisted. Skipping.");
                    Persistence.deleteMatch(FFT_Match_ID);
                    getSelf().tell("NextMatch", tracker);
                    return;
                }

                if(Persistence.gameExists(FFT_Match_ID)) {
                    getSelf().tell("NextMatch", tracker);
                    return;
                }
                Long leagueID = (Long) jsonObject.get("LeagueID");
                //Helper.setLeagueID((Long) jsonObject.get("LeagueID"));
                //Persistence.addLeague();
                String season = (String) jsonObject.get("Season");
                Date gameDate = df.parse((String) jsonObject.get("Date"));
                Commands.MatchGlobals matchGlobals = commands.new MatchGlobals(gameLink, leagueID, FFT_Match_ID, season, gameDate, stadium);
                ioRouter.route(matchGlobals, tracker);
                //tracker.tell(commands.new StartCommand(prefixes[currentPrefixIndex], currentMatchIndex), getSelf());
            } catch (FileNotFoundException e) {
                System.out.println("File " + prefix + currentMatchIndex + " not found. Doing nothing.");
                return;
            } catch (ParseException e) {
                System.out.println("Could not parse file " + prefix + currentMatchIndex + " Doing nothing.");
                return;
            } catch (org.json.simple.parser.ParseException e) {
                System.out.println("Could not parse file " + prefix + currentMatchIndex + " Doing nothing.");
                return;
            } catch (IOException e) {
                System.out.println("IO Exception for file " + prefix + currentMatchIndex + " Doing nothing.");
                return;
            }
        }
    }

    private boolean getPlayerDetails(Commands.PlayerDetails playerDetails, ActorRef sender) {
        //System.out.println("Player Link = " + playerLink);
        String[] playerLinkSplits = playerDetails.playerLink.split("/");
        String FFT_player_id = playerLinkSplits[8];

        Document doc = playerDetails.playerDocument; String team_name = doc.select("div.team-name").get(0).text();

        String playerName = doc.select("div#statzone_player_header h1").get(0).text();

        playerDetails.FFT_player_id = FFT_player_id; playerDetails.team_name = team_name;

        if(!Persistence.addPlayer(team_name, playerName, FFT_player_id, playerDetails.matchGlobals.getFFT_Match_ID(), playerDetails.matchGlobals.getGameDate())) {
            System.out.println("Add Player failed for Team = " + team_name + " Player = " + playerName + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
            return false;
        }

        String[] playerNameSplits = playerName.split(" ");
        switch(playerDetails.j) {
            case 0:
                if(!Persistence.addStartingXIs(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason())) {
                    System.out.println("Add Starting XI Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                    return false;
                }
                for(String s : playerNameSplits) {
                    if(playerDetails.matchGlobals.getHomeRedCards().containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = playerDetails.matchGlobals.getHomeRedCards().get(s);
                        playerDetails.matchGlobals.getHomeRedCards().remove(s);
                        if(!Persistence.addRedCard(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason(), time)) {
                            System.out.println("Add Home Red Card Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                            return false;
                        }
                    }
                }
                break;
            case 1:
                if(!Persistence.addStartingXIs(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason())) {
                    System.out.println("Add Starting XI Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                    return false;
                }
                for(String s : playerNameSplits) {
                    if(playerDetails.matchGlobals.getAwayRedCards().containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = playerDetails.matchGlobals.getAwayRedCards().get(s);
                        playerDetails.matchGlobals.getAwayRedCards().remove(s);
                        if(!Persistence.addRedCard(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason(), time)) {
                            System.out.println("Add Away Red Card Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                            return false;
                        }
                    }
                }
                break;
            case 2:
                if(!Persistence.addSUBs(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason())) {
                    System.out.println("Add SUB Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                    return false;
                }
                for(String s : playerNameSplits) {
                    if(playerDetails.matchGlobals.getHomeRedCards().containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = playerDetails.matchGlobals.getHomeRedCards().get(s);
                        playerDetails.matchGlobals.getHomeRedCards().remove(s);
                        if(!Persistence.addRedCard(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason(), time)) {
                            System.out.println("Add Home Red Card Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                            return false;
                        }
                    }
                }
                break;
            case 4:
                if(!Persistence.addSUBs(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason())) {
                    System.out.println("Add SUB Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                    return false;
                }
                for(String s : playerNameSplits) {
                    if(playerDetails.matchGlobals.getAwayRedCards().containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = playerDetails.matchGlobals.getAwayRedCards().get(s);
                        playerDetails.matchGlobals.getAwayRedCards().remove(s);
                        if(!Persistence.addRedCard(playerDetails.matchGlobals.getFFT_Match_ID(), team_name, FFT_player_id, playerDetails.matchGlobals.getSeason(), time)) {
                            System.out.println("Add Away Red Card Failed for Player ID = " + FFT_player_id + " Team = " + team_name + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
                            return false;
                        }
                    }
                }
                break;
            default:
                System.out.println("Strange Error in getPlayerDocument.");
                return false;
        }

        ioRouter.route(commands.new ShotsCommand(playerDetails, 1), getSender());
        ioRouter.route(commands.new ShotsCommand(playerDetails, 2), getSender());
        ioRouter.route(commands.new ShotsCommand(playerDetails, 3), getSender());
        ioRouter.route(commands.new ShotsCommand(playerDetails, 4), getSender());
        ioRouter.route(commands.new PassesCommand(playerDetails, 1), getSender());
        ioRouter.route(commands.new PassesCommand(playerDetails, 2), getSender());
        ioRouter.route(commands.new PassesCommand(playerDetails, 3), getSender());
        ioRouter.route(commands.new AssistsCommand(playerDetails, 1), getSender());
        ioRouter.route(commands.new AssistsCommand(playerDetails, 2), getSender());
        ioRouter.route(commands.new ReceivedPassesCommand(playerDetails), getSender());
        ioRouter.route(commands.new ChancesCreatedCommand(playerDetails, 1), getSender());
        ioRouter.route(commands.new ChancesCreatedCommand(playerDetails, 2), getSender());
        ioRouter.route(commands.new LongPassesCommand(playerDetails), getSender());
        ioRouter.route(commands.new ShortPassesCommand(playerDetails), getSender());
        ioRouter.route(commands.new CrossesCommand(playerDetails), getSender());
        ioRouter.route(commands.new TakeOnsCommand(playerDetails), getSender());
        ioRouter.route(commands.new CornersCommand(playerDetails), getSender());
        ioRouter.route(commands.new OffsidePassesCommand(playerDetails), getSender());
        ioRouter.route(commands.new BallRecoveriesCommand(playerDetails), getSender());
        ioRouter.route(commands.new TacklesCommand(playerDetails), getSender());
        ioRouter.route(commands.new InterceptionsCommand(playerDetails), getSender());
        ioRouter.route(commands.new BlocksCommand(playerDetails), getSender());
        ioRouter.route(commands.new ClearancesCommand(playerDetails), getSender());
        ioRouter.route(commands.new AerialDuelsCommand(playerDetails), getSender());
        ioRouter.route(commands.new BlockedCrossesCommand(playerDetails), getSender());
        ioRouter.route(commands.new DefensiveErrorsCommand(playerDetails, 1), getSender());
        ioRouter.route(commands.new DefensiveErrorsCommand(playerDetails, 2), getSender());
        ioRouter.route(commands.new FoulsCommand(playerDetails, 1), getSender());
        ioRouter.route(commands.new FoulsCommand(playerDetails, 2), getSender());

        return true;
    }
}