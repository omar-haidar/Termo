package dev.omar.termo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;
import com.termux.view.TerminalView;
import com.termux.view.TerminalViewClient;

import java.util.Map;

import dev.omar.termo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements TerminalViewClient, TerminalSessionClient {
    private TerminalView terminalView;
    private TerminalSession terminalSession;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        terminalView = new TerminalView(this, null);
        terminalView.setTerminalViewClient(this);
        terminalView.attachSession(createSession(getFilesDir().getAbsolutePath()));
        terminalView.setKeepScreenOn(true);
        terminalView.setTextSize(24);
        terminalView.setLayoutParams(createLayoutParams(-1, -1, 1));
        binding.root.addView(terminalView, 0);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }


    private ViewGroup.LayoutParams createLayoutParams(int width, int h, int weight) {
        return new LinearLayout.LayoutParams(width, h, weight);
    }

    private TerminalSession createSession(String dir) {
        Map<String, String> environment = System.getenv();
        String[] env = new String[environment.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            env[index] = entry.getKey() + "=" + entry.getValue();
            index++;
        }
        terminalSession = new TerminalSession("/system/bin/sh", dir, new String[]{"ls"}, env, TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS, this);
        return terminalSession;
    }


    @Override
    public void onTextChanged(@NonNull TerminalSession changedSession) {
        terminalView.onScreenUpdated();
    }

    @Override
    public void onTitleChanged(@NonNull TerminalSession changedSession) {

    }

    @Override
    public void onSessionFinished(@NonNull TerminalSession finishedSession) {
        finish();
    }

    @Override
    public void onCopyTextToClipboard(@NonNull TerminalSession session, String text) {
        ClipboardUtils.copyText("Termo", text);
    }

    @Override
    public void onPasteTextFromClipboard(@Nullable TerminalSession session) {

    }

    @Override
    public void onBell(@NonNull TerminalSession session) {

    }

    @Override
    public void onColorsChanged(@NonNull TerminalSession session) {

    }

    @Override
    public void onTerminalCursorStateChange(boolean state) {

    }

    @Override
    public void setTerminalShellPid(@NonNull TerminalSession session, int pid) {

    }

    @Override
    public Integer getTerminalCursorStyle() {
        return TerminalEmulator.DEFAULT_TERMINAL_CURSOR_STYLE;
    }


    @Override
    public float onScale(float scale) {
        if (scale < 0.9f || scale > 1.1f) {
            boolean increase = scale > 1.0f;
            changeFontSize(increase);
            return 1.0f;
        }
        return scale;
    }

    private void changeFontSize(boolean increase) {
        int fontSize = 24;
        fontSize += (increase ? 1 : -1) * 2;
        terminalView.setTextSize(fontSize);

    }


    @Override
    public void onSingleTapUp(MotionEvent e) {
        showSoftInput();
    }

    private void showSoftInput() {
        if (terminalView != null) {
            terminalView.requestFocus();
            KeyboardUtils.showSoftInput(terminalView);
        }
    }


    @Override
    public boolean shouldBackButtonBeMappedToEscape() {
        return false;
    }

    @Override
    public boolean shouldEnforceCharBasedInput() {
        return true;
    }

    @Override
    public boolean shouldUseCtrlSpaceWorkaround() {
        return false;
    }

    @Override
    public boolean shouldSupportClipboardKeybindings() {
        return false;
    }

    @Override
    public boolean isTerminalViewSelected() {
        return true;
    }

    @Override
    public void copyModeChanged(boolean copyMode) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && !terminalSession.isRunning()) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongPress(MotionEvent event) {
        return false;
    }

    @Override
    public boolean readControlKey() {
        return false;
    }

    @Override
    public boolean readAltKey() {
        return false;
    }

    @Override
    public boolean readShiftKey() {
        return false;
    }

    @Override
    public boolean readFnKey() {
        return false;
    }

    @Override
    public boolean onCodePoint(int codePoint, boolean ctrlDown, TerminalSession session) {
        return false;
    }

    @Override
    public void onEmulatorSet() {
        setTerminalCursorBlinkingState(true);
    }

    private void setTerminalCursorBlinkingState(boolean b) {
        if (terminalView != null && terminalView.mEmulator != null) {
            terminalView.setTerminalCursorBlinkerState(b, true);
        }
    }

    @Override
    public void logError(String tag, String message) {

    }

    @Override
    public void logWarn(String tag, String message) {

    }

    @Override
    public void logInfo(String tag, String message) {

    }

    @Override
    public void logDebug(String tag, String message) {

    }

    @Override
    public void logVerbose(String tag, String message) {

    }

    @Override
    public void logStackTraceWithMessage(String tag, String message, Exception e) {

    }

    @Override
    public void logStackTrace(String tag, Exception e) {

    }
}