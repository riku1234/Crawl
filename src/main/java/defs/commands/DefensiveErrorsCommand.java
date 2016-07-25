package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class DefensiveErrorsCommand extends Global implements Serializable {
    public ArrayList<String> defensiveerrors;
    public int index;

    public DefensiveErrorsCommand(PlayerDetails playerDetails, int index) {
        this.playerDetails = playerDetails;
        this.index = index;
        if (index == 1)
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_10#tabs-wrapper-anchor";
        else
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_09#tabs-wrapper-anchor";
    }
}

