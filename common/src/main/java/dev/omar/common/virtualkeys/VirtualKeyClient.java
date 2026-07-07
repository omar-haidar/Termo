package dev.omar.common.virtualkeys;

import android.view.View;
import android.widget.Button;
import com.termux.terminal.TerminalSession;
import dev.omar.common.virtualkeys.VirtualKeyButton;

public class VirtualKeyClient implements VirtualKeysView.IVirtualKeysView {

    private final TerminalSession session;

    public VirtualKeyClient(TerminalSession session) {
        this.session = session;
    }

    @Override
    public void onVirtualKeyButtonClick(View view, VirtualKeyButton buttonInfo, Button button) {
        if (buttonInfo == null) {
            return;
        }

        String key = buttonInfo.getKey();
        // التحقق من أن النص ليس فارغاً أو null (بديل لـ isNullOrEmpty في Kotlin)
        if (key == null || key.isEmpty()) {
            return;
        }

        if (session != null) {
            switch (key) {
                case "ESC":
                session.write("\u001B"); // ESC
                break;
                case "TAB":
                session.write("\u0009"); // TAB
                break;
                case "HOME":
                session.write("\u001B[H"); // HOME
                break;
                case "UP":
                session.write("\u001B[A"); // UP Arrow (ANSI escape code)
                break;
                case "DOWN":
                session.write("\u001B[B"); // DOWN Arrow (ANSI escape code)
                break;
                case "LEFT":
                session.write("\u001B[D"); // LEFT Arrow (ANSI escape code)
                break;
                case "RIGHT":
                session.write("\u001B[C"); // RIGHT Arrow (ANSI escape code)
                break;
                case "PGUP":
                session.write("\u001B[5~"); // Page Up (ANSI escape code)
                break;
                case "PGDN":
                session.write("\u001B[6~"); // Page Down (ANSI escape code)
                break;
                case "END":
                session.write("\u001B[4~"); // End (ANSI escape code, may vary)
                break;
                default:
                session.write(buttonInfo.getKey());
                break;
            }
        }
    }

    @Override
    public boolean performVirtualKeyButtonHapticFeedback(View view, VirtualKeyButton buttonInfo, Button button) {
        return false;
    }
}