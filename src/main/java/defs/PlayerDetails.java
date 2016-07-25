package defs;

import org.jsoup.nodes.Document;

import java.io.Serializable;

/**
 * Created by gourimishra on 7/25/16.
 */

public class PlayerDetails implements Serializable {
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
    }
}
