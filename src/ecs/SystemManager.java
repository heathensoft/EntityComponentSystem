package ecs;

import ecs.util.Container;
import ecs.util.Iterator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public class SystemManager {

    private final Map<Class<? extends EntitySystem>, Long> systemBits;
    private final Map<Class<? extends EntitySystem>, EntitySystem> systemsMap;
    private final Container<EntitySystem> systems;

    private boolean initialized;
    private int bitGen;


    public SystemManager() {
        systems = new Container<>(Long.SIZE); // resize (9)
        systemsMap = new HashMap<>();
        systemBits = new HashMap<>();
        initialized = false;
        bitGen = 0;
    }

    public void register(EntitySystem system) {
        if (initialized)
            throw new IllegalStateException("manager initialized");
        if (system == null)
            throw new IllegalStateException("attempt to register null System");

        Class<? extends EntitySystem> c = system.getClass();
        if (systemsMap.get(c) == null) {
            systems.push(system);
            systemsMap.put(c,system);
            system.setSystemBit(getBit(c));
        }
    }

    public <T extends EntitySystem> T getSystem(Class<T> c) {
        return c.cast(systemsMap.get(c));
    }

    protected void initialize() {
        if (!initialized) {
            systems.fit(true);
            systems.iterate(EntitySystem::initialize);
            initialized = true;
        }
    }

    protected void terminate() {
        systems.iterate(EntitySystem::terminate);
        systems.clear();
        systemsMap.clear();
    }

    protected void iterate(Iterator<EntitySystem> itr) {
        systems.iterateUnsafe(itr);
    }

    protected long getBit(Class<? extends EntitySystem> c) {
        Long flag = systemBits.get(c);
        if(flag == null){
            flag = 1L << bitGen++;
            systemBits.put(c, flag);
        }return flag;
    }
}
