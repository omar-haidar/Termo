package dev.omar.termo.content;

import android.content.Context;
import com.termux.terminal.TerminalSession;
import com.termux.view.TerminalView;

public interface ITerminalContext {

    public TerminalView getTerminalView();

    public TerminalSession getTerminalSession();

    public Context getContext();
}
