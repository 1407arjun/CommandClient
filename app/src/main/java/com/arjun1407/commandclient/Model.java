package com.arjun1407.commandclient;

import com.google.gson.annotations.SerializedName;

public class Model {
    @SerializedName("auth")
    private int auth;
    @SerializedName("cmd")
    private String cmd;

    public Model(int auth, String cmd) {
        this.auth = auth;
        this.cmd = cmd;
    }

    public void setAuth(int auth) {
        this.auth = auth;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
