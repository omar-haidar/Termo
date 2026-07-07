package dev.omar.common.virtualkeys;

import android.view.View;
import android.widget.Button;
import com.termux.terminal.TerminalSession;

public class VirtualKeysListener implements VirtualKeysView.IVirtualKeysView {

    private final TerminalSession session;

    public VirtualKeysListener(TerminalSession session) {
        this.session = session;
    }

    @Override
    public void onVirtualKeyButtonClick(View view, VirtualKeyButton buttonInfo, Button button) {
        if (buttonInfo == null || buttonInfo.getKey() == null) {
            return;
        }

        String key = buttonInfo.getKey();
        String writeable;

        switch (key) {
            case "UP":
            writeable = "\u001B[A"; // Escape sequence for Up Arrow
            break;
            case "DOWN":
            writeable = "\u001B[B"; // Escape sequence for Down Arrow
            break;
            case "LEFT":
            writeable = "\u001B[D"; // Escape sequence for Left Arrow
            break;
            case "RIGHT":
            writeable = "\u001B[C"; // Escape sequence for Right Arrow
            break;
            case "ENTER":
            writeable = "\\u000D"; // Carriage Return for Enter
            break;
            case "PGUP":
            writeable = "\u001B[5~"; // Escape sequence for Page Up
            break;
            case "PGDN":
            writeable = "\u001B[6~"; // Escape sequence for Page Down
            break;
            case "TAB":
            writeable = "\u0009"; // Horizontal Tab
            break;
            case "HOME":
            writeable = "\u001B[H"; // Escape sequence for Home
            break;
            case "END":
            writeable = "\u001B[F"; // Escape sequence for End
            break;
            case "ESC":
            writeable = "\u001B"; // Escape
            break;
            default:
            writeable = key;
            break;
        }

        if (session != null) {
            session.write(writeable);
        }
    }

    @Override
    public boolean performVirtualKeyButtonHapticFeedback(View view, VirtualKeyButton buttonInfo, Button button) {
        return false;
    }
}