package com.example.harald.runwithme2;

import java.io.Serializable;

/**
 * Created by Harald on 22.06.2017.
 */

public interface IMessageConsumer extends Serializable {

    public void showMessage(String message);
}
