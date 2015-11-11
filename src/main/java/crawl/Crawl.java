package crawl;

import actors.Info;
import actors.Tracker;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import command.Commands;
import fourfourtwo.Persistence;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/*
class MyComparator implements Comparator<JSONObject> { // Comparator to Sort Date objects
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
*/

public class Crawl {

    public static void main(String[] args) throws IOException, ParseException, org.json.simple.parser.ParseException {
        Persistence.createTables();
        //Persistence.deleteMatch("321662");
        final ActorSystem actorSystem = ActorSystem.create("Actor-System");
        final ActorRef tracker = actorSystem.actorOf(Props.create(Tracker.class).withDispatcher("TrackerDispatcher"), "Tracker");
        tracker.tell(new Commands().new StartCommand(0), null);

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
    }

    public MyDocument getDocument(String link) {

        synchronized (Crawl.class) {
            //System.out.println("Get Document called by " + Thread.currentThread().getName());
            while (true) {
                try {
                    MyDocument document = new MyDocument(Jsoup.connect(link).timeout(10000).get());
                    if(Info.fileWriter != null)
                        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "Inside Crawl getDocument. Success. Size = " + document.toString().length()+ "\n");
                    if(document.document.toString().length() < 150000) {
                        System.out.println("FLAG --- SIZE LESS " + document.document.toString().length());
                    }
                    if(document.document.toString().length() < 100000) {
                        System.out.println("Size less than 100000. Re-loading. Size = " + document.document.toString().length());
                        throw new IOException();
                    }
                    return document;
                } catch (IOException e) {
                    int rand = (int) (Math.random() * 10000);

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

    public Boolean addSubstitutions(String subInPlayerLink, String subOutPlayerLink) throws IOException {
        //System.out.println("Add Substitutions Called for Sub-In - " + subInPlayerLink + " Sub-Out - " + subOutPlayerLink);
        String[] subInDetails = subInPlayerLink.split("/");
        String[] subOutDetails = subOutPlayerLink.split("/");

        if(!subInDetails[6].equals(subOutDetails[6]))
            cleanTerminate("Sub In and Sub Out Match Id not same. Exiting.");

        String game_id = subInDetails[6];
        String sub_in_id = subInDetails[8]; String sub_out_id = subOutDetails[8];
        if(Info.fileWriter != null)
            Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + "Inside addSubstitutions. Persistence addSubstitutions called for GameID = " + game_id + " Sub-in ID = " + sub_in_id + " Sub-out ID = " + sub_out_id + "\n");
        return Persistence.addSubstitutions(game_id, sub_in_id, sub_out_id);
    }
/*
    void populateJSON(JSONObject jsonObject, String gameLink) throws ParseException {
        Document doc = getDocument(gameLink);

        String matchHeader = doc.document.select("div.teams").text();
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
    public boolean populateGameDetails(Commands.MatchGlobals matchGlobals) {

        Document doc = matchGlobals.getGameDocument();
        String home_team_name = doc.select("span.home-head").text();
        String away_team_name = doc.select("span.away-head").text();
        String full_time_score = doc.select("span.score").text();
        Double home_team_possession = Double.parseDouble(new StringTokenizer(doc.select("div.summary-chart svg text").get(0).text(), "%").nextToken());
        Double away_team_possession = Double.parseDouble(new StringTokenizer(doc.select("div.summary-chart svg text").get(1).text(), "%").nextToken());
        Elements homeRedCardElements = doc.select("div.home span.red_card");
        Elements awayRedCardElements = doc.select("div.away span.red_card");

        matchGlobals.setHome_team_name(home_team_name); matchGlobals.setAway_team_name(away_team_name);
        matchGlobals.setHome_possession(home_team_possession); matchGlobals.setAway_possession(away_team_possession);
        matchGlobals.setFullTimeScore(full_time_score);

        for(int i=0;i<homeRedCardElements.size();i++) {
            String text = homeRedCardElements.get(i).text();
            String[] splits = text.split(" ");
            matchGlobals.addHomeRedCard(splits[0].trim(), splits[1].trim());
            System.out.println("Home Red added. Name = " + splits[0] + " Time = " + splits[1]);
        }

        for(int i=0;i<awayRedCardElements.size();i++) {
            String text = awayRedCardElements.get(i).text();
            String[] splits = text.split(" ");
            matchGlobals.addAwayRedCard(splits[1].trim(), splits[0].trim());
            System.out.println("Away Red added. Name = " + splits[1] + " Time = " + splits[0]);
        }

        if(!Persistence.addMatch(matchGlobals.getStadium(), matchGlobals.getGameDate(), home_team_name, away_team_name, full_time_score, matchGlobals.getFFT_Match_ID(), matchGlobals.getSeason(), home_team_possession, away_team_possession)) {
            System.out.println("Add Match Unsuccessful for GameLink = " + matchGlobals.getGameLink());
            return false;
        }
        return true;
    }
/*
    ArrayList<String> getGameLinks(String resultsPage) throws IOException {
        Document doc = getDocument(resultsPage);
        Elements elements = doc.document.select("td.link-to-match a");
        ArrayList<String> gameLinks = new ArrayList<String>();
        for(int i = 0; i < elements.size(); i++) {
            gameLinks.add(elements.get(i).attr("abs:href"));
        }
        return gameLinks;
    }
*/

    public ArrayList<String> foulsDetails(MyDocument doc, int who) throws IOException {
        ArrayList<String> fouls = new ArrayList<>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside foulsDetails. No. of Fouls = " + fouls.size() + "\n");
        return fouls;
    }

    public ArrayList<String> redCardDetails(String gameLink) throws IOException {
        ArrayList<String> redCards = new ArrayList<>();
        MyDocument doc = getDocument(gameLink);
        Elements elements = doc.document.select("span.red_card");
        for(int i=0;i<elements.size();i++) {
            Element element = elements.get(i);
            String text = element.text();
            //String[] splits = text.split(" ");
            //String time = splits[0].trim(); String player = splits[1].trim();
            redCards.add(text);
        }
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside redCardDetails. No. of Red Cards = " + redCards.size() + "\n");
        return redCards;
    }

    public ArrayList<String> defensiveErrorsDetails(MyDocument doc, int leadingTo) throws IOException {
        Elements elements = doc.document.select("image");
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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside defensiveErrorsDetails. No. of defensiveErrors = " + defensiveErrors.size() + "\n");
        return defensiveErrors;
    }

    public ArrayList<String> blockedCrossesDetails(MyDocument doc) throws IOException {
        ArrayList<String> blockedCrosses = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside blockedCrossesDetails. No. = " + blockedCrosses.size() + "\n");
        return blockedCrosses;
    }

    public ArrayList<String> aerialDuelsDetails(MyDocument doc) throws IOException {
        ArrayList<String> aerial_duels = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside aerialDuelsDetails. No. = " + aerial_duels.size() + "\n");
        return aerial_duels;
    }

    public ArrayList<String> clearancesDetails(MyDocument doc) throws IOException {
        ArrayList<String> clearances = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside clearancesDetails. No. = " + clearances.size() + "\n");
        return clearances;
    }

    public ArrayList<String> blocksDetails(MyDocument doc) throws IOException {
        ArrayList<String> blocks = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside blocksDetails. No. = " + blocks.size() + "\n");
        return blocks;
    }

    public ArrayList<String> interceptionsDetails(MyDocument doc) throws IOException {
        ArrayList<String> interceptions = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside interceptionsDetails. No. = " + interceptions.size() + "\n");
        return interceptions;
    }

    public ArrayList<String> tacklesDetails(MyDocument doc) throws IOException {
        ArrayList<String> tackles = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside tacklesDetails. No. = " + tackles.size() + "\n");
        return tackles;
    }

    public ArrayList<String> chancesCreatedDetails(MyDocument doc, int from) throws IOException {
        ArrayList<String> chancesCreated = new ArrayList<>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside chancesCreatedDetails. No. = " + chancesCreated.size() + "\n");
        return chancesCreated;
    }

    public ArrayList<String> ballRecoveriesDetails(MyDocument doc) throws IOException {
        ArrayList<String> ballrecoveries = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside ballRecoveriesDetails. No. = " + ballrecoveries.size() + "\n");
        return ballrecoveries;
    }

    public ArrayList<String> offsidePassesDetails(MyDocument doc) throws IOException {
        ArrayList<String> offsidePasses = new ArrayList<String>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside offsidePassesDetails. No. = " + offsidePasses.size() + "\n");
        return offsidePasses;
    }

    public ArrayList<String> cornersDetails(MyDocument doc) throws IOException {
        ArrayList<String> corners = new ArrayList<String>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside cornersDetails. No. = " + corners.size() + "\n");
        return corners;
    }

    public ArrayList<String> takeOnsDetails(MyDocument doc) throws IOException {
        ArrayList<String> takeons = new ArrayList<String>();
        Elements elements = doc.document.select("image");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside takeonsDetails. No. = " + takeons.size() + "\n");
        return takeons;
    }

    public ArrayList<String> crossesDetails(MyDocument doc) throws IOException {
        ArrayList<String> crosses = new ArrayList<String>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside crossesDetails. No. = " + crosses.size() + "\n");
        return crosses;
    }

    public String getShortPassesDetails(MyDocument doc) throws IOException {
        String shortpasses = "";
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside shortPassesDetails. Count = " + count + " Success = " + success_count + " Fail = " + fail_count + " Assists = " + assist_count + " Chances = " + chances_count + "\n");
        return shortpasses;
    }

    public String getLongPassesDetails(MyDocument doc) throws IOException {
        String longpasses = "";
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside longPassesDetails. Count = " + count + " Success = " + success_count + " Fail = " + fail_count + " Assists = " + assist_count + " Chances = " + chances_count + "\n");
        return longpasses;
    }

    public ArrayList<String> receivedPassDetails(MyDocument doc) throws IOException {
        ArrayList<String> receivedPasses = new ArrayList<String>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside receivedPassDetails. No. = " + receivedPasses.size() + "\n");
        return receivedPasses;
    }

    public ArrayList<String> assistDetails(MyDocument doc, int from) throws IOException {
        ArrayList<String> assists = new ArrayList<>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside assistDetails. No. = " + assists.size() + "\n");
        return assists;
    }

    public ArrayList<String> passDetails(MyDocument doc, int third) throws IOException {
        ArrayList<String> passes = new ArrayList<>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Insidee passDetails. No. = " + passes.size() + "\n");
        return passes;
    }

    public ArrayList<String> freekickShotsDetails(MyDocument doc) throws IOException {
        ArrayList<String> freekick_shots = new ArrayList<String>();
        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside FKShotsDetails. No. = " + freekick_shots.size() + "\n");
        return freekick_shots;
    }

    public ArrayList<String> shotsDetails(MyDocument doc, int part) throws IOException {
        ArrayList<String> shots = new ArrayList<>();

        Elements elements = doc.document.select("line");

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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside shotsDetails. No. = " + shots.size() + "\n");
        return shots;
    }

    public ArrayList<String> penaltyDetails(MyDocument doc) throws IOException {
        Elements elements = doc.document.select("line");
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
        Info.fileWriter.write("\n" + System.currentTimeMillis() + " ==> " + " Inside penaltyDetails. No. = " + penalties.size() + "\n");
        return penalties;
    }

    public List<Elements> getPlayerStatsLink(Document doc) {
        int count = 0;
        Elements homeXIs = doc.select("div.lineup.home span a");
        if (homeXIs.size() != 11) {
            System.out.println("Home team doesn't have 11 players. Error.");
            return null;
        }
        Elements awayXIs = doc.select("div.lineup.away span a");
        if (awayXIs.size() != 11) {
            System.out.println("Away team doesn't have 11 players. Error.");
            return null;
        }
        Elements homeSUBSIN = doc.select("ul.home.subs li div ul li.first a");
        Elements homeSUBSOUT = doc.select("ul.home.subs li div ul li.last a");
        Elements awaySUBSIN = doc.select("ul.away.subs li div ul li.first a");
        Elements awaySUBSOUT = doc.select("ul.away.subs li div ul li.last a");
        if(homeSUBSIN.size() != homeSUBSOUT.size()) {
            System.out.println("Home Sub-In Size != Home Sub-Out Size. Error.");
            return null;
        }
        if(awaySUBSIN.size() != awaySUBSOUT.size()) {
            System.out.println("Away Sub-In Size != Away Sub-Out Size. Error.");
            return null;
        }
        List<Elements> list = new ArrayList();
        list.add(homeXIs);
        list.add(awayXIs);
        list.add(homeSUBSIN);
        list.add(homeSUBSOUT);
        list.add(awaySUBSIN);
        list.add(awaySUBSOUT);
        return list;
    }

    /*public void cleanTerminate(String errorMessage) {
        System.out.println(errorMessage);
        System.out.println("Match ID: " + Info.FFT_match_id);
        Persistence.deleteMatch(Info.FFT_match_id);
        System.out.println("Record Deleted. Exiting.");
        System.exit(1);
    }*/
}
