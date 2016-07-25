package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class ChancesCreatedCommand extends Global implements Serializable {
    public ArrayList<String> chancescreated;
    public int index;

    public ChancesCreatedCommand(PlayerDetails playerDetails, int index) {
        this.playerDetails = playerDetails;
        this.index = index;
        if (index == 1)
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_1101#tabs-wrapper-anchor";
        else
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_1102#tabs-wrapper-anchor";
    }
}

