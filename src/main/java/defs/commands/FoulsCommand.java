package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class FoulsCommand extends Global implements Serializable {
    public ArrayList<String> fouls;
    public int index;

    public FoulsCommand(PlayerDetails playerDetails, int index) {
        this.playerDetails = playerDetails;
        this.index = index;
        if (index == 1)
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "4_FOULS_01#tabs-wrapper-anchor";
        else
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "4_FOULS_02#tabs-wrapper-anchor";
    }
}
