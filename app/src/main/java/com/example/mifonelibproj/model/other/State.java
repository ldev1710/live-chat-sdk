package com.example.mifonelibproj.model.other;

import org.linphone.core.Call;

public class State {
    Call.State state;
    public static final int Idle = (0);
    public static final int IncomingReceived = (1);
    public static final int OutgoingInit = (2);
    public static final int OutgoingProgress = (3);
    public static final int OutgoingRinging = (4);
    public static final int OutgoingEarlyMedia = (5);
    public static final int Connected = (6);
    public static final int StreamsRunning = (7);
    public static final int Pausing = (8);
    public static final int Paused = (9);
    public static final int Resuming = (10);
    public static final int Referred = (11);
    public static final int Error = (12);
    public static final int End = (13);
    public static final int PausedByRemote = (14);
    public static final int UpdatedByRemote = (15);
    public static final int IncomingEarlyMedia = (16);
    public static final int Updating = (17);
    public static final int Released = (18);
    public static final int EarlyUpdatedByRemote = (19);
    public static final int EarlyUpdating = (20);
    public State(Call.State state) {
        this.state = state;
    }

    public int toInt(){
        return state.toInt();
    }
}
