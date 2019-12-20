package org.redstudios.objecthunt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PlayGamesAuthProvider;

import org.redstudios.objecthunt.model.AppState;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInSilently();
    }

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
                                    firebaseAuthWithPlayGames(signedInAccount);
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

    private void launchApplication() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void toastError(String message) {
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void startErrorDialogNetworkError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
        builder.setTitle("Network Error");
        builder.setMessage("Please connect your device to the internet and retry.");
        DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
            if (DialogInterface.BUTTON_NEUTRAL == which) {
                //TODO restart app
                Log.d("SignInTAG", "Restarting app");
            }
        };
        builder.setPositiveButton("Restart app", dialogClickListener);

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();
    }

}
