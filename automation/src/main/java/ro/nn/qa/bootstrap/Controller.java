package ro.nn.qa.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by Alexandru Giurovici on 02.09.2015.
 * Modernized to remove TN5250j dependencies
 */
public class Controller extends Thread {
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private List<ControllerListener> listeners;
    private ControllerEvent event;
    public static final int CONTROLLER_PORT = 3036;
    
    // Singleton pattern to prevent multiple instances
    private static Controller instance = null;
    private static final Object lock = new Object();
    private volatile boolean shutdown = false;

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private Controller()
    {
        super("QA Controller");
        try
        {
            serverSocket = new ServerSocket(CONTROLLER_PORT);
            log.info("Controller bound to port {}", CONTROLLER_PORT);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Cannot bind automation controller to port " + CONTROLLER_PORT, e);
        }
    }
    
    /**
     * Get singleton instance of Controller
     */
    public static Controller getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new Controller();
            }
            return instance;
        }
    }
    
    /**
     * Shutdown the controller and release resources
     */
    public void shutdown() {
        synchronized (lock) {
            shutdown = true;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    log.info("Controller server socket closed");
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                log.warn("Error closing controller sockets: {}", e.getMessage());
            }
            instance = null;
            interrupt(); // Signal the thread to stop
        }
    }    public void run()
    {
        log.info("Automation Controller listening");
        while (!shutdown && !isInterrupted())
        {
            listen();
            if (!shutdown) {
                getNewSessionOptions();
                log.info("Got one");
            }
        }
        log.info("Automation Controller stopping");
    }// add a listener to list.
    public synchronized void addListener(ControllerListener listener) {

        if (listeners == null)
        {
            listeners = new ArrayList<>(3);
        }
        listeners.add(listener);
        log.info("Added new listener.");

    }    // notify all registered listeners of the event.
    private void fireBootEvent() {

        if (listeners != null) {
            for (ControllerListener target : listeners) {
                target.onControllerEvent(event);
            }
        }
    }    // Listen for a connection from another session starting.
    private void listen() {
        try
        {
            if (!shutdown && serverSocket != null && !serverSocket.isClosed()) {
                socket = serverSocket.accept();
            }
        }
        catch (IOException e) {
            if (!shutdown) { // Only log if not shutting down
                log.warn(this.getName() + ": " + e.getMessage());
            }
        }
    }// Retrieve the boot options from the other JVM wanting to start a new session.
    private void getNewSessionOptions()
    {
        try {

            BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            event = new ControllerEvent(this,in.readLine());
            System.out.println(event.getNewSessionOptions());
            fireBootEvent();
            in.close();
            socket.close();
        }
        catch (IOException e)
        {
            log.warn(e.getLocalizedMessage());
        }

    }



}
