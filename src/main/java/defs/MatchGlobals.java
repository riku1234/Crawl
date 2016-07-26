package defs;

import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by gourimishra on 7/25/16.
 */

public class MatchGlobals implements Serializable {
    public Long leagueID;
    public HashMap<String, String> homeSubstitutions, awaySubstitutions;
    public String gameLink, FFT_Match_ID, season, stadium;
    public Date gameDate;
    public Document gameDocument, playersDocument;
    public String home_team_name, away_team_name;
    public double home_possession, away_possession;
    public HashMap<String, String> home_red_cards, away_red_cards;
    public String fullTimeScore;
    public int num_players;

    public MatchGlobals(String gameLink, Long leagueID, String FFT_Match_ID, String season, Date gameDate, String stadium) {
        this.gameLink = gameLink;
        this.leagueID = leagueID;
        this.FFT_Match_ID = FFT_Match_ID;
        this.season = season;
        this.gameDate = gameDate;
        this.stadium = stadium;
        this.home_red_cards = new HashMap<>();
        this.away_red_cards = new HashMap<>();
        this.homeSubstitutions = new HashMap<>();
        this.awaySubstitutions = new HashMap<>();
    }
}