package dev.omar.termo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.google.android.material.textview.MaterialTextView;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;
import com.termux.view.TerminalView;
import com.termux.view.TerminalViewClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.omar.common.virtualkeys.VirtualKeyButton;
import dev.omar.common.virtualkeys.VirtualKeysView;
import dev.omar.termo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements TerminalViewClient, TerminalSessionClient, VirtualKeysView.IVirtualKeysView {
    private TerminalView terminalView;
    private TerminalSession terminalSession;
    private ActivityMainBinding binding;
    private Process terminalProcess;
    private OutputStream terminalInputWriter;

    private MaterialTextView terminalOutput;
    private int fontSize = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        terminalOutput = binding.txtLog;
        terminalView = new TerminalView(this, null);
        terminalView.setTerminalViewClient(this);
        terminalView.attachSession(createSession(getFilesDir().getAbsolutePath()));
        terminalView.setKeepScreenOn(true);
        terminalView.setTextSize(24);
        terminalView.setLayoutParams(createLayoutParams(-1, -1, 1));
        binding.root.addView(terminalView, 0);
        binding.virtualKeyTable.setVirtualKeysViewClient(this);
        checkAndInitializeEnvironment();

    }


    private void checkAndInitializeEnvironment() {

        File usrFolder = new File(getFilesDir(), "usr");

        if (!usrFolder.exists()) {
            // البيئة غير موجودة، نفتح نافذة انتظار ونبدأ التثبيت
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("تهيئة النظام");
            progressDialog.setMessage("جاري تنزيل وتثبيت حزمة الـ Bootstrap الأساسية، يرجى الانتظار...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            BootstrapInstaller installer = new BootstrapInstaller(this);

            // استدعاء الـ CompletableFuture
            installer.install().thenAccept(success -> {
                // العودة لواجهة المستخدم لإغلاق الـ Dialog وتشغيل الطرفية
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(MainActivity.this, "تم التثبيت بنجاح!", Toast.LENGTH_SHORT).show();
                        launchTerminalSession();
                    } else {
                        //terminalOutput.setText("فشلت عملية تهيئة الـ Bootstrap. تحقق من اتصال الإنترنت وأعد تشغيل التطبيق.");
                    }
                });
            }).exceptionally(throwable -> {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    //terminalOutput.setText("خطأ غير متوقع: " + throwable.getMessage());
                });
                return null;
            });

        } else {

            launchTerminalSession();
        }
    }

    private void launchTerminalSession() {
        terminalOutput.append("جاري تشغيل بيئة XTerminal عبر PRoot...\n");

        CompletableFuture.runAsync(() -> {
            try {
                dev.omar.termo.TerminalSession session = new dev.omar.termo.TerminalSession(this);
                // تشغيل عملية الـ PRoot والـ Bash
                terminalProcess = session.startTerminal();

                // الإمساك بـ مجرى الإدخال لإرسال أوامر لاحقاً (مثل apt update)
                terminalInputWriter = terminalProcess.getOutputStream();

                // قراءة مخرجات الطرفية في الخلفية لعرضها على الشاشة
                BufferedReader reader = new BufferedReader(new InputStreamReader(terminalProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    final String outputLine = line;
                    runOnUiThread(() -> terminalOutput.append(outputLine + "\n"));
                }

            } catch (Exception e) {
                runOnUiThread(() -> terminalOutput.append("\nخطأ أثناء تشغيل الجلسة: " + e.getMessage()));
            }
        });
    }

    public void executeCommand(String command) {
        if (terminalInputWriter != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    terminalInputWriter.write((command + "\n").getBytes());
                    terminalInputWriter.flush();
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "فشل إرسال الأمر", Toast.LENGTH_SHORT).show());
                }
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


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }


    private ViewGroup.LayoutParams createLayoutParams(int width, int h, int weight) {
        LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(width, h);
        lpp.weight = 1.0f;
        return  lpp;
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
        int fontSize = this.fontSize;
        fontSize += (increase ? 1 : -1) * 2;
        fontSize = Math.max(12, Math.min(fontSize, 32));
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