package ecs;

/**
 * @author Frederik Dahl
 * 09/09/2021
 */


public class Memory {

    protected RunTimeStatistics runTimeStatistics;
    protected ContainerControl containerControl;
    private ComponentManager componentManager;
    private EntityManager entityManager;

    boolean containerControlEnabled;


    private void update(float dt) {

    }

    private void updateRunTimeStatistics() {

    }

    public void toggleContainerControl(boolean on) {
        containerControlEnabled = on;
    }

    public RunTimeStatistics runTimeStatistics() {
        return runTimeStatistics;
    }

    protected void attemptRefitContainer(byte id) {

    }

    protected void attemptRefitPool(byte id) {

    }
}
