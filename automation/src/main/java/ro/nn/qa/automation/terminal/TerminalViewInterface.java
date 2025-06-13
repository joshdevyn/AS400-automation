package ro.nn.qa.automation.terminal;

import ro.nn.qa.automation.terminal.AS400Screen;
import ro.nn.qa.automation.terminal.AS400Terminal;
import ro.nn.qa.bootstrap.ControllerEvent;
import ro.nn.qa.bootstrap.ControllerListener;

/**
 * Modernized TerminalViewInterface using AS400Terminal.
 */
public abstract class TerminalViewInterface {
    protected static AS400Terminal terminal;
    protected static int sequence;
    protected int frameSeq;

    public TerminalViewInterface(AS400Terminal terminalInstance) {
        terminal = terminalInstance;
    }

    public int getFrameSequence() {
        return this.frameSeq;
    }

    public abstract void addSessionView(String sessionId, AS400Screen screen);

    public abstract void removeSessionView(AS400Screen screen);

    public abstract boolean containsSession(AS400Screen screen);

    public abstract int getSessionViewCount();

    public abstract AS400Screen getSessionAt(int index);

    public abstract void onSessionJump(ControllerEvent event);

    public abstract void onSessionChanged(ControllerEvent event);

    public abstract void closeSession();

    public abstract AS400Screen getScreen();
}
