package com.example.harald.runwithme2;

import android.graphics.Color;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by harald on 02.06.17.
 */

public class Competitor implements Serializable {

    private GPSPosition startPos;
    private GPSPosition endPos;
    private ArrayList<PathItem> items;
    private PathItem lastItem;
    private Double desiredSpeed;
    private int color;
    private IMessageConsumer consumer;

    //protobuf communication
    private transient ProtobufMqttProgram protoBuf = null;

    public Competitor(int color) {
        this.items = new ArrayList<>();
        this.color = color;
    }
    public void setProtoBuf(ProtobufMqttProgram protoBuf, IMessageConsumer consumer) {
        this.protoBuf = protoBuf;
        this.consumer = consumer;
    }
    public void addItem(GPSPosition pos)
    {
        Double distance = 0.0d;
        Double speed = 0.0d;
        Long now = System.currentTimeMillis();

        if(lastItem != null)
        {
            distance = this.distance(this.lastItem.getPosition(), pos);
            speed = this.speed(distance, this.lastItem.getTime(), now);
        }

        this.lastItem = new PathItem(pos, now, distance, speed);
        this.items.add(this.lastItem);

        if(this.protoBuf != null)
        {
            try
            {
                this.protoBuf.sendPathItem(pos.getLatitude(), pos.getLongitude(), now, distance, speed, 1);
            } catch (MqttException e) {
                this.consumer.showMessage(e.toString());
            }
        }


    }
    public void setStartPos(GPSPosition startPoint) {
        this.startPos = startPoint;
        this.addItem(this.startPos);
    }
    public GPSPosition getStartPos() {
        return startPos;
    }

    public GPSPosition getEndPos() {
        return endPos;
    }
    public void setEndPos(GPSPosition endPos) {
        this.endPos = endPos;
    }

    public ArrayList<PathItem> getItems() {
        return items;
    }
    public Double getDesiredSpeed() {
        return desiredSpeed;
    }
    public void setDesiredSpeed(Double desiredSpeed) {
        this.desiredSpeed = desiredSpeed;
    }


    private double deg2rad(double deg) {
        //function converts decimal degrees to radians
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        //function converts radians to decimal degrees
        return (rad * 180 / Math.PI);
    }

    public double distance(GPSPosition pos1, GPSPosition pos2) {
        double theta = pos1.getLongitude() - pos2.getLongitude();
        double dist = Math.sin(deg2rad(pos1.getLatitude())) * Math.sin(deg2rad(pos2.getLatitude())) + Math.cos(deg2rad(pos1.getLatitude())) * Math.cos(deg2rad(pos2.getLatitude())) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return dist;
    }
    public double speed(double distance, long time1, long time2) {
        Double timeDiff = Double.valueOf(time2 - time1);
        timeDiff = timeDiff / 1000; //seconds
        timeDiff = timeDiff / 3600; //hours
        double speed = distance / timeDiff;
        return speed;
    }

    public int getColor() {
        return color;
    }
}
