package com.umainebiomechanicslab.biomechanicslabapp.trials;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;

import java.util.ArrayList;

public abstract class Trial {

    //Variable to store the name of the trial
    protected final String trialName;

    //Variable to store trialTimeStamp
    protected final String trialTimeStamp;

    public Trial(String trialName, String trialTimeStamp){

        this.trialName = trialName;
        this.trialTimeStamp = trialTimeStamp;

    }

    public abstract ArrayList<FileManager.DotLogFile> getDotLogFiles();

    public String getTrialName(){
        return trialName;
    }


}
