package com.example.haendchen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.room.Room;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements BluetoothConnectionListener {
    boolean permissions_granted = false;
    boolean device_connected = false;
    boolean nightMode;
    AppDatabase appDatabase;
    User user;
    BLE ble;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    SwitchMaterial switchNightMode;
    Button btnConnectDevice;
    Button btnManualControl;
    Button btnGripPatterns;
    SwitchCompat loginSignupSwitch;
    Button btnSignup;
    Button btnLogin;
    Button btnLogout;
    View loginSignupOverlay;
    View loginForm;
    View signupForm;
    TextView loginFormText;
    TextView userGreetingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        boolean isUser_logged_in = prefs.getBoolean("isUserLoggedIn", false); // false is the default value

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "db").allowMainThreadQueries().build();
        ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);

        loginSignupSwitch = findViewById(R.id.loginSignupToggle);
        btnSignup = findViewById(R.id.signupButton);
        btnLogin = findViewById(R.id.loginButton);
        btnLogout = findViewById(R.id.logoutButton);
        loginSignupOverlay = findViewById(R.id.loginSignupOverlayPanel);
        loginForm = findViewById(R.id.loginForm);
        signupForm = findViewById(R.id.signupForm);
        loginFormText = findViewById(R.id.LoginFormText);
        userGreetingText = findViewById(R.id.UserNameGreeting);
        btnConnectDevice = findViewById(R.id.connect_device_btn);
        btnManualControl = findViewById(R.id.manual_control_btn);
        btnGripPatterns = findViewById(R.id.grip_patterns_btn);

        if (isUser_logged_in) {
            String username = prefs.getString("Username", "User");
            userGreetingText.setText("Hello, " + username);
            loginSignupOverlay.setVisibility(View.GONE);
            btnConnectDevice.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);

        } else {
            loginSignupOverlay.setVisibility(View.VISIBLE);
            btnConnectDevice.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
        }

        OnBackPressedCallback callbackMainBack = new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                finish();
            }};

        getOnBackPressedDispatcher().addCallback(this, callbackMainBack);

        SwitchCompat switchNightMode = findViewById(R.id.dark_light_switch);
        sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", false);

            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", true);


            }
            editor.apply();
        });

        updateUIBasedOnConnectionStatus();

        btnConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setOrientationLocked(true);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan QR Code on device");
                integrator.initiateScan();
            }

        });


        btnLogin.setOnClickListener(v -> {
            String username = ((TextView) findViewById(R.id.loginUsername)).getText().toString();
            String password = ((TextView) findViewById(R.id.loginPassword)).getText().toString();

            login(username, password, success -> {
                runOnUiThread(() -> {
                    if(success) {
                        // Login success
                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        loginSignupOverlay.setVisibility(View.GONE);

                        SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                        editor.putBoolean("isUserLoggedIn", true);
                        editor.putString("Username", username);
                        editor.apply();

                        btnConnectDevice.setVisibility(View.VISIBLE);
                        btnLogout.setVisibility(View.VISIBLE);

                    } else {
                        // Login failure
                        Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();

                    }
                });
            });
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("isUserLoggedIn", false);
            editor.apply();
            loginSignupOverlay.setVisibility(View.VISIBLE); // Show login overlay again
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            userGreetingText.setText("Logging in...");
            btnConnectDevice.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);

        });


        btnSignup.setOnClickListener(v -> {
            String username = ((TextView) findViewById(R.id.signupUsername)).getText().toString();
            String password = ((TextView) findViewById(R.id.signupPassword)).getText().toString();
            signup(username, password, success -> {
                runOnUiThread(() -> {
                    if(success) {
                        // Signup success
                        Toast.makeText(MainActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        // Signup failure
                        Toast.makeText(MainActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });


        // Inside your overlay handling method
        loginSignupSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (!isChecked) {
                loginFormText.setText("Login");
                loginForm.setVisibility(View.VISIBLE);
                signupForm.setVisibility(View.GONE);
            }
            else {
                loginFormText.setText("Create user");
                loginForm.setVisibility(View.GONE);
                signupForm.setVisibility(View.VISIBLE);
            }
        });



        btnManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent manualControlIntent = new Intent(MainActivity.this, ManualControllActivity.class);
                startActivity(manualControlIntent);

            }
        });

        btnGripPatterns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gripPatternsIntent = new Intent(MainActivity.this, GripPatternsActivity.class);
                startActivity(gripPatternsIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                if (checkAndRequestPermissions()) {
                    if (ble.bluetoothAdapter != null && !ble.bluetoothAdapter.isEnabled()) {
                        Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(enableBleIntent);
                    } else {
                        Intent scanResultIntent = new Intent(MainActivity.this, ConnectDeviceActivity.class);
                        scanResultIntent.putExtra("QR_CODE_CONTENT", result.getContents()); // Pass the QR code content
                        startActivity(scanResultIntent);
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateUIBasedOnConnectionStatus() {

        if (BLE.getInstance(this).isConnected()) {

            btnManualControl.setVisibility(View.VISIBLE);
            btnGripPatterns.setVisibility(View.VISIBLE);
            btnConnectDevice.setVisibility(View.GONE);

        } else {
            btnManualControl.setVisibility(View.GONE);
            btnGripPatterns.setVisibility(View.GONE);
            btnConnectDevice.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissions_granted = checkAndRequestPermissions();
        updateUIBasedOnConnectionStatus();
    }

    private boolean checkAndRequestPermissions() {

        boolean ret = true;

        // Linked list for faster access to unique items
        LinkedList<String> permissions = new LinkedList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(permission);
                ret = false;
            }
        }
        return ret;
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showPermissionRationale(permission);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, ble.ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }

    public void login(String username, String password, UserAuthCallback callback) {
        new Thread(() -> {
            User loginUser = appDatabase.userDao().getUser(username, password);
            callback.onResult(loginUser != null); // true if user is found, false otherwise
            user = loginUser;
            userGreetingText.setText("Hello, " + username);
        }).start();
    }
    // Method to handle signup
    public void signup(String username, String password, UserAuthCallback callback) {
        new Thread(() -> {
            User existingUser = appDatabase.userDao().findUserByUsername(username);
            if(existingUser == null) {
                User newUser = new User();
                newUser.username = username;
                newUser.password = password;
                appDatabase.userDao().insertUser(newUser);
                callback.onResult(true); // Signup success
            } else {
                callback.onResult(false); // User exists
            }
        }).start();
    }


    private void showPermissionRationale(String permission) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission for Bluetooth required")
                .setTitle("Permission Required")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, ble.ENABLE_BLUETOOTH_REQUEST_CODE))

                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkAndEnableBluetooth() {

            Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                startActivity(enableBleIntent);
            }
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        runOnUiThread(() -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean isUserLoggedIn = prefs.getBoolean("isUserLoggedIn", false);
            if (isUserLoggedIn) {
                String username = prefs.getString("Username", "User");
                userGreetingText.setText("Hello, " + username);
            }
            updateUIBasedOnConnectionStatus();
        });
    }

    @Override
    public void onDataReceive(int rxData) {
        Log.d("MyApp", "Data recieved: "+ rxData);
    }

}