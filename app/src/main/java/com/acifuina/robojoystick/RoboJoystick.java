package com.acifuina.robojoystick;

import android.app.Application;

/**
 * Created by racifuina on 11/09/17.
 */

public class RoboJoystick extends Application {
    private static RoboJoystick singleton;
    @Override
    public void onCreate() {
        super.onCreate();
        BLEConnectionManager.getInstance();
        singleton = this;
    }
    public static RoboJoystick getInstance(){
        return singleton;
    }

}
