package com.nudge.ecs.util.containers.test;

import com.nudge.ecs.util.containers.Iterator;

/**
 *
 * Generic auto-growing container.
 * Backed up by: Object[]
 * Can be used as a stack or an indexed container.
 *
 * !! target capacity is important for all ECSArrays. They are auto-resizable
 * and the targetCap value is the "restingCap" for the arrays.
 * In most cases, when you remove the final item and the array length is greater
 * than the targetCap it will shrink back to that "restingCap".
 * The fit() method too, will consider the targetCap. Unless you fit absolute.
 * You can set the targetCap after creating a ECSArray.
 *
 * Use-cases (in short):
 *
 * Either one:
 *
 * 1. As a stack where you don't care about the specific object added or returned.
 * 2. As an array where "holes" are allowed, indexes matter and objects are externally tracked.
 *
 * About:
 *
 * In the ECS it is used both a stack and indexed container.
 * Components in the ComponentManager is stored in a Container where they are indexed by entity ID.
 * We use the set() method to place components. There should never be conflict or overwrites because
 * the entityID is unique.
 * This setup will allow for holes, but since Entities are recycled that's ok.
 * The empty space is a fair tradeoff for the performance of getting the components.
 * Another setup could be having entities be containers for their components.
 *
 * If you attempt to set items out of bound, it will grow to the new capacity of: ( (index of item added + 1) * 3 ) / 2 + 1;
 * You can fill holes with stack() and you can shrink the array to fit its items with fit()
 * fit() will shrink it down to the outermost object (the "peakIndex" or its tail), it will not stack the array.
 * Read fit. It has some other caveats.
 *
 * @author Frederik Dahl
 * 24/09/2021
 */

@SuppressWarnings("unchecked")
public class Container<E> implements ECSArray<E>{

    private int targetCap;
    private int peakIndex;
    private int count;

    private Object[] items;

    public Container(int targetCap) {
        this.targetCap = Math.max(targetCap,1);
        this.items = new Object[this.targetCap];
        this.peakIndex = -1;
        this.count = 0;
    }

    public Container() {
        this(DEFAULT_CAPACITY);
    }


    @Override
    public void iterate(Iterator<E> itr) {
        if (count > 0) {
            if (isStacked())
                for (int i = 0; i < count; i++)
                    itr.next(get(i));
            else {
                int count = this.count;
                for (int i = 0; i <= peakIndex; i++) {
                    if (items[i] != null) {
                        itr.next(get(i));
                        if (--count == 0) return;
                    }
                }
            }
        }
    }

    public E get(int index) {
        return (E)items[index];
    }

    public void push(E item) {
        if (item != null) {
            if (usedSpace() == capacity())
                resize(growFormula(capacity()));
            items[++peakIndex] = item;
            count++;
        }
    }

    /**
     * pops the item at the peak-index (outermost item)
     * @return the item or null if empty
     */
    public E pop() {
        if (isEmpty()) return null;
        Object item = items[peakIndex];
        items[peakIndex--] = null;
        if (peakIndex > -1) {
            while (items[peakIndex] == null)
                if (--peakIndex == -1) break;
        }
        count--;
        return (E)item;
    }

    public void set(E item, int index) {
        if (index >= items.length) {
            if (item != null) {
                resize(growFormula(index));
                peakIndex = index;
                items[index] = item;
                count++;
            }
        } else {
            Object prev = items[index];
            if (prev == null) {
                if (item != null) {
                    peakIndex = Math.max(index,peakIndex);
                    items[index] = item;
                    count++;
                }
            } else {
                if (item == null)
                    count--;
                items[index] = item;
            }
        }
    }

    public E remove(int index) {
        Object item = items[index];
        if (item != null) {
            items[index] = null;
            count--;
            if (index == peakIndex) {
                while (items[peakIndex] == null)
                    if (--peakIndex == -1) break;
            }
        }
        return (E)item;
    }

    @Override
    public void clear() {
        final int usedSpace = usedSpace();
        for (int i = 0; i < usedSpace; i++)
            items[i] = null;
        peakIndex = -1;
        count = 0;
    }

    private void resize(int capacity) {
        Object[] tmp = items;
        items = new Object[capacity];
        System.arraycopy(tmp,0,items,0,usedSpace());
    }

    @Override
    public void ensureCapacity(int n) {
        int cap = n + usedSpace();
        if (cap > capacity()) {
            resize(cap);
        }
    }

    @Override
    public boolean fit(boolean absolute) {
        int used = usedSpace();
        if (used == capacity()) return false;
        int size = absolute ? Math.max(used,1) : Math.max(used,targetCap);
        if (size == capacity()) return false;
        resize(size);
        return true;
    }

    public void stack() {
        if (isEmpty())
            return;
        if (!isStacked()) {
            int l = 0;
            int r = peakIndex;
            while (l < r) {
                while (items[l] != null) l++;
                while (items[r] == null) r--;
                items[l++] = items[r];
                items[r--] = null;
            }peakIndex = count - 1;
        }
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public float loadFactor() {
        return (float) count / items.length;
    }

    @Override
    public int capacity() {
        return items.length;
    }

    @Override
    public int targetCapacity() {
        return targetCap;
    }

    @Override
    public void setTargetCapacity(int cap) {
        targetCap = Math.max(cap,1);
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @Override
    public boolean notEmpty() {
        return count > 0;
    }

    public boolean isStacked() {
        return usedSpace() == count;
    }

    public int usedSpace() {
        return peakIndex + 1;
    }
}
