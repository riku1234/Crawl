package crawl;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import actors.Info;
import actors.Parent;
import actors.Tracker;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import command.Commands;
import fourfourtwo.Helper;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fourfourtwo.Persistence;
import java.text.DateFormat;
import java.text.ParseException;

import org.json.simple.JSONObject;
import scala.concurrent.Await;

import javax.print.Doc;

class MyExceptionHandler extends IOException {

}

class MyComparator implements Comparator<JSONObject> { /* Comparator to Sort Date objects */
    public int compare(JSONObject a, JSONObject b) {
        Date date_a = (Date)a.get("Date");
        Date date_b = (Date)b.get("Date");
        if(date_a.before(date_b))
            return -1;
        else if(date_a.equals(date_b))
            return 0;
        else
            return 1;
    }
}

public class Crawl {

    //HashMap<Integer, String> players;
    //public static String season=""; public static String FFT_match_id = ""; public static Date match_date;
    //ArrayList<String> blackLists = new ArrayList<String>(); /* Blacklist for Faulty links */

    //private static volatile Crawl oneCrawl = null;

    public Crawl() {
        //players = new HashMap<Integer, String>();
    }
/*
    public static Crawl getCrawlInstance() {
        if(oneCrawl == null) {
            synchronized (Crawl.class) {
                if(oneCrawl == null)
                    oneCrawl = new Crawl();
            }
        }
        return oneCrawl;
    }
*/
    public static void main(String[] args) throws IOException, ParseException, org.json.simple.parser.ParseException {
        //System.out.println("Arguments - " + args.length);
        //crawl.commands = new Commands();
        //crawl.parent.tell(crawl.commands.new StartParentCommand(crawl), null);

        Persistence.createTables();

        final ActorSystem actorSystem = ActorSystem.create("Actor-System");
        final ActorRef tracker = actorSystem.actorOf(Props.create(Tracker.class).withDispatcher("TrackerDispatcher"), "Tracker");
        tracker.tell(new Commands().new StartCommand(0), null);

        /* Blacklists begin ... */
        //crawl.blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360636"); /* No Data */
        //crawl.blackLists.add("http://www.fourfourtwo.com/statszone/8-2011/matches/360805"); /* No Data */
        //crawl.blackLists.add("http://www.fourfourtwo.com/statszone/21-2012/matches/459522"); /* No Data */
        //crawl.blackLists.add("http://www.fourfourtwo.com/statszone/24-2014/matches/752029"); /* Header Problem, less tokens. */
        //crawl.blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321742"); /* Subs mismatch */
        //crawl.blackLists.add("http://www.fourfourtwo.com/statszone/8-2010/matches/321900"); /* Subs missing */
        /* Blacklists end ... */

        //ArrayList<String> FFTResultsPage = new ArrayList<String>();

        /* English Premier League Begin ... */
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/8-2010");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/8-2011");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/8-2012");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/8-2013");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/8-2014");
        /*English Premier League End ... */

        /*Serie A Begin ... */
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/21-2012");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/21-2013");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/21-2014");
        /* Serie A End ... */

        /* Bundesliga Begin ... */
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/22-2012");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/22-2013");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/22-2014");
        /* Bundesliga End ... */

        /* La Liga Begin ... */
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/23-2012");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/23-2013");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/23-2014");
        /* La Liga End ... */

        /* Ligue 1 Begin ... */
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/24-2012");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/24-2013");
        //FFTResultsPage.add("http://www.fourfourtwo.com/statszone/results/24-2014");
        /* Ligue 1 End ... */

/*
        ArrayList<JSONObject> games = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        for (int l = 0; l < FFTResultsPage.size(); l++) {
            String FFTresultsPage = FFTResultsPage.get(l);
            StringTokenizer temp1 = new StringTokenizer(FFTresultsPage, "-");
            String leagueToken = temp1.nextToken();
            long league_id = Long.parseLong(leagueToken.substring(leagueToken.lastIndexOf('/') + 1, leagueToken.length()));
            season = temp1.nextToken();
            //System.out.println("League: " + Helper.getLeagueName(league_id) + " Season = " + season);
            ArrayList<String> gameLinks = crawl.getGameLinks(FFTResultsPage.get(l));
            for (int i = gameLinks.size() - 1; i >= 0; i--) {

                if (crawl.isBlackListed(gameLinks.get(i))) {
                    System.out.println("Game " + gameLinks.get(i) + " blacklisted. ");
                    continue;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("LeagueID", league_id);
                jsonObject.put("Season", season);
                String gameLink = gameLinks.get(i);
                jsonObject.put("GameLink", gameLink);
                crawl.populateJSON(jsonObject, gameLink);
                games.add(jsonObject);
            }
        }
        String prepend = "2014/";
        JSONObject jsonObjects = new JSONObject();
        //System.out.println("Starting sorting all the game links based on Date object");
        games.sort(new MyComparator());
        //System.out.println("Games sorted.");
        System.out.println("No. of Games - " + games.size());
        FileWriter sizeFile = new FileWriter(prepend + "SIZE");
        sizeFile.write(games.size());
        sizeFile.flush();
        sizeFile.close();

        for (int i = 0; i < games.size(); i++) {
            //System.out.println(games.get(i).toJSONString());
            //jsonObjects.put(i, games.get(i));
            FileWriter out = new FileWriter(prepend + i);
            games.get(i).put("Date", ((Date) games.get(i).get("Date")).toString());
            games.get(i).writeJSONString(out);
            out.flush();
            out.close();
        }

        //System.exit(0);

*/
        //Persistence.deleteMatch("321900");
/*
        JSONParser jsonParser = new JSONParser();
        String prepend = "2010_380/";

        for (int i = 0; i < 380; i++) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(prepend + i));
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Helper.getLocale((long) jsonObject.get("LeagueID")));
            String gameLink = (String) jsonObject.get("GameLink");
            //System.out.println("GameLink - " + gameLink);
            if (crawl.isBlackListed(gameLink)) {
                System.out.println("Game " + gameLink + " is Blacklisted. Skipping.");
                continue;
            }
            Helper.setLeagueID((Long) jsonObject.get("LeagueID"));
            Persistence.addLeague();
            season = (String) jsonObject.get("Season");
            match_date = df.parse((String) jsonObject.get("Date"));

            if(!crawl.populateGameDetails(gameLink, (String) jsonObject.get("Stadium"))) {
                System.out.println("Skipping Game - " + gameLink);
                continue;
            }

            System.out.println("Starting Game - " + gameLink);
            System.out.println("LeagueID - " + (Long) jsonObject.get("LeagueID") + " Season - " + season + " Date - " + match_date + " FFT ID - " + FFT_match_id);
            String playerStatsPage = gameLink + "/player-stats#tabs-wrapper-anchor";
            List<Elements> playerLinks = crawl.getPlayerStatsLink(playerStatsPage);

            for (int j = 0; j < playerLinks.size(); j++) {
                Boolean noTask = false;
                switch (j) {
                    case 0:
                        //System.out.println("HOME TEAM DETAILS");
                        break;
                    case 1:
                        //System.out.println("AWAY TEAM DETAILS");
                        break;
                    case 2:
                        //System.out.println("HOME TEAM SUBS DETAILS");
                        break;
                    case 3:
                        noTask = true;
                        break;
                    case 4:
                        //System.out.println("AWAY TEAM SUBS DETAILS");
                        break;
                    case 5:
                        noTask = true;
                        break;
                    default:
                        crawl.cleanTerminate("Strange Error - Java - 2");
                }

                if (!noTask) {
                    Elements temp = playerLinks.get(j);
                    for (int k = 0; k < temp.size(); k++) {
                        //System.out.println("GETTING PLAYER DETAILS FOR PLAYER#" + k);
                        String playerLink = temp.get(k).attr("abs:href");
                        crawl.getPlayerDetails(playerLink, j);
                    }
                }
            }

            for (int j = 0; j < playerLinks.get(2).size(); j++) {
                String homeTeamSubInLink = playerLinks.get(2).get(j).attr("abs:href");
                String homeTeamSubOutLink = playerLinks.get(3).get(j).attr("abs:href");
                if (!crawl.addSubstitutions(homeTeamSubInLink, homeTeamSubOutLink))
                    crawl.cleanTerminate("Substitutions could not be added. Error.");
            }

            //System.out.println("Game: " + gameLink + ", Match ID: " + FFT_match_id + " details saved.");
            if(!Persistence.gameSaved(FFT_match_id))
                crawl.cleanTerminate("Game Could not be Saved.");
            FFT_match_id = "";
            match_date = null;
        }
        */
    }

    public Document getDocument(String link) {
        synchronized (Crawl.class) {
            //System.out.println("Get Document called by " + Thread.currentThread().getName());
            while (true) {
                try {
                    Document document = Jsoup.connect(link).timeout(10000).get();
                    return document;
                } catch (IOException e) {
                    int rand = (int) (Math.random() * 10000);
                    //System.out.println("IO Exception. Trying again after " + rand + " ms.");
                    //System.out.println(e.toString());
                    try {
                        Thread.sleep(rand);
                    } catch (InterruptedException ee) {
                        continue;
                    }
                    continue;
                }
            }
        }
    }

    public Boolean addSubstitutions(String subInPlayerLink, String subOutPlayerLink) {
        //System.out.println("Add Substitutions Called for Sub-In - " + subInPlayerLink + " Sub-Out - " + subOutPlayerLink);
        String[] subInDetails = subInPlayerLink.split("/");
        String[] subOutDetails = subOutPlayerLink.split("/");

        if(!subInDetails[6].equals(subOutDetails[6]))
            cleanTerminate("Sub In and Sub Out Match Id not same. Exiting.");

        String game_id = subInDetails[6];
        String sub_in_id = subInDetails[8]; String sub_out_id = subOutDetails[8];

        return Persistence.addSubstitutions(game_id, sub_in_id, sub_out_id);
    }
/*
    void populateJSON(JSONObject jsonObject, String gameLink) throws ParseException {
        Document doc = getDocument(gameLink);

        String matchHeader = doc.select("div.teams").text();
        //System.out.println("Header = " + matchHeader);
        String[] matchHeaderDetails = matchHeader.split(",");
        String stadium = matchHeaderDetails[0].trim();
        jsonObject.put("Stadium", stadium);
        String day = matchHeaderDetails[matchHeaderDetails.length - 3].trim();
        String date = matchHeaderDetails[matchHeaderDetails.length - 2].trim();
        String[] otherDetails = matchHeaderDetails[matchHeaderDetails.length - 1].trim().split(" ");
        String year = otherDetails[0].trim();
        String time = otherDetails[2].trim();
        //System.out.println("Stadium: " + stadium + " Day: " + day + " date: " + date + " year: " + year + " time: " + time);
        String date_full = day.substring(0, 3) + " " + date.split(" ")[0].substring(0, 3) + " " + date.split(" ")[1].trim() + " " + time + " " + year;
        //System.out.println(date_full);
        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm yyyy", Helper.getLocale((Long)jsonObject.get("LeagueID")));
        Date game_date = df.parse(date_full);
        jsonObject.put("Date", game_date);
    }
*/
    public Boolean populateGameDetails(String gameLink, String stadium) throws IOException, ParseException {
        StringTokenizer st = new StringTokenizer(gameLink, "/");
        int count = st.countTokens();
        while(st.hasMoreTokens()) {
            Info.FFT_match_id = st.nextToken();
        }
        if(Persistence.gameExists(Info.FFT_match_id))
            return false;

        Document doc = getDocument(gameLink);

        String home_team_name = doc.select("span.home-head").text();
        String away_team_name = doc.select("span.away-head").text();
        String full_time_score = doc.select("span.score").text();
        Double home_team_possession = Double.parseDouble(new StringTokenizer(doc.select("div.summary-chart svg text").get(0).text(), "%").nextToken());
        Double away_team_possession = Double.parseDouble(new StringTokenizer(doc.select("div.summary-chart svg text").get(1).text(), "%").nextToken());


        //System.out.println(home_team_name + "," + away_team_name + "," + full_time_score + "," + FFT_match_id + "," + season);
        if(!Persistence.addMatch(stadium, Info.match_date, home_team_name, away_team_name, full_time_score, Info.FFT_match_id, Info.season, home_team_possession, away_team_possession))
            cleanTerminate("Add Match failed in Persistance.");
        return true;
    }
/*
    ArrayList<String> getGameLinks(String resultsPage) throws IOException {
        Document doc = getDocument(resultsPage);
        Elements elements = doc.select("td.link-to-match a");
        ArrayList<String> gameLinks = new ArrayList<String>();
        for(int i = 0; i < elements.size(); i++) {
            gameLinks.add(elements.get(i).attr("abs:href"));
        }
        return gameLinks;
    }
*/
/*
    public void getPlayerDetails(String playerLink, int j) throws IOException {
        String[] playerDetails = playerLink.split("/");
        //String FFTmatchID = playerDetails[6], FFTplayerID = playerDetails[8];
        Document doc = getDocument(playerLink);

        ArrayList<String> penalties = penaltyDetails(playerLink.substring(0, playerLink.length() - 30) + "0_SHOT_09#tabs-wrapper-anchor");

        ArrayList<String> shots = shotsDetails(playerLink);

        ArrayList<String> freekick_shots = freekickShotsDetails(playerLink);

        ArrayList<String> passes = passDetails(playerLink);

        ArrayList<String> assists = assistDetails(playerLink);

        ArrayList<String> receivedPasses = receivedPassDetails(playerLink);

        ArrayList<String> chancesCreated = chancesCreatedDetails(playerLink);

        String longpasses = getLongPassesDetails(playerLink);

        String shortpasses = getShortPassesDetails(playerLink);

        ArrayList<String> crosses = crossesDetails(playerLink);

        ArrayList<String> takeOns = takeOnsDetails(playerLink);

        ArrayList<String> corners = cornersDetails(playerLink);

        ArrayList<String> offsidePasses = offsidePassesDetails(playerLink);

        ArrayList<String> ballRecoveries = ballRecoveriesDetails(playerLink);

        ArrayList<String> tackles = tacklesDetails(playerLink);

        ArrayList<String> interceptions = interceptionsDetails(playerLink);

        ArrayList<String> blocks = blocksDetails(playerLink);

        ArrayList<String> clearances = clearancesDetails(playerLink);

        ArrayList<String> aerial_duels = aerialDuelsDetails(playerLink);

        ArrayList<String> blocked_crosses = blockedCrossesDetails(playerLink);

        ArrayList<String> defensiveErrors = defensiveErrorsDetails(playerLink);

        ArrayList<String> fouls = foulsDetails(playerLink);

        //System.out.println("PLAYER DETAILS ACQUIRED ... SAVING ...");
        if(!Persistence.addPlayer(doc.select("div.team-name").get(0).text(), doc.select("div#statzone_player_header h1").get(0).text(), FFTplayerID, FFTmatchID, match_date))
            cleanTerminate("Add Player Failed in Persistence.");
        switch(j) {
            case 0:
                if(!Persistence.addStartingXIs(FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
                    cleanTerminate("Add Starting XI Home Failed in Persistence.");
                break;
            case 1:
                if(!Persistence.addStartingXIs(FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
                    cleanTerminate("Add Starting XI Away Failed in Persistence.");
                break;
            case 2:
                if(!Persistence.addSUBs(FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
                    cleanTerminate("Add Subs Home Failed in Persistence.");
                break;
            case 4:
                if(!Persistence.addSUBs(FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
                    cleanTerminate("Add Subs Away Failed in Persistence.");
                break;
            default:
                cleanTerminate("Strange Error - Java - 1");
        }

        if(!Persistence.addPenalties(penalties, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add penalties Failed in Persistence.");

        if(!Persistence.addShots(shots, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add shots Failed in Persistence.");

        if(!Persistence.addFKShots(freekick_shots, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add FKshots Failed in Persistence.");

        if(!Persistence.addPasses(passes, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add passes Failed in Persistence.");

        if(!Persistence.addAssists(assists, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add assists Failed in Persistence.");

        if(!Persistence.addReceivedPasses(receivedPasses, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add received passes Failed in Persistence.");

        if (!Persistence.addChancesCreated(chancesCreated, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add chances created Failed in Persistence.");

        if (!Persistence.addLongPasses(longpasses, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add long passes Failed in Persistence.");

        if (!Persistence.addShortPasses(shortpasses, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add short passes Failed in Persistence.");

        if (!Persistence.addCrosses(crosses, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add crosses Failed in Persistence.");

        if (!Persistence.addTakeOns(takeOns, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add takeons Failed in Persistence.");

        if (!Persistence.addCorners(corners, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add corners Failed in Persistence.");

        if (!Persistence.addOffsidePasses(offsidePasses, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add offside passes Failed in Persistence.");

        if (!Persistence.addBallRecoveries(ballRecoveries, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add ball recoveries Failed in Persistence.");

        if (!Persistence.addTackles(tackles, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add tackles Failed in Persistence.");

        if (!Persistence.addInterceptions(interceptions, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add interceptions Failed in Persistence.");

        if (!Persistence.addBlocks(blocks, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add blocks Failed in Persistence.");

        if (!Persistence.addClearances(clearances, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add clearances Failed in Persistence.");

        if (!Persistence.addAerialDuels(aerial_duels, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add aerial duels Failed in Persistence.");

        if (!Persistence.addBlockedCrosses(blocked_crosses, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add blocked crosses Failed in Persistence.");

        if (!Persistence.addDefensiveErrors(defensiveErrors, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add defensive errors Failed in Persistence.");

        if (!Persistence.addFouls(fouls, FFTmatchID, doc.select("div.team-name").get(0).text(), FFTplayerID, season))
            cleanTerminate("Add fouls Failed in Persistence.");

        //System.out.println("PLAYER DETAILS SAVED ...");
    }
*/
    /*
    public ArrayList<String> foulsDetails(String playerLink) throws IOException {
        ArrayList<String> fouls = new ArrayList<String>();
        addFoulsDetails(fouls, playerLink, 1);
        addFoulsDetails(fouls, playerLink, 2);

        return fouls;
    }
*/

    public ArrayList<String> foulsDetails(Document doc, int who) throws IOException {
        ArrayList<String> fouls = new ArrayList<>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String output = time + ";;" + location + ";;" + who;
                //System.out.println("FOULS >> " + output);
                fouls.add(output);
            }
        }
        return fouls;
    }
/*
    public ArrayList<String> defensiveErrorsDetails(String playerLink) throws IOException {
        ArrayList<String> defensiveErrors = new ArrayList<String>();
        addDefensiveErrorsDetails(defensiveErrors, playerLink, 1);
        addDefensiveErrorsDetails(defensiveErrors, playerLink, 2);

        return defensiveErrors;
    }
*/
    public ArrayList<String> defensiveErrorsDetails(Document doc, int leadingTo) throws IOException {
        Elements elements = doc.select("image");
        ArrayList<String> defensiveErrors = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String output = time + ";;" + location + ";;" + leadingTo;
                //System.out.println("DEFENSIVE ERRORS >> " + output);
                defensiveErrors.add(output);
            }
        }
        return defensiveErrors;
    }

    public ArrayList<String> blockedCrossesDetails(Document doc) throws IOException {
        ArrayList<String> blockedCrosses = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String output = time + ";;" + location;
                //System.out.println("BLOCKED CROSSES >> " + output);
                blockedCrosses.add(output);
            }
        }
        return blockedCrosses;
    }

    public ArrayList<String> aerialDuelsDetails(Document doc) throws IOException {
        ArrayList<String> aerial_duels = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");
                String result = "";
                if (element.attr("xlink:href").contains("won")) {
                    result = "1";
                } else if (element.attr("xlink:href").contains("lost")) {
                    result = "0";
                }
                String output = time + ";;" + location + ";;" + result;
                //System.out.println("AERIAL DUELS >> " + output);
                aerial_duels.add(output);
            }
        }
        return aerial_duels;
    }

    public ArrayList<String> clearancesDetails(Document doc) throws IOException {
        ArrayList<String> clearances = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");
                String result = "";
                if (element.attr("xlink:href").contains("success")) {
                    result = "1";
                } else if (element.attr("xlink:href").contains("fail")) {
                    result = "0";
                }
                String output = time + ";;" + location + ";;" + result;
                //System.out.println("CLEARANCES >> " + output);
                clearances.add(output);
            }
        }
        return clearances;
    }

    public ArrayList<String> blocksDetails(Document doc) throws IOException {
        ArrayList<String> blocks = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String output = time + ";;" + location;
                //System.out.println("BLOCKS >> " + output);
                blocks.add(output);
            }
        }
        return blocks;
    }

    public ArrayList<String> interceptionsDetails(Document doc) throws IOException {
        ArrayList<String> interceptions = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String output = time + ";;" + location;
                //System.out.println("INTERCEPTIONS >> " + output);
                interceptions.add(output);
            }
        }
        return interceptions;
    }

    public ArrayList<String> tacklesDetails(Document doc) throws IOException {
        ArrayList<String> tackles = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");
                String result = "";
                if (element.attr("xlink:href").contains("success")) {
                    result = "1";
                } else if (element.attr("xlink:href").contains("fail")) {
                    result = "0";
                }
                String output = time + ";;" + location + ";;" + result;
                //System.out.println("TACKLES >> " + output);
                tackles.add(output);
            }
        }
        return tackles;
    }
/*
    public ArrayList<String> chancesCreatedDetails(String playerLink) throws IOException {
        ArrayList<String> chancesCreated = new ArrayList<String>();
        addChancesCreatedDetails(chancesCreated, playerLink, 1);
        addChancesCreatedDetails(chancesCreated, playerLink, 2);

        return chancesCreated;
    }
*/
    public ArrayList<String> chancesCreatedDetails(Document doc, int from) throws IOException {
        ArrayList<String> chancesCreated = new ArrayList<>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");

                String output = time + ";;" + location_start + ";;" + location_end + ";;" + from;
                //System.out.println("CHANCES >> " + output);
                chancesCreated.add(output);
            }
        }
        return chancesCreated;
    }

    public ArrayList<String> ballRecoveriesDetails(Document doc) throws IOException {
        ArrayList<String> ballrecoveries = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String output = time + ";;" + location;
                //System.out.println("BALL RECOVERIES >> " + output);
                ballrecoveries.add(output);
            }
        }
        return ballrecoveries;
    }

    public ArrayList<String> offsidePassesDetails(Document doc) throws IOException {
        ArrayList<String> offsidePasses = new ArrayList<String>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");

                String output = time + ";;" + location_start + ";;" + location_end;
                //System.out.println("OFFSIDE PASSES >> " + output);
                offsidePasses.add(output);
            }
        }
        return offsidePasses;
    }

    public ArrayList<String> cornersDetails(Document doc) throws IOException {
        ArrayList<String> corners = new ArrayList<String>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("smallyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("smallblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("smallred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("smalldeepskyblue")) {
                    result = "4";
                } else if (element.attr("marker-end").contains("smalldarkgrey")) {
                    result = "5";
                }
                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result;
                //System.out.println("CORNERS >> " + output);
                corners.add(output);
            }
        }
        return corners;
    }

    public ArrayList<String> takeOnsDetails(Document doc) throws IOException {
        ArrayList<String> takeons = new ArrayList<String>();
        Elements elements = doc.select("image");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location = element.attr("x") + "-" + element.attr("y");

                String result = "";
                if (element.attr("xlink:href").contains("success")) {
                    result = "1";
                } else if (element.attr("xlink:href").contains("fail")) {
                    result = "0";
                }
                String output = time + ";;" + location + ";;" + result;
                //System.out.println("TAKE ON >> " + output);
                takeons.add(output);
            }
        }
        return takeons;
    }

    public ArrayList<String> crossesDetails(Document doc) throws IOException {
        ArrayList<String> crosses = new ArrayList<String>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("smallyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("smallblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("smallred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("smalldeepskyblue")) {
                    result = "4";
                } else if (element.attr("marker-end").contains("smalldarkgrey")) {
                    result = "5";
                }
                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result;
                //System.out.println("CROSS >> " + output);
                crosses.add(output);
            }
        }
        return crosses;
    }

    public String getShortPassesDetails(Document doc) throws IOException {
        String shortpasses = "";
        Elements elements = doc.select("line");

        int count = 0; int success_count = 0; int fail_count = 0; int assist_count = 0; int chances_count = 0;

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                count++;

                if (element.attr("marker-end").contains("smallyellow")) {
                    assist_count++;
                } else if (element.attr("marker-end").contains("smallblue")) {
                    success_count++;
                } else if (element.attr("marker-end").contains("smallred")) {
                    fail_count++;
                } else if (element.attr("marker-end").contains("smalldeepskyblue")) {
                    chances_count++;
                }

            }
        }
        shortpasses = count + ";;" + success_count + ";;" + fail_count + ";;" + assist_count + ";;" + chances_count;
        return shortpasses;
    }

    public String getLongPassesDetails(Document doc) throws IOException {
        String longpasses = "";
        Elements elements = doc.select("line");

        int count = 0; int success_count = 0; int fail_count = 0; int assist_count = 0; int chances_count = 0;

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                count++;

                if (element.attr("marker-end").contains("smallyellow")) {
                    assist_count++;
                } else if (element.attr("marker-end").contains("smallblue")) {
                    success_count++;
                } else if (element.attr("marker-end").contains("smallred")) {
                    fail_count++;
                } else if (element.attr("marker-end").contains("smalldeepskyblue")) {
                    chances_count++;
                }

            }
        }
        longpasses = count + ";;" + success_count + ";;" + fail_count + ";;" + assist_count + ";;" + chances_count;
        return longpasses;
    }

    public ArrayList<String> receivedPassDetails(Document doc) throws IOException {
        ArrayList<String> receivedPasses = new ArrayList<String>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("smallyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("smallblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("smallred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("smalldeepskyblue")) {
                    result = "4";
                }
                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result;
                //System.out.println("RECEIVED PASS >> " + output);
                receivedPasses.add(output);
            }
        }
        return receivedPasses;
    }
/*
    public ArrayList<String> assistDetails(String playerLink) throws IOException {
        ArrayList<String> assists = new ArrayList<String>();
        addAssistDetails(assists, playerLink, 1);
        addAssistDetails(assists, playerLink, 2);

        return assists;
    }
*/
    public ArrayList<String> assistDetails(Document doc, int from) throws IOException {
        ArrayList<String> assists = new ArrayList<>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");

                String output = time + ";;" + location_start + ";;" + location_end + ";;" + from;
                //System.out.println("ASSIST >> " + output);
                assists.add(output);
            }
        }
        return assists;
    }
/*
    public ArrayList<String> passDetails(String playerLink) throws IOException {
        ArrayList<String> passes = new ArrayList<String>();
        addPassDetails(passes, playerLink, 1);
        addPassDetails(passes, playerLink, 2);
        addPassDetails(passes, playerLink, 3);
        addPassDetails(passes, playerLink, 4); // Free kick Passes

        return passes;
    }
*/
    public ArrayList<String> passDetails(Document doc, int third) throws IOException {
        ArrayList<String> passes = new ArrayList<>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = new StringTokenizer(element.attr("class").substring(19, element.attr("class").length()), " ").nextToken();
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("smallyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("smallblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("smallred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("smalldeepskyblue")) {
                    result = "4";
                }
                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result + ";;" + third;
                //System.out.println("PASS >> " + output);
                passes.add(output);
            }
        }
        return passes;
    }

    public ArrayList<String> freekickShotsDetails(Document doc) throws IOException {
        ArrayList<String> freekick_shots = new ArrayList<String>();
        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = element.attr("class").substring(19, element.attr("class").length());
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("bigyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("bigblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("bigred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("bigdarkgrey")) {
                    result = "4";
                }
                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result;
                //System.out.println("FREE KICK SHOT >> " + output);
                freekick_shots.add(output);
            }
        }
        return freekick_shots;
    }
/*
    public ArrayList<String> shotsDetails(String playerLink) throws IOException {
        ArrayList<String> shots = new ArrayList<String>();
        addShotsDetails(shots, playerLink, 1);
        addShotsDetails(shots, playerLink, 2);
        addShotsDetails(shots, playerLink, 3);
        addShotsDetails(shots, playerLink, 4);
        addShotsDetails(shots, playerLink, 5); // Shots from set-play
        return shots;
    }
*/
    public ArrayList<String> shotsDetails(Document doc, int part) throws IOException {
        ArrayList<String> shots = new ArrayList<>();

        Elements elements = doc.select("line");

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = element.attr("class").substring(19, element.attr("class").length());
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("bigyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("bigblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("bigred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("bigdarkgrey")) {
                    result = "4";
                }

                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result + ";;" + part;
                //System.out.println("SHOT >> " + output);
                shots.add(output);
            }
        }
        return shots;
    }

    public ArrayList<String> penaltyDetails(Document doc) throws IOException {
        Elements elements = doc.select("line");
        ArrayList<String> penalties = new ArrayList();
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            if (element.attr("class").contains("pitch-object")) {
                String time = element.attr("class").substring(19, element.attr("class").length());
                String location_start = element.attr("x1") + "-" + element.attr("y1");
                String location_end = element.attr("x2") + "-" + element.attr("y2");
                String result = "";
                if (element.attr("marker-end").contains("bigyellow")) {
                    result = "3";
                } else if (element.attr("marker-end").contains("bigblue")) {
                    result = "1";
                } else if (element.attr("marker-end").contains("bigred")) {
                    result = "2";
                } else if (element.attr("marker-end").contains("bigdarkgrey")) {
                    result = "4";
                }
                String output = time + ";;" + location_start + ";;" + location_end + ";;" + result;
                //System.out.println("PENALTY >> " + output);
                penalties.add(output);
            }
        }
        return penalties;
    }

    public List<Elements> getPlayerStatsLink(String URL) throws IOException {
        int count = 0;
        Document doc = getDocument(URL);
        Elements homeXIs = doc.select("div.lineup.home span a");
        if (homeXIs.size() != 11) {
            cleanTerminate("Home team doesn't have 11 players. Error.");
        }
        Elements awayXIs = doc.select("div.lineup.away span a");
        if (awayXIs.size() != 11) {
            cleanTerminate("Away team doesn't have 11 players. Error.");
        }
        Elements homeSUBSIN = doc.select("ul.home.subs li div ul li.first a");
        Elements homeSUBSOUT = doc.select("ul.home.subs li div ul li.last a");
        Elements awaySUBSIN = doc.select("ul.away.subs li div ul li.first a");
        Elements awaySUBSOUT = doc.select("ul.away.subs li div ul li.last a");
        if(homeSUBSIN.size() != homeSUBSOUT.size())
            cleanTerminate("Number of home subs in not equal to number of home subs out.");
        if(awaySUBSIN.size() != awaySUBSOUT.size())
            cleanTerminate("Number of away subs in not equal to number of home subs out.");
        List<Elements> list = new ArrayList();
        list.add(homeXIs);
        list.add(awayXIs);
        list.add(homeSUBSIN);
        list.add(homeSUBSOUT);
        list.add(awaySUBSIN);
        list.add(awaySUBSOUT);
        return list;
    }

    public void cleanTerminate(String errorMessage) {
        System.out.println(errorMessage);
        System.out.println("Match ID: " + Info.FFT_match_id);
        Persistence.deleteMatch(Info.FFT_match_id);
        System.out.println("Record Deleted. Exiting.");
        System.exit(1);
    }
}
