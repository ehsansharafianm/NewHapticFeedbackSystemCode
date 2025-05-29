package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.widget.Button;

public class ParticipantMenuUI extends UserInterface{

    public ParticipantMenuUI(Activity activity, int pageID) {

        super(activity, pageID);

        //Declare UI Objects
        Button goBackButton = activity.findViewById(R.id.participant_menu_GoBack);

        //Set Go Back Button Click Listener
        goBackButton.setOnClickListener(view -> userInterfaceForBackButton.showPage());
    }
}
