package com.example.harald.runwithme2;

import com.example.harald.runwithme2.GPSPosition;

import java.io.Serializable;

/**
 * Created by harald on 02.06.17.
 */

public class PathItem implements Serializable {

    private GPSPosition position;
    private Long time;
    private Double distance;
    private Double speed;
    private Integer steps;

    public PathItem(GPSPosition position, Long time, Double distance, Double speed, Integer steps) {
        this.position = position;
        this.time = time;
        this.distance = distance;
        this.speed = speed;
        this.steps = steps;
    }

    public boolean runFaster(Double desiredSpeed) {
        return (this.speed < desiredSpeed);
    }

    public GPSPosition getPosition() {
        return position;
    }
    public Long getTime() { return this.time; }
}
