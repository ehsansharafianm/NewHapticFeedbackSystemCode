package com.umainebiomechanicslab.biomechanicslabapp;

import android.content.Context;
import android.util.Log;

import com.xsens.dot.android.sdk.events.DotData;
import com.xsens.dot.android.sdk.models.DotPayload;
import com.xsens.dot.android.sdk.utils.DotLogger;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StreamingIMU extends UniversalIMU{

    private final String TAG = "StreamingIMU";

    protected String trialName;
    protected int sampleCounter;
    protected int trialDurationMin;
    protected double trialAngleSum;

    protected FileManager.DotLogFile dotLogFile;
    protected boolean offsetAnglesInitialized;
    protected int offsetInitializationDurationSec;
    protected double offsetEulerAngle;

    public StreamingIMU(String nameOfIMU, Context context, IMUManager imuManager, UserInterfaceWithIMU userInterface, FileManager fileManager, int measurementMode){

        super(nameOfIMU, context, imuManager, userInterface, fileManager, measurementMode);

        //Offset Angles are not Initialized By Default
        this.offsetAnglesInitialized = false;

        //Set Duration for Initialization of Offset Angles
        offsetInitializationDurationSec = 3;

    }

    public double getOffsetEulerAngle() {
        return offsetEulerAngle;
    }

    public boolean getOffsetAnglesInitialized() {
        return offsetAnglesInitialized;
    }

    public void startOffsetInitialization(){
        trialName = "Initialization";
        sampleCounter = 0;
        trialAngleSum = 0;

        try{
            if(movellaDotDevice.startMeasuring()) {
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started");
                offsetAnglesInitialized = false;
            }
            else{
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }
        }
        catch (NullPointerException e){
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "startOffsetInitialization", e);
        }

    }

    public void stopOffsetInitialization(){

        if(movellaDotDevice != null){
            try{
                if(movellaDotDevice.stopMeasuring()){
                    fileManager.writeToLogFile(nameOfIMU + " Measurement Stopped");
                }
                else{
                    fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                    userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                }
            } catch (NullPointerException e){
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                Log.e(TAG, "startOffsetInitialization", e);
            }
        }

        imuManager.onAngleOffsetInitializationComplete();

    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

        this.trialName = trialName;
        this.trialDurationMin = trialDurationMin;
        sampleCounter = 0;

        if(logData){
            //Stream and save results to logFile
            dotLogFile = createDataLog(trialName, timeStamp);
        }
        else{
            dotLogFile = null;
        }

        try{
            if(movellaDotDevice.startMeasuring()) {
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started");
            }
            else{
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }
        }
        catch (NullPointerException e){
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "startTrial", e);
        }

    }

    @Override
    public FileManager.DotLogFile stopTrial(boolean logData) {

        if(movellaDotDevice != null){
            try{
                if(movellaDotDevice.stopMeasuring()){
                    fileManager.writeToLogFile(nameOfIMU + " Measurement Stopped");
                    if(logData){

                        //Pause for 0.1 seconds to allow the IMU to connect
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "stopTrial", e);
                        }

                        //Stop the logger that was created in startTrial()
                        dotLogFile.getDotLogger().stop();
                        return dotLogFile;
                    }
                }
                else{
                    fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                    userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                }
            } catch (NullPointerException e){
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                Log.e(TAG, "stopTrial", e);
            }
        }
        else{
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
        }
        return null;
    }

    @Override
    protected FileManager.DotLogFile createDataLog(String trialName, String timeStamp){
        try{
            File sessionFolderPath = fileManager.getSessionFolderPath();
            String dotLogFileFolder = sessionFolderPath.getPath() + "/Dot Log Files";
            //Ensure dotLogFileFolder exists
            if(!new File(dotLogFileFolder).exists()){
                if(!new File(dotLogFileFolder).mkdirs()){
                    Log.e(TAG, "createDataLog: Failed to create dotLogFileFolder");
                }
            }
            String dotLogFileName = movellaDotDevice.getTag() + "_" + nameOfIMU + "_" + trialName + "_" + timeStamp + ".csv";
            String dotLogFilePath = dotLogFileFolder + "/" + dotLogFileName;
            File dotLogFile = new File(dotLogFilePath);

            fileManager.writeToLogFile(dotLogFileName + " created");

            DotLogger logger = new DotLogger(context.getApplicationContext(), 1, DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1, dotLogFilePath, movellaDotDevice.getTag(), movellaDotDevice.getFirmwareVersion(),true,60,null,"25",0);

            return new FileManager.DotLogFile(dotLogFileName, dotLogFile, logger);
        }
        catch(NullPointerException e){
            fileManager.writeToLogFile("Error: Sensor Not Found");
            userInterface.errorMessagePopUp("Error: Sensor not found");
            Log.e(TAG, "createDataLog", e);
            return null;
        }
    }

    @Override
    public void onDotDataChanged(String address, DotData dotData) {

        double eulerAngleX = dotData.getEuler()[0];

        //Initialization is a special-case trialName that is used to calculate the offset angle
        if(trialName.equals("Initialization")){
            if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)){
                if ((sampleCounter % outputFrequency) == 0) {
                    userInterface.updateIMUDataOutput(nameOfIMU, "Initializing...");
                }
                trialAngleSum += eulerAngleX;
            } else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)){
                offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);
                userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"Initialized %.3f",offsetEulerAngle));
                fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);
                offsetAnglesInitialized = true;
                stopOffsetInitialization();
            }
        }

        //If trialDurationMin is set to 0, this means that the trial should run indefinitely, and output angle values instead of counting up time
        else if (trialDurationMin == 0) {
            dotData.setPacketCounter(sampleCounter);

            //Update the angle in real time 3 times/sec
            if ((sampleCounter % (outputFrequency/3)) == 0){
                userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"%.3f",(eulerAngleX - offsetEulerAngle)));
            }

            //If the dotLogFile is not null, update it
            if(dotLogFile != null){
                dotLogFile.getDotLogger().update(dotData);
            }

        }

        else{
            dotData.setPacketCounter(sampleCounter);

            if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {
                if ((sampleCounter % outputFrequency) == 0) {
                    userInterface.updateIMUDataOutput(nameOfIMU, ((sampleCounter / outputFrequency / 60) + ":" + String.format(Locale.US, "%02d", ((sampleCounter / outputFrequency) % 60))));
                }

                //If the dotLogFile is not null, update it
                if(dotLogFile != null){
                    dotLogFile.getDotLogger().update(dotData);
                }

            } else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {
                userInterface.updateIMUDataOutput(nameOfIMU, "DONE");
            }

        }

        //Increase the sample counter by 1
        sampleCounter++;

    }


}
