package defs.commands;

import defs.Global;
import defs.PlayerDetails;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gourimishra on 7/25/16.
 */

public class PassesCommand extends Global implements Serializable {
    public ArrayList<String> passes;
    public int index;

    public PassesCommand(PlayerDetails playerDetails, int index) {
        this.playerDetails = playerDetails;
        this.index = index;
        switch (index) {
            case 1:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_08#tabs-wrapper-anchor";
                break;
            case 2:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0802#tabs-wrapper-anchor";
                break;
            case 3:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_0801#tabs-wrapper-anchor";
                break;
            case 4:
                this.commandLink = playerDetails.playerLink.substring(0, playerDetails.playerLink.length() - 30) + "1_PASS_113#tabs-wrapper-anchor";
                break;
        }
    }
}

