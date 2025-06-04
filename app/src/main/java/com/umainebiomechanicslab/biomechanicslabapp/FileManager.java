package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.xsens.dot.android.sdk.utils.DotLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FileManager{

    //Declare TAG for Error Logging
    private static final String TAG = "FileManager";

    //Declare the activity
    private final Activity activity;

    //Declare the log popup window UI
    private final LogPopupWindowUI logPopupWindowUI;

    //Declare the loading window UI
    private final LoadingWindowUI loadingWindowUI;

    //Declare the folder name variable
    private String folderName;

    //Declare the session folder path
    private File sessionFolderPath;

    //Declare the application log files
    private File applicationLogFile;

    //Declare the file array list of all files in the session folder
    private final ArrayList<File> fileArrayList;

    //Create the Dot Log File Array List
    static ArrayList<DotLogFile> dotLogFileList = new ArrayList<>();

    //Declare the DotLogFile array list of all the DotData log files in the session folder
    ArrayList<Trial> allTrials;

    //Declare the Firebase Authentication instance
    private final FirebaseAuth mAuth;

    //Declare the Google Sign-In client
    private final GoogleSignInClient mGoogleSignInClient;

    //
    public static final int RC_SIGN_IN = 9001;

    //Declare the current user
    FirebaseUser currentUser;

    public FileManager(Context context, Activity activity, LogPopupWindowUI logPopupWindowUI, LoadingWindowUI loadingWindowUI){

        //Set the activity
        this.activity = activity;

        //Link the log popup window UI
        this.logPopupWindowUI = logPopupWindowUI;

        //Link the loading window UI
        this.loadingWindowUI = loadingWindowUI;

        //Create new ArrayList for fileArrayList and allTrials
        fileArrayList = new ArrayList<>();
        allTrials = new ArrayList<>();

        //Set currentDate to the current date in YYYY_MM_DD format
        String currentDate = java.text.DateFormat.getDateInstance().format(new Date());

        //Create a temporary folder name with the current date and time
        folderName = "unnamedFolder_" + java.text.DateFormat.getDateTimeInstance().format(new Date());

        //Create the path to the folder for this session (in the App Data folder)
        sessionFolderPath = context.getApplicationContext().getExternalFilesDir("App Data/" + currentDate + "/" + folderName);

        //Create the application log file in the session folder
        applicationLogFile = new File(sessionFolderPath, "applicationLog.txt");
        fileArrayList.add(applicationLogFile);

        //Initialize Firebase
        FirebaseApp.initializeApp(context);

        // For Play Integrity API
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id)) // Use your web client ID
                .requestEmail()
                .build();

        // Build a GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

    }

    public File getSessionFolderPath(){
        return sessionFolderPath;
    }

    public boolean renameSessionFolder(String newFolderName) {
        if (newFolderName == null || newFolderName.isEmpty() || newFolderName.contains(File.separator)) {
            Log.e(TAG, "New folder name is invalid: " + newFolderName);
            writeToLogFile("ERROR: New folder name is invalid: " + newFolderName);
            UserInterface.errorMessagePopUp("Directory Error", activity);
            return false;
        }

        if (sessionFolderPath == null || !sessionFolderPath.exists() || !sessionFolderPath.isDirectory()) {
            Log.e(TAG, "Original session folder path is invalid or does not exist.");
            writeToLogFile("ERROR: Original session folder path is invalid or does not exist.");
            UserInterface.errorMessagePopUp("Directory Error", activity);
            return false;
        }

        File parentDir = sessionFolderPath.getParentFile();
        if (parentDir == null) {
            Log.e(TAG, "Cannot get parent directory of the session folder.");
            writeToLogFile("ERROR: Cannot get parent directory of the session folder.");
            UserInterface.errorMessagePopUp("Directory Error", activity);
            return false;
        }

        File newSessionFolderPath = new File(parentDir, newFolderName);

        if (newSessionFolderPath.exists()) {
            Log.e(TAG, "A folder with the new name already exists: " + newSessionFolderPath.getAbsolutePath());
            writeToLogFile("ERROR: A folder with the new name already exists: " + newSessionFolderPath.getAbsolutePath());
            UserInterface.errorMessagePopUp("Directory Error", activity);
            return false;
        }

        // Attempt to rename the folder
        if (sessionFolderPath.renameTo(newSessionFolderPath)) {
            Log.i(TAG, "Session folder renamed from " + sessionFolderPath.getName() + " to " + newFolderName);
            File oldSessionFolderPath = sessionFolderPath; // Keep a reference to the old path for updating
            sessionFolderPath = newSessionFolderPath;     // Update the class field to the new path

            // --- Loop through fileArrayList to update File objects ---
            ArrayList<File> updatedFileArrayList = new ArrayList<>();
            for (File oldFile : fileArrayList) {
                // Assuming all files in fileArrayList were directly inside the sessionFolderPath
                // If they could be in subdirectories, this logic would need to be more complex
                // to preserve the relative path within the session folder.
                // For simple cases where files are directly in sessionFolderPath:
                String fileName = oldFile.getName();
                File newFile = new File(sessionFolderPath, fileName); // Create new File object with new parent path
                updatedFileArrayList.add(newFile);

                // Update specific references if needed (like applicationLogFile)
                if (oldFile.equals(applicationLogFile)) {
                    applicationLogFile = newFile;
                }
            }
            fileArrayList.clear();
            fileArrayList.addAll(updatedFileArrayList);
            // --- End of update loop ---

            writeToLogFile("Session folder renamed to: " + newFolderName); // Log the change
            folderName = newFolderName;
            return true;
        } else {
            Log.e(TAG, "Failed to rename session folder to " + newFolderName);
            return false;
        }
    }

    public void writeToLogFile(String message){

        //Write To Log Popup Window:
        logPopupWindowUI.appendToLogPopUpWindow(message);

        //Write To Log File:
        FileOutputStream stream = null;

        //Add time stamps to the log message
        String logMessageWithDateTime = java.text.DateFormat.getDateTimeInstance().format(new Date()) + ": " + message + "\n";

        //Write to the log file
        try {
            stream = new FileOutputStream(applicationLogFile, true);
            stream.write(logMessageWithDateTime.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "writeToLogFile", e);
        } catch (NullPointerException e) {
            UserInterface.textPopUp("No Log File Found", activity);
            Log.e(TAG, "writeToLogFile", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(TAG, "closeStream", e);
                }
            }
        }

    }

    public void checkGoogleDriveSignIn(){
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, you can show user info here
            Log.d(TAG, "User email: " + currentUser.getEmail());
        } else {
            signIn();
        }
    }

    // Method to start the Google Sign-In flow
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        this.activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Method that will run once the user is signed in
    public void onSignInResult(Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.w("MainActivity", "Google sign in failed", e);
        }
    }

    // Authenticate with Firebase using Google ID Token
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.activity, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success, update UI with the signed-in user's information
                        currentUser = mAuth.getCurrentUser();
                    } else {
                        // If sign-in fails, display a message to the user.
                        Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                        currentUser = null;
                    }
                });
    }

    public static class DotLogFile {

        private final String loggerFileName;
        private final File loggerFileAddress;
        private final DotLogger dotLogger;

        public DotLogFile(String loggerFileName, File loggerFileAddress, DotLogger dotLogger){
            this.loggerFileName = loggerFileName;
            this.loggerFileAddress = loggerFileAddress;
            this.dotLogger = dotLogger;

            //Add the Dot Log File To The Dot Log File Array List
            dotLogFileList.add(this);
        }

        public String getLoggerFileName() {
            return loggerFileName;
        }

        public File getLoggerFileAddress() {
            return loggerFileAddress;
        }

        public DotLogger getDotLogger() {
            return dotLogger;
        }
    }

    public void uploadFilesToFirebaseCloudStorage(UserInterface userInterfaceCalledFrom){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        int totalFilesToUpload = dotLogFileList.size() + fileArrayList.size();
        Log.d(TAG, "Files: " + totalFilesToUpload);
        int filesUploaded = 0;

        //Show the loading page
        loadingWindowUI.showPage();

        loadingWindowUI.startLoadingPage("Uploading " + totalFilesToUpload + " Files to Cloud...", new LoadingWindowUI.LoadingPageListener() {
            @Override
            public void onLoadingPageFinished() {
                userInterfaceCalledFrom.showPage();
            }

            @Override
            public void onLoadingPageCancelled() {
                userInterfaceCalledFrom.showPage();

            }
        });

        //Pause for 100 milliseconds between each file upload
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
        }

        for (DotLogFile dotLogFile : dotLogFileList) {
            Uri file = Uri.fromFile(dotLogFile.getLoggerFileAddress());
            String fileName = dotLogFile.getLoggerFileName();
            StorageReference fileReference = storageReference.child("userFiles/" + uid + "/Colby Walking Study Trial Files/" + folderName + "/Movella Dot Data Logs/" + fileName);
            UploadTask uploadTask = fileReference.putFile(file);

            //Each file has its own file number, in ascending order
            int fileNumber = ++filesUploaded;

            uploadTask.addOnSuccessListener(taskSnapshot -> {

                writeToLogFile("Uploaded File: " + fileName);
                loadingWindowUI.updateInnerSpinnerText((double) fileNumber / totalFilesToUpload + "%");

            }).addOnFailureListener(e -> {

                writeToLogFile("Error Uploading File: " + fileName);
                UserInterface.errorMessagePopUp("Error Uploading File(s) to Cloud", activity);

            });

            //Pause for 100 milliseconds between each file upload
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

        }

        for (File logTextFile : fileArrayList) {
            Uri file = Uri.fromFile(logTextFile);
            String fileName = file.getLastPathSegment();
            StorageReference fileReference = storageReference.child("userFiles/" + uid + "/Colby Walking Study Trial Files/" + folderName + "/Log Text Files/" + fileName);
            UploadTask uploadTask = fileReference.putFile(file);

            //Each file has its own file number, in ascending order
            int fileNumber = ++filesUploaded;

            uploadTask.addOnSuccessListener(taskSnapshot -> {

                writeToLogFile("Uploaded File: " + fileName);
                loadingWindowUI.updateInnerSpinnerText((double) fileNumber / totalFilesToUpload + "%");

            }).addOnFailureListener(e -> {

                writeToLogFile("Error Uploading File: " + fileName);
                UserInterface.errorMessagePopUp("Error Uploading File(s) to Cloud", activity);

            });

            //Pause for 100 milliseconds between each file upload
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

        }

        loadingWindowUI.onLoadingComplete();

    }

}
