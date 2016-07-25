package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class BlockedCrossesCommand extends Global implements Serializable {
    public ArrayList<String> blockedcrosses;

    public BlockedCrossesCommand(PlayerDetails playerDetails) {
        this.playerDetails = playerDetails;
        this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_07#tabs-wrapper-anchor";
    }
}

