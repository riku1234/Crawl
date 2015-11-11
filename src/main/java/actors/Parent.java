package actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import command.Commands;
import crawl.Crawl;
import crawl.MyDocument;
import fourfourtwo.Helper;
import fourfourtwo.Persistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by gsm on 9/10/15.
 */
public class Parent extends UntypedActor{
    Commands commands = new Commands();
    Crawl crawl = new Crawl();
    ArrayList<String> blackLists = new ArrayList<String>();

    public Parent() {
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

    @Override
    public void onReceive(Object message) throws Exception {

        if(message instanceof Commands.StartCommand) {
            //System.out.println("Parent Called ... Dispatcher = " + getContext().dispatcher().toString());
            this.startProcess(getSender(), ((Commands.StartCommand) message).j);
        }
    }

    private void startProcess(ActorRef sender, int index) throws IOException, ParseException, java.text.ParseException {
        JSONParser jsonParser = new JSONParser();

        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(Info.prepend + index));
        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Helper.getLocale((Long) jsonObject.get("LeagueID")));
        String gameLink = (String) jsonObject.get("GameLink");

        if (blackLists.contains(gameLink)) {
            //System.out.println("Game " + gameLink + " is Blacklisted. Skipping.");
            String[] splits = gameLink.split("/");
            Persistence.deleteMatch(splits[splits.length-1]);
            sender.tell(commands.new SkipGameCommand(), getSelf());
            return;
        }
        Helper.setLeagueID((Long) jsonObject.get("LeagueID"));
        Persistence.addLeague();
        Info.season = (String) jsonObject.get("Season");
        Info.match_date = df.parse((String) jsonObject.get("Date"));

        if (!crawl.populateGameDetails(gameLink, (String) jsonObject.get("Stadium"))) {
            //System.out.println("Skipping Game - " + gameLink);
            sender.tell(commands.new SkipGameCommand(), getSelf());
            return;
        }

        System.out.println("Starting Game - " + gameLink);
        String playerStatsPage = gameLink + "/player-stats#tabs-wrapper-anchor";
        List<Elements> playerLinks = crawl.getPlayerStatsLink(playerStatsPage);

        if(playerLinks.get(2).size() != playerLinks.get(3).size())
            crawl.cleanTerminate("Home subs size not equal. Exiting.");
        if(playerLinks.get(4).size() != playerLinks.get(5).size())
            crawl.cleanTerminate("Away subs size not equal. Exiting.");

        Info.numMessages = (33 * (playerLinks.get(0).size() + playerLinks.get(1).size() + playerLinks.get(2).size() + playerLinks.get(4).size()));
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "Starting Game = " + gameLink + " Date = " + Info.match_date + " Season = " + Info.season + " NumMessages = " + Info.numMessages);
        for (int j = 0; j < playerLinks.size(); j++) {
            Boolean noTask = false;
            if(j == 3 || j == 5)
                noTask = true;

            if (!noTask) {
                Elements temp = playerLinks.get(j);
                for (int k = 0; k < temp.size(); k++) {
                    String playerLink = temp.get(k).attr("abs:href");
                    this.getPlayerDetails(playerLink, j, sender);
                }
            }
        }

        for (int j = 0; j < playerLinks.get(2).size(); j++) {
            String homeTeamSubInLink = playerLinks.get(2).get(j).attr("abs:href");
            String homeTeamSubOutLink = playerLinks.get(3).get(j).attr("abs:href");
            if (!crawl.addSubstitutions(homeTeamSubInLink, homeTeamSubOutLink))
                crawl.cleanTerminate("Home Substitutions could not be added. Error.");
        }

        for (int j = 0; j < playerLinks.get(4).size(); j++) {
            String awayTeamSubInLink = playerLinks.get(4).get(j).attr("abs:href");
            String awayTeamSubOutLink = playerLinks.get(5).get(j).attr("abs:href");
            if (!crawl.addSubstitutions(awayTeamSubInLink, awayTeamSubOutLink))
                crawl.cleanTerminate("Away Substitutions could not be added. Error.");
        }
    }

    private void getPlayerDetails(String playerLink, int j, ActorRef sender) throws IOException {
        //System.out.println("Player Link = " + playerLink);
        String[] playerDetails = playerLink.split("/");
        Info.FFT_match_id = playerDetails[6]; String FFT_player_id = playerDetails[8];

        MyDocument doc = (MyDocument)crawl.getDocument(playerLink); String team_name = doc.document.select("div.team-name").get(0).text();
        Commands.PlayerDetails playerDetailsObj = commands.new PlayerDetails(playerLink, FFT_player_id, team_name);
        String playerName = doc.document.select("div#statzone_player_header h1").get(0).text();

        if(!Persistence.addPlayer(team_name, playerName, FFT_player_id, Info.FFT_match_id, Info.match_date))
            crawl.cleanTerminate("Add Player Failed in Persistence.");

        String[] playerNameSplits = playerName.split(" ");
        switch(j) {
            case 0:
                if(!Persistence.addStartingXIs(Info.FFT_match_id, team_name, FFT_player_id, Info.season))
                    crawl.cleanTerminate("Add Starting XI Home Failed in Persistence.");
                for(String s : playerNameSplits) {
                    if(Info.homeRedCards.containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = Info.homeRedCards.get(s);
                        Info.homeRedCards.remove(s);
                        if(!Persistence.addRedCard(Info.FFT_match_id, team_name, FFT_player_id, Info.season, time))
                            crawl.cleanTerminate("Add Red Cards failed in Home XI.");
                    }
                }

                break;
            case 1:
                if(!Persistence.addStartingXIs(Info.FFT_match_id, team_name, FFT_player_id, Info.season))
                    crawl.cleanTerminate("Add Starting XI Away Failed in Persistence.");
                for(String s : playerNameSplits) {
                    if(Info.awayRedCards.containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = Info.awayRedCards.get(s);
                        Info.awayRedCards.remove(s);
                        if(!Persistence.addRedCard(Info.FFT_match_id, team_name, FFT_player_id, Info.season, time))
                            crawl.cleanTerminate("Add Red Cards failed in Away XI.");
                    }
                }
                break;
            case 2:
                if(!Persistence.addSUBs(Info.FFT_match_id, team_name, FFT_player_id, Info.season))
                    crawl.cleanTerminate("Add Subs Home Failed in Persistence.");
                for(String s : playerNameSplits) {
                    if(Info.homeRedCards.containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = Info.homeRedCards.get(s);
                        Info.homeRedCards.remove(s);
                        if(!Persistence.addRedCard(Info.FFT_match_id, team_name, FFT_player_id, Info.season, time))
                            crawl.cleanTerminate("Add Red Cards failed in Home Sub In.");
                    }
                }
                break;
            case 4:
                if(!Persistence.addSUBs(Info.FFT_match_id, team_name, FFT_player_id, Info.season))
                    crawl.cleanTerminate("Add Subs Away Failed in Persistence.");
                for(String s : playerNameSplits) {
                    if(Info.awayRedCards.containsKey(s)) {
                        System.out.println("Red Card to Player Short = " + s + " Long = " + playerName);
                        String time = Info.awayRedCards.get(s);
                        Info.awayRedCards.remove(s);
                        if(!Persistence.addRedCard(Info.FFT_match_id, team_name, FFT_player_id, Info.season, time))
                            crawl.cleanTerminate("Add Red Cards failed in Away Sub In.");
                    }
                }
                break;
            default:
                crawl.cleanTerminate("Strange Error - Java - 1");
        }

        ActorRef self = getSender();
        Info.iorouter.route(commands.new ShotsCommand(playerDetailsObj, 1), self);
        Info.iorouter.route(commands.new ShotsCommand(playerDetailsObj, 2), self);
        Info.iorouter.route(commands.new ShotsCommand(playerDetailsObj, 3), self);
        Info.iorouter.route(commands.new ShotsCommand(playerDetailsObj, 4), self);
        Info.iorouter.route(commands.new PassesCommand(playerDetailsObj, 1), self);
        Info.iorouter.route(commands.new PassesCommand(playerDetailsObj, 2), self);
        Info.iorouter.route(commands.new PassesCommand(playerDetailsObj, 3), self);
        Info.iorouter.route(commands.new AssistsCommand(playerDetailsObj, 1), self);
        Info.iorouter.route(commands.new AssistsCommand(playerDetailsObj, 2), self);
        Info.iorouter.route(commands.new ReceivedPassesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new ChancesCreatedCommand(playerDetailsObj, 1), self);
        Info.iorouter.route(commands.new ChancesCreatedCommand(playerDetailsObj, 2), self);
        Info.iorouter.route(commands.new LongPassesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new ShortPassesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new CrossesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new TakeOnsCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new CornersCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new OffsidePassesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new BallRecoveriesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new TacklesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new InterceptionsCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new BlocksCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new ClearancesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new AerialDuelsCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new BlockedCrossesCommand(playerDetailsObj), self);
        Info.iorouter.route(commands.new DefensiveErrorsCommand(playerDetailsObj, 1), self);
        Info.iorouter.route(commands.new DefensiveErrorsCommand(playerDetailsObj, 2), self);
        Info.iorouter.route(commands.new FoulsCommand(playerDetailsObj, 1), self);
        Info.iorouter.route(commands.new FoulsCommand(playerDetailsObj, 2), self);
    }
}