package ru.androidclass.easybackupsample;

import static ru.androidclass.easybackupsample.db.AppDatabase.DATABASE_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.androidclass.easybackup.core.BackupManager;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.drive.DriveAppBackupCreator;
import ru.androidclass.easybackupsample.db.DB;
import ru.androidclass.easybackupsample.db.entity.Lipsum;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_CLIENT_ID")
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(view -> signIn());
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            updateUI(account);
        } else {
            updateUI(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            ((TextView) findViewById(R.id.status)).setText("Google Drive backup using account: " + account.getDisplayName());
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.status)).setText("To backup using Google Drive Please Sign In:");
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    private final ThreadPoolExecutor mWorkerThreadPool = new ThreadPoolExecutor(0, 4, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());


    private void backup() {
        mWorkerThreadPool.execute(() -> {
            try {
                getBackupManager().backupAll();
            } catch (BackupInitializationException e) {
                e.printStackTrace();
                toast("Initialization Failed!");
            } catch (BackupException e) {
                e.printStackTrace();
                toast("Backup Failed!");
            }
        });
    }

    private void restore() {
        mWorkerThreadPool.execute(() -> {
            try {
                getBackupManager().restoreAll();
            } catch (BackupInitializationException e) {
                e.printStackTrace();
                toast("Initialization Failed!");
            } catch (RestoreException e) {
                e.printStackTrace();
                toast("Restore Failed!");
            }
        });
    }

    private BackupManager getBackupManager() throws BackupInitializationException {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_APPDATA));
            credential.setSelectedAccount(account.getAccount());

            Drive driveService = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
            BackupManager backupManager = new BackupManager();
            backupManager.addBackupCreator(new DriveAppBackupCreator(
                    getApplication(),
                    driveService,
                    Collections.singletonList(getPackageName()),
                    Collections.singletonList(DATABASE_NAME),
                    Collections.singletonList(getFilesDir().getPath()))
            );
            return backupManager;
        }
        toast("To make backup please sign in");
        throw new BackupInitializationException(new Throwable("To make backup please sign in"));
    }

    public void toast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
    }
}
