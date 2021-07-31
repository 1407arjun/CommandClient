package com.arjun1407.commandclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.constraintLayout) ConstraintLayout layout;
    @BindView(R.id.ipLayout) TextInputLayout ipLayout;
    @BindView(R.id.ipEditText) EditText ipText;
    @BindView(R.id.portLayout) TextInputLayout portLayout;
    @BindView(R.id.portEditText) EditText portText;
    @BindView(R.id.cmdLayout) TextInputLayout cmdLayout;
    @BindView(R.id.cmdEditText) EditText cmdText;
    @BindView(R.id.pinLayout) TextInputLayout pinLayout;
    @BindView(R.id.pinEditText) EditText pinText;
    @BindView(R.id.authenticate) Button auth;
    @BindView(R.id.responseText) TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {

            // this means we can use biometric sensor
            case BiometricManager.BIOMETRIC_SUCCESS:
                break;

            // this means that the device doesn't have fingerprint sensor
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Snackbar.make(layout, "Device doesn't support biometric authentication.", Snackbar.LENGTH_LONG).show();
                break;

            // this means that biometric sensor is not available
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Snackbar.make(layout, "Biometric hardware in use by some other application. Please close all apps and try again.", Snackbar.LENGTH_LONG).show();
                break;

            // this means that the device doesn't contain your fingerprint
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Snackbar.make(layout, "Biometrics has not been setup on this device.", Snackbar.LENGTH_LONG).show();
                break;

            default:
                Snackbar.make(layout, "Unknown biometric authentication error. Please try again later.", Snackbar.LENGTH_LONG).show();
                break;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        final BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence s) {
                super.onAuthenticationError(errorCode, s);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                postCmd();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Snackbar.make(layout, "Authentication failed.", Snackbar.LENGTH_LONG).show();
            }
        });

        final BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder().setTitle("Two-level Authentication")
                .setDescription("Use biometrics to authenticate").setNegativeButtonText("Cancel").build();

        auth.setOnClickListener(v -> {
            ipLayout.setError(null);
            portLayout.setError(null);
            cmdLayout.setError(null);
            pinLayout.setError(null);
            responseText.setText("");

//            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            if (getCurrentFocus() != null) {
//                getCurrentFocus().clearFocus();
//                if (getCurrentFocus().getWindowToken() != null)
//                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//            }

            if (!ipText.getText().toString().isEmpty() && !portText.getText().toString().isEmpty() &&
                    !cmdText.getText().toString().isEmpty() && !pinText.getText().toString().isEmpty()) {
                biometricPrompt.authenticate(info);
            } else {
                if (ipText.getText().toString().isEmpty())
                    ipLayout.setError("Field cannot be empty.");
                if (portText.getText().toString().isEmpty())
                    portLayout.setError("Field cannot be empty.");
                if (cmdText.getText().toString().isEmpty())
                    cmdLayout.setError("Field cannot be empty.");
                if (pinText.getText().toString().isEmpty())
                    pinLayout.setError("Field cannot be empty.");
            }
        });
    }

    private void getIp() {
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.ipify.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        API api = retrofit.create(API.class);
        Call<ResponseModel> call = api.getIp("json");
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null) {
                    ResponseModel model = response.body();
                    Log.i("myIP", model.getIp());
                    postCmd(model.getIp());
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Snackbar.make(layout, "Couldn't get your IP. " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });*/
    }

    private void postCmd() {
        Snackbar.make(layout, "Authenticated successfully.", Snackbar.LENGTH_LONG).show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ipText.getText().toString() + ":" + portText.getText().toString() + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        API api = retrofit.create(API.class);
        try {
            JSONObject object = new JSONObject();
            object.put("pin", Integer.parseInt(pinText.getText().toString()));
            object.put("cmd", cmdText.getText().toString());
            Call<JSONObject> call = api.sendCmd(object);

            call.enqueue(new Callback<JSONObject>() {
                @Override
                public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                    if (response.body() != null) {
                        Snackbar.make(layout, "Request sent to server.", Snackbar.LENGTH_LONG).show();
                        JSONObject res = response.body();
                        responseText.setText(res.toString());
                    }
                }

                @Override
                public void onFailure(Call<JSONObject> call, Throwable t) {
                    if (t.getMessage() != null)
                        Snackbar.make(layout, t.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}