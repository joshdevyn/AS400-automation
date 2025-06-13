package ro.nn.qa.bootstrap;

/**
 * Event class for Controller
 * Replaces TN5250j BootEvent
 */
public class ControllerEvent {
    
    private Object source;
    private String message;
    private long timestamp;
    
    /**
     * Constructor
     * @param source The source object that generated the event
     * @param message The event message
     */
    public ControllerEvent(Object source, String message) {
        this.source = source;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the source object
     * @return The source object
     */
    public Object getSource() {
        return source;
    }
      /**
     * Get the event message
     * @return The event message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get new session options (equivalent to getNewSessionOptions in TN5250j)
     * @return The session options message
     */
    public String getNewSessionOptions() {
        return message;
    }
    
    /**
     * Get the timestamp when the event was created
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "ControllerEvent{" +
                "source=" + source +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
