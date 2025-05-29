package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public abstract class UserInterface {

    protected Activity activity;
    protected final RelativeLayout relativeLayout;
    protected UserInterface userInterfaceForBackButton;
    private static final ArrayList<UserInterface> userInterfaceList = new ArrayList<>();

    public UserInterface(Activity activity, int pageID){
        this.activity = activity;
        relativeLayout = activity.findViewById(pageID);
        userInterfaceList.add(this);
    }

    public void linkUserInterfaceForBackButton(UserInterface userInterfaceForBackButton){
        this.userInterfaceForBackButton = userInterfaceForBackButton;
    }

    public void showPage(){

        //Loop through all the userInterfaces that have been constructed
        for(UserInterface userInterface : userInterfaceList){

            //Set the visibility of the userInterface to invisible unless it is the userInterface that is being shown
            updateLayoutVisibility(userInterface.relativeLayout, userInterface == this);
        }
    }

    public void textPopUp(String messageContent){
        activity.runOnUiThread(() -> {
            final Toast toast = Toast.makeText(activity, messageContent, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public static void textPopUp(String messageContent, Activity activity){
        activity.runOnUiThread(() -> {
            final Toast toast = Toast.makeText(activity, messageContent, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public void errorMessagePopUp(String messageContent){

        VibratorManager vibratorManager = (VibratorManager) activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        Vibrator vibrator = vibratorManager.getDefaultVibrator();

        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);

        // it is safe to cancel other vibrations currently taking place
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect);

        activity.runOnUiThread(() -> {
            final Toast toast = Toast.makeText(activity, messageContent, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    public static void errorMessagePopUp(String messageContent, Activity activity){

        VibratorManager vibratorManager = (VibratorManager) activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        Vibrator vibrator = vibratorManager.getDefaultVibrator();

        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE);

        // it is safe to cancel other vibrations currently taking place
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect);

        activity.runOnUiThread(() -> {
            final Toast toast = Toast.makeText(activity, messageContent, Toast.LENGTH_SHORT);
            toast.show();
        });
    }

    protected void updateLayoutVisibility(RelativeLayout layout, boolean isVisible){
        if(isVisible){
            activity.runOnUiThread(() -> layout.setVisibility(View.VISIBLE));
        }
        else{
            activity.runOnUiThread(() -> layout.setVisibility(View.INVISIBLE));
        }
    }

    protected void updateButtonColor(Button button, String color){

        //String variable that the Hex Color code will be stored in
        String colorHexCode;

        //Switch Case For The Hex Codes Of The Colors We Use In The App
        switch (color){
            case "Green":
                colorHexCode = "#4CAF50";
                break;
            case "Orange":
                colorHexCode = "#FF9933";
                break;
            case "Red":
                colorHexCode = "#F44336";
                break;
            default:
                colorHexCode = "#F45336";
                break;
        }

        //Change The Background Color of the Button
        activity.runOnUiThread(() -> button.setBackgroundColor(Color.parseColor(colorHexCode)));

    }

    public void updateButtonColor(int id, String color){
        Button button = activity.findViewById(id);
        updateButtonColor(button, color);
    }

    protected void updateButtonText(Button button, String newText){

        //Change The Text of the Button
        activity.runOnUiThread(() -> button.setText(newText));

    }

    public void updateButtonText(int id, String newText){
        Button button = activity.findViewById(id);
        updateButtonText(button, newText);
    }

    protected void updateButtonEnabledStatus(Button button, Boolean enableButton){

        //if enableButton is true, this means we want to enable the button
        activity.runOnUiThread(() -> button.setEnabled(enableButton));

    }

    public void updateButtonEnabledStatus(int id, Boolean enableButton){
        Button button = activity.findViewById(id);
        updateButtonEnabledStatus(button, enableButton);
    }

    protected void updateSpinnerEnabledStatus(Spinner spinner, Boolean enableSpinner){

        //if enableButton is true, this means we want to enable the button
        activity.runOnUiThread(() -> spinner.setEnabled(enableSpinner));

    }

    public void updateSpinnerEnabledStatus(int id, Boolean enableSpinner){
        Spinner spinner = activity.findViewById(id);
        updateSpinnerEnabledStatus(spinner, enableSpinner);
    }

    protected void updateTextViewText(TextView textView, String newText){

        //Change The Text of the Button
        activity.runOnUiThread(() -> textView.setText(newText));

    }

    public void updateTextViewText(int id, String newText){
        TextView textView = activity.findViewById(id);
        updateTextViewText(textView, newText);
    }

    protected void updateTextViewVisibility(TextView textView, boolean visibility){

        if(visibility) {
            activity.runOnUiThread(() -> textView.setVisibility(View.VISIBLE));
        }
        else{
            activity.runOnUiThread(() -> textView.setVisibility(View.INVISIBLE));
        }

    }

    public void updateTextViewVisibility(int id, Boolean visibility){
        TextView textView = activity.findViewById(id);
        updateTextViewVisibility(textView, visibility);
    }

    public void updateProgressBar(int id, int minValue, int maxValue, int currentValue){
        ProgressBar progressBar = activity.findViewById(id);

        activity.runOnUiThread(() -> {
            progressBar.setMin(minValue);
            progressBar.setMax(maxValue);
            progressBar.setProgress(currentValue);
        });
    }

}
