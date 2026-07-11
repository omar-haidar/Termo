package dev.omar.termo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.andihasan7.flashbar.Flashbar;
import com.blankj.utilcode.util.ClipboardUtils;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;

import dev.omar.common.utils.XEnvironment;
import dev.omar.termo.content.ITerminalContext;
import dev.omar.termo.databinding.ActivityMainBinding;
import dev.omar.termo.utils.TerminalInstaller;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements ITerminalContext {
    private TerminalView terminalView;
    private TerminalSession terminalSession;
    private ActivityMainBinding binding;
    private Process terminalProcess;
    private TerminalBackend backend;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupTerminalView();
        
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Tools installing!");
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();
        TerminalInstaller.checkAndInstallTerminal(
                        this,
                        (msg, value) -> {
                            runOnUiThread(
                                    () -> {
                                        pd.setMessage(msg);
                                    });
                        })
                .whenComplete(
                        (result, th) -> {
                            runOnUiThread(
                                    () -> {
                                        pd.dismiss();
                                        if (!result.success) {
                                            new Flashbar.Builder(MainActivity.this)
                                                    .title("Failed to install tools!")
                                                    .message(result.message)
                                                    .gravity(Flashbar.Gravity.TOP)
                                                    .positiveActionText("Close")
                                                    .positiveActionTapListener(
                                                            (fb) -> {
                                                                finish();
                                                            })
                                                    .negativeActionText("Copy")
                                                    .negativeActionTapListener(
                                                            (fb) -> {
                                                                ClipboardUtils.copyText(
                                                                        result.message);
                                                            })
                                                    .show();
                                        }
                                    });
                        });
    }
    
    private void setupTerminalView() {
    	terminalView = new TerminalView(this, null);
        backend = new TerminalBackend(this);
        terminalView.setTerminalViewClient(backend);
        terminalView.attachSession(createSession(XEnvironment.HOME));
        terminalView.setKeepScreenOn(true);
        terminalView.setTextSize(24);
        terminalView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        binding.terminalContainer.addView(terminalView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (terminalProcess != null) {
            terminalProcess.destroy();
        }
    }

    
    private TerminalSession createSession(String dir) {
        Map<String, String> environment = XEnvironment.getEnvironment();

        String[] env = XEnvironment.envToProps(environment);

        terminalSession =
                new TerminalSession(
                        "/system/bin/sh",
                        dir,
                        new String[] {""},
                        env,
                        TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                        backend);
        return terminalSession;
    }

    @Override
    public TerminalView getTerminalView() {
        return terminalView;
    }

    @Override
    public TerminalSession getTerminalSession() {
        return terminalSession;
    }

    @Override
    public Context getContext() {
        return this;
    }
}
