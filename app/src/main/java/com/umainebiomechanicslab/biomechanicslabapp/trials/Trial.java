package com.umainebiomechanicslab.biomechanicslabapp.trials;

import android.content.Context;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterface;

import java.io.File;
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

    public abstract String getCommaSeparatedTrialData();

    public abstract ArrayList<FileManager.DotLogFile> getDotLogFiles();

    public abstract void appendToGaitParameterArrayList(String nameOfIMU, String gaitParameter, double data);

    public abstract void updateCompletedTrialData(String nameOfIMU, String gaitParameter, double data);

    public abstract File createTrialCSVFile(String subjectTitle, Context context, FileManager fileManager, UserInterface userInterface);

    public String getTrialName(){
        return trialName;
    }

}
