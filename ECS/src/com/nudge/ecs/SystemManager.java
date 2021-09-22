package com.nudge.ecs;


import com.nudge.ecs.util.containers.Container;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public class SystemManager {

    private final Map<Class<? extends ECSystem>, Long> systemBits;
    private final Map<Class<? extends ECSystem>, ECSystem> systemsMap;
    protected final Container<ECSystem> systems;
    private int bitGen = 0;

    private final ECS ecs;

    protected SystemManager(ECS ecs) {
        this.ecs = ecs;
        systems = new Container<>(Long.SIZE);
        systemsMap = new HashMap<>();
        systemBits = new HashMap<>();
    }


    protected void initializeSystems() {
        systems.fit(true);
        systems.iterate(ECSystem::initialize);
    }

    protected void deactivateSystems() {
        systems.iterate(ECSystem::deactivate);
    }

    protected void terminate() {
        systems.iterate(ECSystem::terminate);
        systems.clear();
        systemsMap.clear();
    }

    protected void register(ECSystem system) {
        Class<? extends ECSystem> c = system.getClass();
        if (systemsMap.get(c) == null) {
            system.set(getBit(c));
            systems.push(system);
            systemsMap.put(c,system);
        }
    }

    protected <T extends ECSystem> T getSystem(Class<T> c) {
        return c.cast(systemsMap.get(c));
    }


    private long getBit(Class<? extends ECSystem> c) {
        Long flag = systemBits.get(c);
        if(flag == null){
            flag = 1L << bitGen++;
            systemBits.put(c, flag);
        }return flag;
    }
}
