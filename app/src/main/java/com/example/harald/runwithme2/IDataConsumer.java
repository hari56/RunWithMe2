package com.example.harald.runwithme2;

import java.io.Serializable;

/**
 * Created by Harald on 22.06.2017.
 */

public interface IDataConsumer extends Serializable {

    /**
     * shows a message
     * @param message
     */
    public void showMessage(String message);

    /**
     * update the map
     */
    public void updateMap();
}
