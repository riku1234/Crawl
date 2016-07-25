package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class AssistsCommand extends Global implements Serializable {
    public ArrayList<String> assists;
    public int index;

    public AssistsCommand(PlayerDetails playerDetails, int index) {
        this.playerDetails = playerDetails;
        this.index = index;
        if (index == 1)
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0901#tabs-wrapper-anchor";
        else
            this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0902#tabs-wrapper-anchor";
    }
}

