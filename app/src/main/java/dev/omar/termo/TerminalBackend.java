package dev.omar.termo;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;

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
import dev.omar.termo.content.ITerminalContext;

public class TerminalBackend implements TerminalViewClient, TerminalSessionClient {
    private ITerminalContext terminalContext;
    private TerminalView terminalView;
    public TerminalBackend(ITerminalContext terminalContext) {
        this.terminalContext = terminalContext;
        this.terminalView = terminalContext.getTerminalView();
    }

    private int fontSize = 24;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onTextChanged(@NonNull TerminalSession changedSession) {
        terminalContext.getTerminalView().onScreenUpdated();
    }

    @Override
    public void onTitleChanged(@NonNull TerminalSession changedSession) {}

    @Override
    public void onSessionFinished(@NonNull TerminalSession finishedSession) {
        if (terminalContext.getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) terminalContext.getContext()).finish();
        }
    }

    @Override
    public void onCopyTextToClipboard(@NonNull TerminalSession session, String text) {
        ClipboardUtils.copyText("Termo", text);
    }

    @Override
    public void onPasteTextFromClipboard(@Nullable TerminalSession session) {}

    @Override
    public void onBell(@NonNull TerminalSession session) {}

    @Override
    public void onColorsChanged(@NonNull TerminalSession session) {}

    @Override
    public void onTerminalCursorStateChange(boolean state) {}

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
        int fontSize = this.fontSize;
        fontSize += (increase ? 1 : -1) * 2;
        fontSize = Math.max(12, Math.min(fontSize, 32));
        terminalContext.getTerminalView().setTextSize(fontSize);
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
    public boolean isTerminalViewSelected() {
        return true;
    }

    @Override
    public void copyModeChanged(boolean copyMode) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
                && !terminalContext.getTerminalSession().isRunning()) {
            finish();
            return true;
        }
        return false;
    }

    private void finish() {
        if (terminalContext.getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) terminalContext.getContext()).finish();
        }
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
    public void logError(String tag, String message) {}

    @Override
    public void logWarn(String tag, String message) {}

    @Override
    public void logInfo(String tag, String message) {}

    @Override
    public void logDebug(String tag, String message) {}

    @Override
    public void logVerbose(String tag, String message) {}

    @Override
    public void logStackTraceWithMessage(String tag, String message, Exception e) {}

    @Override
    public void logStackTrace(String tag, Exception e) {}
}
