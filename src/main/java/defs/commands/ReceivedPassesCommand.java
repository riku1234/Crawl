package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class ReceivedPassesCommand extends Global implements Serializable {
    public ArrayList<String> receivedpasses;

    public ReceivedPassesCommand(PlayerDetails playerDetails) {
        this.playerDetails = playerDetails;
        this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_07#tabs-wrapper-anchor";
    }
}

