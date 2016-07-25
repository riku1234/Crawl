package defs.commands;

import java.io.Serializable;

/**
 * Created by gourimishra on 7/25/16.
 */

public class StartCommand implements Serializable {

    public int num_trackers;
    public int num_child;
    public int num_io;
    public int num_tor;

    public StartCommand(int n_trackers, int n_child, int n_io, int n_tor) {
        this.num_trackers = n_trackers;
        this.num_child = n_child;
        this.num_io = n_io;
        this.num_tor = n_tor;
    }
}

