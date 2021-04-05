package com.pleiades.pleione.pixivdownloader.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pleiades.pleione.pixivdownloader.DeviceController;
import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.client.CommonClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_SHORT;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_IS_CROWN;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_REFRESH_TOKEN;
import static com.pleiades.pleione.pixivdownloader.Config.PERMISSIONS_STORAGE;
import static com.pleiades.pleione.pixivdownloader.Config.PREFS;
import static com.pleiades.pleione.pixivdownloader.Config.REQUEST_CODE_PERMISSIONS;
import static com.pleiades.pleione.pixivdownloader.Variable.isGuest;
import static com.pleiades.pleione.pixivdownloader.Variable.isLoggedIn;
import static com.pleiades.pleione.pixivdownloader.Variable.isPermissionGranted;
import static com.pleiades.pleione.pixivdownloader.Variable.refreshToken;

public class LoginActivity extends AppCompatActivity {
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // initialize context
        activity = LoginActivity.this;

        // load ad
        if (!prefs.getBoolean(KEY_IS_CROWN, false))
            DeviceController.initializeInterstitialAd(activity);

        // set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_signature_dark_gray));

        // initialize appbar
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        // initialize token edit text
        EditText tokenEditText = findViewById(R.id.login_token);
        tokenEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tokenEditText.setRawInputType(InputType.TYPE_CLASS_TEXT); // multiline with action done
        tokenEditText.setText(prefs.getString(KEY_REFRESH_TOKEN, null));
        tokenEditText.requestFocus();

        // initialize login button
        TextView loginTextView = findViewById(R.id.login);
        loginTextView.setOnClickListener(v -> {
            // initialize refresh token
            refreshToken = tokenEditText.getText().toString();

            // initialize is guest
            isGuest = false;

            // launch login
            launchLogin();
        });

        // initialize help button
        TextView helpTextView = findViewById(R.id.login_help);
        helpTextView.setOnClickListener(v -> {
            showHelpDialog();
        });

        // initialize guest button
        TextView guestTextView = findViewById(R.id.login_guest);
        guestTextView.setOnClickListener(v -> {
            // initialize is guest
            isGuest = true;

            // launch login
            launchLogin();
        });
    }

    private void launchLogin() {
        if (!isPermissionGranted) {
            Toast.makeText(activity, R.string.toast_error_permissions, LENGTH_SHORT).show();
            return;
        }

        // initialize client
        CommonClient commonClient = new CommonClient();

        // initialize progress dialog
        ProgressDialog progressDialog = new ProgressDialog(activity, R.style.AlertDialogBuilderTheme);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.message_login));
        progressDialog.setCancelable(false);

        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            // refresh access token
            if (commonClient.refreshAccessToken()) {
                isLoggedIn = true;
                if (!isGuest) {
                    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_REFRESH_TOKEN, refreshToken);
                    editor.apply();
                }
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(activity, R.string.toast_error_login, LENGTH_SHORT).show());
            }

            // on complete
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        // show progress dialog
                        progressDialog.show();
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();

                        if (isLoggedIn) {
                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.startActivity(intent);
                            activity.finish();
                        }
                    }
                });
    }

    private void showHelpDialog() {
        // initialize builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogBuilderTheme);
        builder.setMessage(R.string.message_help_login);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String googleDriveUri = "https://drive.google.com/drive/folders/13fyy6sb9h8yixlXeO0nzrDs-e2OF9mun?usp=sharing";

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("move to drive", googleDriveUri);
            clipboard.setPrimaryClip(clip);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(googleDriveUri));
            startActivity(intent);
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // set dialog message attributes
        TextView messageTextView = dialog.findViewById(android.R.id.message);
        if (messageTextView != null) {
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_default));
            messageTextView.setLineSpacing(0f, 1.2f);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // request permissions
        for (String permission : PERMISSIONS_STORAGE) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                isPermissionGranted = true;
            else
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                isPermissionGranted = true;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}