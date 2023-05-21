package com.example.mifonelibproj.model.other;

import org.linphone.core.Call;

public class State {
    Call.State state;

    public State(Call.State state) {
        this.state = state;
    }

    public int toInt(){
        return state.toInt();
    }
}
