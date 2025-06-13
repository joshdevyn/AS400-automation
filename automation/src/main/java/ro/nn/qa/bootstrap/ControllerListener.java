package ro.nn.qa.bootstrap;

/**
 * Event listener interface for Controller
 * Replaces TN5250j BootListener
 */
public interface ControllerListener {
    
    /**
     * Called when a controller event occurs
     * @param event The controller event
     */
    void onControllerEvent(ControllerEvent event);
}
