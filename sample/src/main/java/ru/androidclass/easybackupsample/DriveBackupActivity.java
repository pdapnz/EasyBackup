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
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.androidclass.easybackup.core.BackupManager;
import ru.androidclass.easybackup.core.exception.BackupException;
import ru.androidclass.easybackup.core.exception.BackupInitializationException;
import ru.androidclass.easybackup.core.exception.RestoreException;
import ru.androidclass.easybackup.drive.DriveAppBackup;
import ru.androidclass.easybackupsample.db.DB;
import ru.androidclass.easybackupsample.db.entity.Lipsum;

public class DriveBackupActivity extends AppCompatActivity {
    private static final String TAG = DriveBackupActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private DriveAppBackup mDriveAppBackup;
    private BackupManager mBackupManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_backup);

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
        updateUI();
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
            completedTask.getResult(ApiException.class);
        } catch (ApiException e) {
            e.printStackTrace();
            Log.d(TAG, "handleSignInResult:error", e);
            toast(e.getMessage());
        }
        updateUI();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI());
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> updateUI());
    }

    private void updateUI() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(Scopes.DRIVE_APPFOLDER))) {
            ((TextView) findViewById(R.id.accountName)).setText(account.getDisplayName());
            findViewById(R.id.actualBackups).setVisibility(View.VISIBLE);
            findViewById(R.id.googleSignIn).setVisibility(View.GONE);

            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_APPDATA));
            credential.setSelectedAccount(account.getAccount());

            Drive driveService = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
            try {
                BackupManager backupManager = new BackupManager();
                mDriveAppBackup = new DriveAppBackup(
                        getApplication(),
                        driveService,
                        Collections.singletonList(getPackageName()),
                        Collections.singletonList(DATABASE_NAME),
                        Collections.singletonList("") //root internal storage directory
                );
                backupManager.addBackupCreator(() -> mDriveAppBackup);
                mBackupManager = backupManager;
            } catch (BackupInitializationException e) {
                e.printStackTrace();
                toast(e.getMessage());
            }
            updateActualBackup();
        } else {
            findViewById(R.id.actualBackups).setVisibility(View.GONE);
            findViewById(R.id.googleSignIn).setVisibility(View.VISIBLE);
            mDriveAppBackup = null;
            mBackupManager = null;
        }
    }

    private final ThreadPoolExecutor mWorkerThreadPool = new ThreadPoolExecutor(0, 4, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private void backup() {
        mWorkerThreadPool.execute(() -> {
            runOnUiThread(() -> showLoading(true));
            try {
                if (mBackupManager != null)
                    mBackupManager.backupAll();
                else
                    toast("Backup Failed!");

            } catch (BackupInitializationException | BackupException e) {
                e.printStackTrace();
                toast(e.getMessage());
            }
            updateActualBackup();
        });
    }

    private void restore() {
        showLoading(true);
        mWorkerThreadPool.execute(() -> {
            runOnUiThread(() -> showLoading(true));
            try {
                if (mBackupManager != null)
                    mBackupManager.restoreAll();
                else
                    toast("Restore Failed!");
            } catch (BackupInitializationException | RestoreException e) {
                e.printStackTrace();
                toast(e.getMessage());
            }
            updateActualBackup();
        });
    }

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void updateActualBackup() {
        showLoading(true);
        mWorkerThreadPool.execute(() -> {
            try {
                if (mDriveAppBackup != null) {
                    final com.google.api.services.drive.model.File file = mDriveAppBackup.getBackupFolder();
                    runOnUiThread(() -> {
                        if (file != null) {
                            ((TextView) findViewById(R.id.actualBackupName)).setText(format.format(new Date(file.getCreatedTime().getValue())));
                            findViewById(R.id.backupList).setVisibility(View.VISIBLE);
                            findViewById(R.id.emptyBackup).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.backupList).setVisibility(View.GONE);
                            findViewById(R.id.emptyBackup).setVisibility(View.VISIBLE);
                        }
                        showLoading(false);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                toast(e.getMessage());
            }
        });
    }

    private void toast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
    }

    private void showLoading(boolean isLoading) {
        findViewById(R.id.loading).setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
