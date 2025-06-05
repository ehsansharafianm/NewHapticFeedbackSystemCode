package com.umainebiomechanicslab.biomechanicslabapp.userinterfaces;

import android.app.Activity;
import android.widget.Button;

import com.umainebiomechanicslab.biomechanicslabapp.R;

public class ParticipantMenuUI extends UserInterface {

    public ParticipantMenuUI(Activity activity, int pageID) {

        super(activity, pageID);

        //Declare UI Objects
        Button goBackButton = activity.findViewById(R.id.participant_menu_GoBack);

        //Set Go Back Button Click Listener
        goBackButton.setOnClickListener(view -> userInterfaceForBackButton.showPage());
    }
}
