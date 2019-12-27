package org.redstudios.objecthunt;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PlayGamesAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.redstudios.objecthunt.model.AppState;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInTAG";
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private GamesClient gamesClient;
    private static final int RC_SIGN_IN = 1;
    private static final int PERMISSIONS_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (!isNetworkConnected()) {
            Log.e("SignInTAG", "No network connected.");
            startErrorDialogNetworkError();
        } else {
            if (hasPermission()) {
                startSignIn();
            } else {
                requestPermission();
            }
        }
    }

    private void startSignIn() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInSilently();
    }

    private void launchApplication() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        AppState.get().setFirebaseFirestore(firebaseFirestore);
        FirebaseUser user = AppState.get().getActiveUser();

        DocumentReference userDocument = firebaseFirestore.collection("users").document(user.getUid());

        userDocument.get().addOnCompleteListener((@NonNull Task<DocumentSnapshot> task) -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Log.d(TAG, "No such user, creating it.");
                    HashMap<String, Object> dataStructure = new HashMap<>();
                    dataStructure.put("nickName", user.getDisplayName());
                    dataStructure.put("topScore", 0);
                    dataStructure.put("objectsFound", new HashMap<>());
                    firebaseFirestore.collection("users").document(user.getUid()).set(dataStructure);
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
        AppState.get().setUserDocument(userDocument);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSignIn();
            } else {
                requestPermission();
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                        SignInActivity.this,
                        "Camera permission is required for this app.",
                        Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /*Sign in*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.i("SignInTAG", "Getting sign in intent");
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                printAcc(account);
                firebaseAuthWithPlayGames(account);
            }
        } catch (ApiException e) {
            Log.e("SignInTAG", "signInResult:failed code=" + e.getStatusCode());
            if (e.getStatusCode() == NETWORK_ERROR) {
                startErrorDialogNetworkError();
            }
        }
    }

    private void signInSilently() {
        mGoogleSignInClient
                .silentSignIn()
                .addOnCompleteListener(
                        this, (@NonNull Task<GoogleSignInAccount> task) -> {
                            if (task.isSuccessful()) {
                                // The signed in account is stored in the task's result.
                                GoogleSignInAccount signedInAccount = task.getResult();
                                Log.d("SignInTAG", "Signed in silently");
                                if (signedInAccount != null) {
                                    printAcc(signedInAccount);
                                    gamesClient = Games.getGamesClient(this, signedInAccount);
                                    gamesClient.setViewForPopups(findViewById(R.id.signInView)).addOnCompleteListener(
                                            this,
                                            (@NonNull Task<Void> task2) -> {
                                                if (task2.isSuccessful()) {
                                                    // The signed in account is stored in the task's result.
                                                    Log.d(TAG, "Popup !");
                                                    firebaseAuthWithPlayGames(signedInAccount);
                                                }
                                            });
                                } else {
                                    toastError("Authentication error.");
                                }
                            } else {
                                Log.d("SignInTAG", "Signing in with interface");
                                startSignInIntent();
                            }
                        });
    }

    private void startSignInIntent() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void firebaseAuthWithPlayGames(@NonNull GoogleSignInAccount acct) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        String credentials = acct.getServerAuthCode();
        Log.d(TAG, "Credentials : " + credentials);
        AuthCredential credential = PlayGamesAuthProvider.getCredential(credentials == null ? "" : credentials);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignInTAG", "signInWithCredential:success");
                        FirebaseUser user = auth.getCurrentUser();
                        AppState.get().setActiveUser(user);
                        launchApplication();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e("SignInTAG", "signInWithCredential:failure", task.getException());
                        toastError("Authentication failed.");
                    }
                });
    }

    private void printAcc(@NonNull GoogleSignInAccount account) {
        Log.d("SignInTAG", "firebaseAuthWithPlayGames:" + account.getId());
        Log.d("SignInTAG", "firebaseAuthWithPlayGames:" + account.getEmail());
        Log.d("SignInTAG", "firebaseAuthWithPlayGames:" + account.getServerAuthCode());
    }

    /*ERROR HANDLING*/

    private void toastError(String message) {
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void startErrorDialogNetworkError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this, R.style.AlertDialogStyle);
        builder.setTitle("Network Error");
        builder.setMessage("Please connect your device to the internet and retry.");
        DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                Log.d("SignInTAG", "Restarting app");
                Intent mStartActivity = new Intent(SignInActivity.this, SignInActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(SignInActivity.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) SignInActivity.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 20, mPendingIntent);
                System.exit(0);
            }
        };

        DialogInterface.OnDismissListener dismissListener = (new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                System.exit(0);
            }
        });

        builder.setPositiveButton("Restart app", dialogClickListener);
        builder.setOnDismissListener(dismissListener);

        AlertDialog dialog = builder.create();

        // Display the alert dialog on interface
        dialog.show();
    }

}
