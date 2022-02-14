package ru.androidclass.easybackupsample;

import static ru.androidclass.easybackupsample.db.AppDatabase.DATABASE_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ru.androidclass.easybackup.core.BackupManager;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.drive.DriveAppBackupCreator;
import ru.androidclass.easybackupsample.db.DB;
import ru.androidclass.easybackupsample.db.entity.Lipsum;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class DriveActivity extends AppCompatActivity {
    private static final String TAG = DriveActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        findViewById(R.id.backupButton).setOnClickListener(view -> backup());
        findViewById(R.id.restoreButton).setOnClickListener(view -> restore());

        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        preferences.edit().putString("drive_test_key", String.valueOf(Calendar.getInstance().getTime())).apply();

        DB db = new DB(getApplication());
        List<Lipsum> lipsums = db.getDB().lipsumDao().getLipsums();
        Log.d(TAG, "lipsums " + lipsums);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("459823461429-0tm1thnvd20uv9dkc54qd61bsndqs5t4.apps.googleusercontent.com")
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        // [END customize_button]

        signInButton.setOnClickListener(view -> signIn());
    }

    @Override
    public void onStart() {
        super.onStart();


        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            //updateUI(account);
        } else {
            //updateUI(null);
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        try {
            // Signed in successfully, show authenticated U
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // updateUI(account);
        } catch (ApiException e) {
            // Signed out, show unauthenticated UI.
            Log.w(TAG, "handleSignInResult:error", e);
            //updateUI(null);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // [START_EXCLUDE]
                //updateUI(null);
                // [END_EXCLUDE]
            }
        });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        //updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    private void backup() {
        try {
            getBackupManager().backupAll();
        } catch (BackupInitializationException e) {
            e.printStackTrace();
            Toast.makeText(this, "Initialization Failed!", Toast.LENGTH_LONG).show();
        } catch (BackupException e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup Failed!", Toast.LENGTH_LONG).show();
        }
    }

    private void restore() {
        try {
            getBackupManager().restoreAll();
        } catch (BackupInitializationException e) {
            e.printStackTrace();
            Toast.makeText(this, "Initialization Failed!", Toast.LENGTH_LONG).show();
        } catch (RestoreException e) {
            e.printStackTrace();
            Toast.makeText(this, "Restore Failed!", Toast.LENGTH_LONG).show();
        }
    }

    private BackupManager getBackupManager() throws BackupInitializationException {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            Toast.makeText(this, "Чтобы сделать бекап - войдите", Toast.LENGTH_LONG).show();
            throw new BackupInitializationException(new Throwable("Please sign in"));
        }

        //Credential credential = new GoogleAccountCredential.usingOAuth2(this, Collections.singletonList(DriveScopes.DRIVE_FILE));
        //String token = GoogleAuthUtil.getToken(this, account.getAccount(), DriveScopes.DRIVE_FILE);
        //GoogleCredential credential = new GoogleCredential().setAccessToken(token);

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccountName(account.getEmail());

        Drive driveService = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(getString(R.string.app_name))
                .build();
        BackupManager backupManager = new BackupManager();
        backupManager.addBackupCreator(new DriveAppBackupCreator(
                getApplication(),
                driveService,
                Collections.singletonList(getPackageName()),
                Collections.singletonList(DATABASE_NAME))
        );
        return backupManager;

    }

}
