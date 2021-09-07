package ecs;

import ecs.util.Container;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public class SystemManager extends ECSManager{

    private final Map<Class<? extends EntitySystem>, Long> systemBits;
    private final Map<Class<? extends EntitySystem>, EntitySystem> systemsMap;
    protected final Container<EntitySystem> systems;
    private int bitGen = 0;

    private EntityManager entityManager;

    protected SystemManager() {
        systems = new Container<>(Long.SIZE);
        systemsMap = new HashMap<>();
        systemBits = new HashMap<>();
    }

    @Override
    protected void set(ECS ecs) {
        this.entityManager = ecs.entityManager;
    }

    @Override
    protected void initialize() {
        systems.fit(true);
        systems.iterate(EntitySystem::initialize);
    }

    @Override
    protected void terminate() {
        systems.iterate(EntitySystem::terminate);
        systems.clear();
        systemsMap.clear();
    }

    protected void register(EntitySystem system) {
        if (system == null) throw new IllegalStateException("attempted to register null System");
        Class<? extends EntitySystem> c = system.getClass();
        if (systemsMap.get(c) == null) {
            system.setSystemBit(getBit(c));
            system.set(entityManager);
            systems.push(system);
            systemsMap.put(c,system);
        }
    }

    public <T extends EntitySystem> T getSystem(Class<T> c) {
        return c.cast(systemsMap.get(c));
    }


    protected long getBit(Class<? extends EntitySystem> c) {
        Long flag = systemBits.get(c);
        if(flag == null){
            flag = 1L << bitGen++;
            systemBits.put(c, flag);
        }return flag;
    }
}
