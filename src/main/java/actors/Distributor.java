package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
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

import java.io.*;
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
    public static Router ioRouter = null;
    public static Router childRouter = null;
    public static ActorRef perfActor = null;
    private final Commands commands = new Commands();
    private final Crawl crawl = new Crawl();
    private final int num_cores = Runtime.getRuntime().availableProcessors();
    private final int numTrackers = (int) Math.ceil(num_cores * 2.5);
    private final int numTORProxies = 10;
    private final int numIOWorkers = Math.max(numTORProxies, numTrackers);
    private final int numChildWorkers = numTrackers;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ArrayList<String> blackLists = new ArrayList<String>();
    private String[] prefixes = {"2010_490/", "2011_497/", "2012_1949/", "2013_1951/", "2014_1950/"};
    private int[] numMatches = {490, 497, 1949, 1951, 1950};
    private int currentPrefixIndex = 0;
    private int currentMatchIndex = -1;
    private ActorRef[] trackers = null;
    private Boolean stopActorSystem = false;

    public Distributor() {
        /* Add Blacklist Links */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("../blacklists.txt")));
            String line;
            while ((line = br.readLine()) != null)
                blackLists.add(line);
            br.close();
            br = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onReceive(Object message) {
        if(perfActor != null)
            perfActor.tell("Distributor", getSelf());
        if(message instanceof Commands.StartCommand) {
            System.out.println("Number of TOR Proxies = " + numTORProxies);
            //log.info("Start message received by Distributor. Setting up actors.");
            System.out.println("Number of IO Workers = " + numIOWorkers);
            List<Routee> ioRoutees = new ArrayList<>(numIOWorkers);
            for(int i=0;i<numIOWorkers;i++) {
                ActorRef ioWorker = getContext().actorOf(Props.create(IO.class), "IO-" + i);
                ioWorker.tell("Setup-" + (i % numTORProxies), getSelf());
                getContext().watch(ioWorker);
                ioRoutees.add(new ActorRefRoutee(ioWorker));
            }
            ioRouter = new Router(new SmallestMailboxRoutingLogic(), ioRoutees);

            System.out.println("Number of Child Workers = " + numChildWorkers);
            List<Routee> childRoutees = new ArrayList<>(numChildWorkers);
            for(int i=0;i<numChildWorkers;i++) {
                ActorRef childWorker = getContext().actorOf(Props.create(Child.class), "Child-" + i);
                getContext().watch(childWorker);
                childRoutees.add(new ActorRefRoutee(childWorker));
            }
            childRouter = new Router(new SmallestMailboxRoutingLogic(), childRoutees);

            System.out.println("Number of Tracker Workers = " + numTrackers);
            trackers = new ActorRef[numTrackers];
            for(int i=0;i<numTrackers;i++) {
                trackers[i] = getContext().actorOf(Props.create(Tracker.class), "Tracker-" + i);
                getContext().watch(trackers[i]);
                trackers[i].tell("Setup", getSelf());
            }

            perfActor = getContext().actorOf(Props.create(Perf.class).withDispatcher("PerfDispatcher"), "Perf");
            perfActor.tell("Setup-" + numTORProxies, getSelf());
            JSONObject sysConfObject = new JSONObject();
            sysConfObject.put("CORES", num_cores);
            sysConfObject.put("NUM_TOR_PROXIES", numTORProxies);
            sysConfObject.put("NUM_IO_WORKERS", numIOWorkers);
            sysConfObject.put("NUM_CHILD_WORKERS", numChildWorkers);
            sysConfObject.put("NUM_TRACKER_WORKERS", numTrackers);
            perfActor.tell(sysConfObject, getSelf());

        }
        else if(message instanceof String) {
            if(message.equals("NextMatch")) {
                log.info("Next Match request received from tracker = " + getSender().path());
                if (!stopActorSystem)
                    this.sendWork(getSender());
                else
                    System.out.println("Stop Request Received. Not Sending more work....");
            } else if (message.equals("Stop")) {
                stopActorSystem = true;
            }
            else {
                crawl.cleanTerminate("Strange Error inside onReceive of Distributor. Exiting.");
            }
        }
        else if(message instanceof Commands.MatchGlobals) {
            //log.info("Match Globals request received by Distributor.");
            if(!crawl.populateGameDetails((Commands.MatchGlobals) message)) {
                crawl.cleanTerminate("Populate Game Details failed for GameLink = " + ((Commands.MatchGlobals) message).getGameLink(), ((Commands.MatchGlobals) message).getFFT_Match_ID(), getSelf());
                return;
            }

            List<Elements> playerLinks = crawl.getPlayerStatsLink(((Commands.MatchGlobals) message).getPlayersDocument());
            if(playerLinks == null || playerLinks.size() != 6) {
                crawl.cleanTerminate("PlayerLinks is null. Error.", ((Commands.MatchGlobals) message).getFFT_Match_ID(), getSelf());
                return;
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
                        //log.info("Player Link = " + playerLink);
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
            //log.info("Player Details request received by Distributor.");
            if(!this.getPlayerDetails((Commands.PlayerDetails) message)) {
                crawl.cleanTerminate("Skipping game = " + ((Commands.PlayerDetails) message).matchGlobals.getFFT_Match_ID(), ((Commands.PlayerDetails) message).matchGlobals.getFFT_Match_ID(), getSelf());
                return;
            }
        }
    }

    private void sendWork(ActorRef tracker) {
        long free_memory_before_gc = Runtime.getRuntime().freeMemory();
        Runtime.getRuntime().gc();
        long free_memory_after_gc = Runtime.getRuntime().freeMemory();
        System.out.println("Memory freed by gc = " + (free_memory_after_gc - free_memory_before_gc));
        currentMatchIndex++;

        if(currentPrefixIndex < prefixes.length && currentMatchIndex >= numMatches[currentPrefixIndex]) {
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
                    log.info("Game " + gameLink + " is Blacklisted. Skipping.");
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
                Persistence.addLeague(leagueID);
                String season = (String) jsonObject.get("Season");
                Date gameDate = df.parse((String) jsonObject.get("Date"));
                Commands.MatchGlobals matchGlobals = commands.new MatchGlobals(gameLink, leagueID, FFT_Match_ID, season, gameDate, stadium);
                log.info("Starting GameLink = " + gameLink);
                ioRouter.route(matchGlobals, tracker);
            } catch (FileNotFoundException e) {
                crawl.cleanTerminate("File " + prefix + currentMatchIndex + " not found.");
            } catch (ParseException e) {
                crawl.cleanTerminate("Could not parse file " + prefix + currentMatchIndex + " Doing nothing.");
            } catch (org.json.simple.parser.ParseException e) {
                crawl.cleanTerminate("Could not parse file " + prefix + currentMatchIndex + " Doing nothing.");
            } catch (IOException e) {
                crawl.cleanTerminate("IO Exception for file " + prefix + currentMatchIndex + " Doing nothing.");
            }
        }
    }

    private boolean getPlayerDetails(Commands.PlayerDetails playerDetails) {
        //System.out.println("Player Link = " + playerLink);
        String[] playerLinkSplits = playerDetails.playerLink.split("/");
        String FFT_player_id = playerLinkSplits[8];

        Document doc = playerDetails.playerDocument; String team_name = doc.select("div.team-name").get(0).text();

        String playerName = doc.select("div#statzone_player_header h1").get(0).text();

        playerDetails.FFT_player_id = FFT_player_id; playerDetails.team_name = team_name;

        if(!Persistence.addPlayer(team_name, playerName, FFT_player_id, playerDetails.matchGlobals.getFFT_Match_ID(), playerDetails.matchGlobals.getGameDate())) {
            System.out.println("Add Player failed for Team = " + team_name + " Player = " + playerName + " MatchID = " + playerDetails.matchGlobals.getFFT_Match_ID());
            System.out.println("Player Link = " + playerDetails.playerLink);
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