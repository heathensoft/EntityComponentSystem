package com.nudge.ecs;


import com.nudge.ecs.util.containers.Container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * ComponentType and ComponentGroup instances are created and stored here.
 *
 *       Maximum number of unique ComponentTypes: 64 (Might be reduced to int32)
 *       Maximum number of unique ComponentGroups: 32767
 *       You cannot delete types or groups from a manager instance once created.
 *       When you initiate a type, 1 "slot" gets taken.
 *       When you initiate a group, it automatically creates types of members.
 *       You can rename types and groups. .ie someGroup.setName()
 *
 * @author Frederik Dahl
 * 13/09/2021
 */


public class TypeManager {

    private final ComponentManager manager;

    private final Map<Class<? extends Component>, ComponentType> typeMap;
    private final List<ComponentType> typeList;
    private final Container<ComponentType> typesById;
    private final Container<ComponentGroup> groups;
    private long nextFlag       = 1L;
    private short genGroupID    = 0;
    private byte genTypeID      = 0;

    protected TypeManager(ComponentManager componentManager) {
        this.manager = componentManager;
        typeMap = new HashMap<>();
        typeList = new ArrayList<>();
        groups = new Container<>();
        typesById = new Container<>(9);
    }

    protected ComponentType getType(Class<? extends Component> c) {
        ComponentType type = typeMap.get(c);
        if (type == null) {
            if (manager.ecs.isInitialized())
                throw new IllegalStateException("Creating new types after ECS init not allowed");
            if (typeList.size() == Long.SIZE)
                throw new IllegalStateException("limit break: max types");
            type = new ComponentType(c,nextFlag,genTypeID);
            nextFlag = nextFlag << 1;
            genTypeID++;
            typeMap.put(c,type);
            typeList.add(type);
            typesById.push(type);
            manager.newContainer();
            manager.control.newTimer();
        }return type;
    }

    protected ComponentGroup getGroup(ComponentType... types) {
        ComponentGroup group;
        long mask = 0;
        for (ComponentType type : types)
            mask |= type.flag();
        group = lookUpGroup(mask);
        if (group == null) {
            if (manager.ecs.isInitialized())
                throw new IllegalStateException("Creating groups after ECS init not allowed");
            group = new ComponentGroup(mask,genGroupID);
            groups.set(group,group.id());
            genGroupID++;
        }return group;
    }

    @SafeVarargs
    protected final ComponentGroup getGroup(Class<? extends Component>... classes) {
        ComponentType[] types = new ComponentType[classes.length];
        for (int i = 0; i < classes.length; i++)
            types[i] = getType(classes[i]);
        return getGroup(types);
    }

    protected List<ComponentType> getList() {
        return typeList;
    }

    private ComponentGroup lookUpGroup(long mask) {
        ComponentGroup group;
        for (int i = 0; i < groups.itemCount(); i++) {
            group = groups.get(i);
            if (group.match(mask)) {
                return group;
            }
        }
        return null;
    }

}
