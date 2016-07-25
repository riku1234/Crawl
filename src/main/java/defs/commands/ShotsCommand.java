package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class ShotsCommand extends Global implements Serializable {
    public ArrayList<String> shots;
    public int index;

    public ShotsCommand(PlayerDetails playerDetails, int index) {
        this.playerDetails = playerDetails;
        this.index = index;
        switch (index) {
            case 1:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_12#tabs-wrapper-anchor";
                break;
            case 2:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_13#tabs-wrapper-anchor";
                break;
            case 3:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_11#tabs-wrapper-anchor";
                break;
            case 4:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_14#tabs-wrapper-anchor";
                break;
            case 5:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "0_SHOT_08#tabs-wrapper-anchor";
                break;
        }
    }
}

