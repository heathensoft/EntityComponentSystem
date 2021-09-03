package ecs.util;

/**
 *
 * Generic auto-growing container.
 * Backed up by: Object[]
 * Can be used as a stack or an indexed container.
 * Not thread-safe
 *
 * Use-cases (in short):
 *
 * Either one:
 *
 * 1. As a tightly stacked "pool" where you don't care about the specific object added or returned.
 * 2. As an array where "holes" are allowed, indexes matter and objects are externally tracked.
 *
 * About:
 *
 * If you attempt to add items out of bound, it will grow to the new capacity of: ( (index of item added + 1) * 3 ) / 2 + 1;
 * You can fill holes with stack() and you can shrink the array to fit its items with fit()
 * fit() will shrink it down to the outermost object (the "peakIndex" or its tail), it will not stack the array.
 *
 *
 *
 * @author Frederik Dahl
 * 28/08/2021
 */

@SuppressWarnings("unchecked")

public class Container<E> {

    private int targetCap;
    private int itemCount;
    private int peakIndex;

    private Object[] items;


    public Container(int initialCap) {
        targetCap = initialCap;
        peakIndex = -1;
        itemCount = 0;
        items = new Object[initialCap];
    }

    public Container() {
        this(10);
    }

    public void iterate(Iterator<E> itr) {
        if (itemCount > 0) {
            if (isStacked())
                for (int i = 0; i < itemCount; i++)
                    itr.next(get(i));
            else {
                int count = itemCount;
                for (int i = 0; i <= peakIndex; i++) {
                    if (items[i] != null) {
                        itr.next(get(i));
                        if (--count == 0) return;
                    }
                }
            }
        }
    }

    /**
     * You could use this if you know the Container is stacked.
     * You don't gain much, if anything at all in terms of performance:
     * @param itr the Iterator
     */
    public void iterateUnsafe(Iterator<E> itr) {
        for (int i = 0; i < itemCount; i++)
            itr.next(get(i));
    }

    public void push(E item) {
        if (item != null) {
            if (usedSpace() == capacity())
                grow();
            items[++peakIndex] = item;
            itemCount++;
        }
    }

    public E pop() {
        if (isEmpty()) return null;
        Object item = items[peakIndex];
        items[peakIndex--] = null;
        if (peakIndex > -1) {
            while (items[peakIndex] == null)
                if (--peakIndex == -1) break;
        }
        itemCount--;
        return (E)item;
    }

    public void set(E item, int index) {
        if (index >= items.length) {
            if (item != null) {
                int cap = ((index+1)*3)/2+1;
                resize(cap);
                peakIndex = index;
                items[index] = item;
                itemCount++;
            }
        } else {
            Object prev = items[index];
            if (prev == null) {
                if (item != null) {
                    peakIndex = Math.max(index,peakIndex);
                    items[index] = item;
                    itemCount++;
                }
            } else {
                if (item == null)
                    itemCount--;
                items[index] = item;
            }
        }
    }

    public E remove(int index) {
        Object item = items[index];
        if (item != null) {
            items[index] = null;
            itemCount--;
        }return (E)item;
    }

    public E get(int index) {
        return (E)items[index];
    }

    public void clear() {
        int usedSpace = usedSpace();
        for (int i = 0; i < usedSpace; i++)
            items[i] = null;
        peakIndex = -1;
        itemCount = 0;
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
            }
            peakIndex = itemCount - 1;
        }
    }

    /**
     * fits to: absolute ? usedSpace() : Math.max(usedSpace(), targetCap)
     * @return if container was resized
     */
    public boolean fit(boolean absolute) {
        if (capacity() > usedSpace()) {
            int size = absolute ? usedSpace() : Math.max(usedSpace(), targetCap);
            resize(size);
            return true;
        } return false;
    }

    public void setTargetCap(int capacity) {
        targetCap = capacity;
    }

    public int capacity() {
        return items.length;
    }

    public int itemCount() {
        return itemCount;
    }

    public int usedSpace() {
        return peakIndex + 1;
    }

    public boolean isEmpty() {
        return itemCount == 0;
    }

    public boolean notEmpty() {
        return itemCount > 0;
    }

    public boolean isStacked() {
        return usedSpace() == itemCount;
    }

    public float loadFactor() {
        return (float) itemCount / items.length;
    }

    private void resize(int capacity) {
        if (isEmpty())
            items = new Object[capacity];
        else {
            Object[] oldArr = items;
            items = new Object[capacity];
            System.arraycopy(oldArr,0,items,0,usedSpace());
        }
    }

    private void grow() {
        resize(((capacity()+1)*3)/2+1);
    }

}

