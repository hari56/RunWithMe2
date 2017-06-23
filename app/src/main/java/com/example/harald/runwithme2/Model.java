package com.example.harald.runwithme2;

import android.graphics.Color;

import java.io.Serializable;

/**
 * Created by Harald on 04.06.2017.
 */

public class Model implements Serializable, IProtobufBridge {

    public enum ACTION {
        SELECT_STARTPOINT,
        SELECT_ENDPOINT,
        ACTIVE_RUN
    }

    //active action in program
    private ACTION currentAction;

    //the players
    private Competitor local;
    private Competitor remote;

    //protobuf communication
    private transient ProtobufMqttProgram protoBuf = null;

    public Model() {
        this.local = new Competitor(Color.BLUE, true);
        this.remote = new Competitor(Color.RED, false);
    }
    public void setupProtoBuf(IDataConsumer consumer) {
        this.protoBuf = new ProtobufMqttProgram(this);
        this.local.setProtoBuf(this.protoBuf, consumer);
        this.remote.setProtoBuf(this.protoBuf, consumer);
    }

    public void setCurrentAction(ACTION action) { this.currentAction = action; }
    public ACTION getCurrentAction() { return currentAction; }

    public Competitor getLocal() {
        return local;
    }
    public Competitor getRemote() {
        return remote;
    }

    public ProtobufMqttProgram getProtoBuf() {
        return protoBuf;
    }

}
