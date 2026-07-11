package dev.omar.termo.crash;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CrashActivity extends AppCompatActivity {

    public static final String EXTRA_CRASH_MESSAGE = "crash.extra.EXTRA_CRASH_MESSAGE";
    private String message = "No crash message!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_CRASH_MESSAGE)) {
            message = getIntent().getStringExtra(EXTRA_CRASH_MESSAGE);
        }

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle("An error occurred");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Exit", (d, i) -> finish());
        dialog.setNegativeButton("Copy", (d, i) -> copyCrashMessage());
        dialog.show();
    }

    private void copyCrashMessage() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Termo", message));
        Toast.makeText(this, "Copied!", 0).show();
    }

    public static void initCrashHandler(Application app) {
        if (app == null) return;
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, th) -> {
                    Intent i = new Intent(app.getApplicationContext(), CrashActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(EXTRA_CRASH_MESSAGE, Log.getStackTraceString(th));
                    try {
                        app.startActivity(i);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                });
    }
}
