package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class InterceptionsCommand extends Global implements Serializable {
    public ArrayList<String> interceptions;

    public InterceptionsCommand(PlayerDetails playerDetails) {
        this.playerDetails = playerDetails;
        this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "3_DEFENCE_02#tabs-wrapper-anchor";
    }
}

