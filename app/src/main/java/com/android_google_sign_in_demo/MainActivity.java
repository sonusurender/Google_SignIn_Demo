package com.android_google_sign_in_demo;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 111;//google sign in request code

    private GoogleSignInClient mGoogleSignInClient;//google sign in client

    private SignInButton defaultSignInButton;
    private Button customSignInButton;

    private TextView userDetailLabel;
    private ImageView userProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        configureGoogleSignIn();
    }

    /**
     * find all views and implement click event over default sign in button
     */
    private void findViews() {

        defaultSignInButton = findViewById(R.id.default_google_sign_in_button);
        customSignInButton = findViewById(R.id.custom_sign_in_button);

        userDetailLabel = findViewById(R.id.user_details_label);
        userProfileImageView = findViewById(R.id.user_profile_image_view);

        defaultSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSignInSignOut();
            }
        });
    }

    /**
     * configure google sign in
     */
    private void configureGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()//request email id
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //SILENT SIGN IN
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //update the UI if user has already sign in with the google for this app
        getProfileInformation(account);
    }

    /**
     * custom sign in button click event
     *
     * @param view custom button
     */
    public void customGoogleSignIn(View view) {
        doSignInSignOut();
    }

    /**
     * method to do Sign In or Sign Out on the basis of account exist or not
     */
    private void doSignInSignOut() {

        //get the last sign in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        //if account doesn't exist do login else do sign out
        if (account == null)
            doGoogleSignIn();
        else
            doGoogleSignOut();
    }

    /**
     * do google sign in
     */
    private void doGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);//pass the declared request code here
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            //method to handle google sign in result
            handleSignInResult(task);
        }
    }

    /**
     * method to handle google sign in result
     *
     * @param completedTask from google onActivityResult
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            getProfileInformation(account);

            //show toast
            Toast.makeText(this, "Google Sign In Successful.", Toast.LENGTH_SHORT).show();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());

            //show toast
            Toast.makeText(this, "Failed to do Sign In : " + e.getStatusCode(), Toast.LENGTH_SHORT).show();

            //update Ui for this
            getProfileInformation(null);
        }
    }

    /**
     * method to fetch user profile information from GoogleSignInAccount
     *
     * @param acct googleSignInAccount
     */
    private void getProfileInformation(GoogleSignInAccount acct) {
        //if account is not null fetch the information
        if (acct != null) {

            //user display name
            String personName = acct.getDisplayName();

            //user first name
            String personGivenName = acct.getGivenName();

            //user last name
            String personFamilyName = acct.getFamilyName();

            //user email id
            String personEmail = acct.getEmail();

            //user unique id
            String personId = acct.getId();

            //user profile pic
            Uri personPhoto = acct.getPhotoUrl();

            //show the user details
            userDetailLabel.setText("ID : " + personId + "\nDisplay Name : " + personName + "\nFull Name : " + personGivenName + " " + personFamilyName + "\nEmail : " + personEmail);

            //show the user profile pic
            Picasso.with(this).load(personPhoto).fit().placeholder(R.mipmap.ic_launcher_round).into(userProfileImageView);

            //change the text of Custom Sign in button to sign out
            customSignInButton.setText(getResources().getString(R.string.sign_out));

            //show the label and image view
            userDetailLabel.setVisibility(View.VISIBLE);
            userProfileImageView.setVisibility(View.VISIBLE);

        } else {

            //if account is null change the text back to Sign In and hide the label and image view
            customSignInButton.setText(getResources().getString(R.string.sign_in));
            userDetailLabel.setVisibility(View.GONE);
            userProfileImageView.setVisibility(View.GONE);

        }
    }

    /**
     * method to do google sign out
     * This code clears which account is connected to the app. To sign in again, the user must choose their account again.
     */
    private void doGoogleSignOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Google Sign Out done.", Toast.LENGTH_SHORT).show();
                        revokeAccess();
                    }
                });
    }

    /**
     * DISCONNECT ACCOUNTS
     * method to revoke access from this app
     * call this method after successful sign out
     * <p>
     * It is highly recommended that you provide users that signed in with Google the ability to disconnect their Google account from your app. If the user deletes their account, you must delete the information that your app obtained from the Google APIs
     */
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Google access revoked.", Toast.LENGTH_SHORT).show();
                        getProfileInformation(null);
                    }
                });
    }
}
