package dev.omar.termo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andihasan7.flashbar.Flashbar;
import com.google.android.material.textview.MaterialTextView;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import dev.omar.common.virtualkeys.VirtualKeyButton;
import dev.omar.common.virtualkeys.VirtualKeysView;
import dev.omar.termo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements VirtualKeysView.IVirtualKeysView {
    private TerminalView terminalView;
    private TerminalSession terminalSession;
    private ActivityMainBinding binding;
    private Process terminalProcess;
    private OutputStream terminalInputWriter;
    private TerminalBackend backend;
    private MaterialTextView terminalOutput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        terminalOutput = binding.txtLog;
        terminalView = new TerminalView(this, null);
        backend = new TerminalBackend(terminalView);
        terminalView.setTerminalViewClient(backend);
        terminalView.attachSession(createSession(getFilesDir().getAbsolutePath()));
        terminalView.setKeepScreenOn(true);
        terminalView.setTextSize(24);
        terminalView.setLayoutParams(createLayoutParams(-1, -1, 1));
        binding.root.addView(terminalView, 0);
        binding.virtualKeyTable.setVirtualKeysViewClient(this);

        checkAndInstallAssets();

        new Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.TOP)
                .title("Update")
                .message("New version checking...")
                //.dismissOnTapOutside()
                .duration(5000)
                //.enableSwipeToDismiss()
                //.negativeActionText("Negative")
                .positiveActionText("Cancel")
                .showProgress(Flashbar.ProgressPosition.LEFT)
                .icon(ContextCompat.getDrawable(this, R.mipmap.ic_launcher))
                .build()
                .show();
    }


    private void checkAndInstallAssets() {
        if (new File(XEnvironment.LOCAL_DIR + "assets_installed").exists()) {
            Log.i("MainActivity", "Assets was installed!");
            ProotProcess prootProcess = new ProotProcess(this);
            try {
                //prootProcess.startTerminal();
                Log.e("prootProcess.startTerminal", "Terminal Was started!");

            } catch (Exception e) {
                Log.e("prootProcess.startTerminal", e.getMessage());
            }
        } else {
            AssetsInstaller.install(this)
                    .whenComplete((isSuccess, th) -> {
                        Toast.makeText(MainActivity.this, "Install success", Toast.LENGTH_LONG).show();
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // تدمير العملية عند إغلاق التطبيق لتحرير موارد الـ CPU وذاكرة الهاتف
        if (terminalProcess != null) {
            terminalProcess.destroy();
        }
    }


    private ViewGroup.LayoutParams createLayoutParams(int width, int h, int weight) {
        LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(width, h);
        lpp.weight = 1.0f;
        return lpp;
    }

    private TerminalSession createSession(String dir) {
        Map<String, String> environment = new HashMap<String, String>();
        environment.put("TERM", "xterm-256color");
        environment.put("PREFIX", XEnvironment.PREFIX);
        environment.put("HOME", XEnvironment.HOME);
        environment.put("BOOTSTRAP_PREFIX", XEnvironment.PREFIX);
        environment.put("PATH", "/data/data/dev.omar.termo/files/usr/bin:/data/data/dev.omar.termo/files/usr/bin/applets");
        environment.put("LD_LIBRARY_PATH", XEnvironment.LOCAL_DIR);
        environment.put("TMPDIR", "/data/data/dev.omar.termo/files/usr/tmp");
        String[] env = new String[environment.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            env[index] = entry.getKey() + "=" + entry.getValue();
            index++;
        }
        terminalSession = new TerminalSession(
                "/system/bin/sh",
                dir,
                new String[]{""},
                env,
                TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                backend);
        return terminalSession;
    }


    @Override
    public void onVirtualKeyButtonClick(View view, VirtualKeyButton buttonInfo, Button button) {
        if (buttonInfo.isMacro()) {

        } else {
            onTerminalExtraButtonClick(buttonInfo.getKey(), false, false, false, false);
        }
    }

    private void onTerminalExtraButtonClick(String key, boolean b, boolean b1, boolean b2, boolean b3) {

    }


    @Override
    public boolean performVirtualKeyButtonHapticFeedback(View view, VirtualKeyButton buttonInfo, Button button) {
        return false;
    }
}