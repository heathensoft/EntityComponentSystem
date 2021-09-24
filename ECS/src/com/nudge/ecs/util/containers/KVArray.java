package com.nudge.ecs.util.containers;

import com.nudge.ecs.util.ShortStack;
import com.nudge.ecs.util.containers.ECSArray;
import com.nudge.ecs.util.containers.Iterator;
import com.nudge.ecs.util.containers.KeyValue;
import com.nudge.ecs.util.exceptions.EmptyCollectionException;
import com.nudge.ecs.util.exceptions.ItemNotFoundException;
import com.nudge.ecs.util.exceptions.KeyStateConflictException;

/**
 *
 * An always tightly stacked, auto-resizable Key/Value array.
 * Used for both fast removal and iteration of elements.
 * Replacing the hashmap at the cost of some overhead.
 *
 * Very fast iteration with the Iterator.
 *
 * O[1] removal. For KVShared that is O[A] where A is number of KeyValueArrays
 * that shares the KeyValue item (or rather the approx. average for all items in the array)
 * i.e. if our KeyValue items inhabits 2 KeyValueArrays on average, then A would be 2.
 *
 * !! target capacity is important for all ECSArrays. They are auto-resizable
 * and the targetCap value is the "restingCap" for the arrays.
 * In most cases, when you remove the final item and the array length is greater
 * than the targetCap it will shrink back to that "restingCap".
 * The fit() method too, will consider the targetCap. Unless you fit absolute.
 * You can set the targetCap after creating a ECSArray.
 *
 * Iteration:
 * Does not implement Iterable as using the nudge.ecs Iterator is favorable.
 * Try not to create iterator instances anonymously in a loop.
 * Anonymous is perfect for single iterations .ie initializations.
 * But keep them around if you are using them repeatedly.
 *
 * @author Frederik Dahl
 * 23/09/2021
 */

@SuppressWarnings("unchecked")

public class KVArray<E extends KeyValue> implements ECSArray<E> {


    private static final ShortStack freeIDs = new ShortStack();
    private static final short ID_LIMIT = Short.MAX_VALUE;
    private static short genID = 0;

    private final short id;
    private int count = 0;
    private int targetCap;

    private E[] items;

    /**
     * ! The Constructor CAN fail: if you for some bizarre esoteric reason have created 32767 Arrays and are not freeing them.
     *
     * @param initialCap the estimated use-case capacity of the Array. (auto-resizable).
     *                   It also resets to this capacity on clearAndResize(), or when count reaches 0, on removal of Items;
     */
    public KVArray(int initialCap) {
        targetCap = Math.max(1,initialCap);
        items = (E[]) new KeyValue[targetCap];
        if (freeIDs.isEmpty()) {
            if (genID == ID_LIMIT)
                throw new IllegalStateException("Out of available id's. Use the free() method to recycle id's.");
            id = genID++;
        } else id = freeIDs.pop();
    }

    public KVArray() {this(DEFAULT_CAPACITY);}

    @Override
    public void iterate(Iterator<E> itr) {
        for (int i = 0; i < count; i++)
            itr.next(items[i]);
    }

    public void add(E item) {
        int key = item.getKey(id);
        if (key != KeyValue.NONE) {
            if (key < 0)
                throw new KeyStateConflictException("Provided illegal key: key < -1");
            else if (key >= count)
                throw new KeyStateConflictException("Key provided points out of bound");
            else {
                if (item.equals(items[key])) return;
                else throw new KeyStateConflictException("Key matching another item");
            }
        }
        if (count == items.length) grow();
        item.onInsert(count,id);
        items[count++] = item;
    }

    public void remove(E item) {
        if (count == 0) throw new EmptyCollectionException("Empty array");
        int key = item.getKey(id);
        if (key < 0 || key >= count) {
            if (key == KeyValue.NONE)
                throw new ItemNotFoundException("Item could not find key corresponding to Array attempting to remove it");
            else if (key < KeyValue.NONE)
                throw new KeyStateConflictException("Item provided illegal key: key < -1");
            else throw new ItemNotFoundException("Item key provided to Array points out of bound");
        }
        E found = items[key];
        if (!item.equals(found))
            throw new ItemNotFoundException("Items do not match");
        found.onRemoval(id);
        int last = --count;
        if (key == last)
            items[last] = null;
        else {
            E lastItem = items[last];
            items[last] = null;
            lastItem.onReplacement(key,id);
            items[key] = lastItem;
        }
        if (isEmpty()) {
            if (targetCap < items.length)
                items = (E[]) new KeyValue[targetCap];
        }
    }

    /**
     * If empty and
     * @param index the index
     * @return the item
     */
    public E remove(int index) {
        E item = items[index];
        item.onRemoval(id);
        int last = count--;
        if (index == last)
            items[last] = null;
        else {
            E prev = items[last];
            items[last] = null;
            prev.onReplacement(index,id);
            items[index] = prev;
        }
        if (isEmpty()) {
            if (targetCap < items.length)
                items = (E[]) new KeyValue[targetCap];
        }
        return item;
    }

    public E get(int index) {
        return items[index];
    }

    public int getIndex(E item) {
        if (count == 0) return KeyValue.NONE;
        int key = item.getKey(id);
        if (key < 0 || key >= count)
            return KeyValue.NONE;
        E other = items[key];
        if (!item.equals(other))
            return KeyValue.NONE;
        return key;
    }

    /**
     * Control check for debugging purposes.
     * @param item the contained item to be checked.
     * @throws RuntimeException various descriptive exceptions thrown if check fails.
     */
    public void controlCheck(E item) throws RuntimeException{
        if (count == 0) throw new EmptyCollectionException("Empty array");
        int key = item.getKey(id);
        if (key < 0 || key >= count) {
            if (key == KeyValue.NONE)
                throw new ItemNotFoundException("Item could not find key corresponding to KVArray");
            else if (key < KeyValue.NONE)
                throw new KeyStateConflictException("Item provided illegal key: key < -1");
            else throw new ItemNotFoundException("Item key provided to Array points out of bound");
        }
        if (!item.equals(items[key]))
            throw new ItemNotFoundException("Items do not match");
    }

    public boolean contains(E item) {
        if (item == null) return false;
        int key = item.getKey(id);
        if (key < 0 || key >= count)
            return false;
        return item.equals(items[key]);
    }

    @Override
    public void clear() {
        for (int i = 0; i < count; i++) {
            items[i].onRemoval(id);
            items[i] = null;
        }count = 0;
    }

    /**
     * Clears the array (removes all items, nullifies all item references). Nullifies the array.
     * Free's the id. Renders this KVArray useless. This is more or less a utility function that's
     * reasonable to use .ie on ending a "scene" to signal the GC to collect. And if the application
     * begins another "scene" the static id-generator (pool size: 32767) can recycle this id.
     */
    public void free() {
        clear();
        freeIDs.push(id);
        items = null;
    }

    @Override
    public void ensureCapacity(int n) {
        int size = n + count;
        if (size > items.length)
            resize(size);
    }

    @Override
    public boolean fit(boolean absolute) {
        if (count == capacity()) return false;
        int size = absolute ? Math.max(count,1) : Math.max(count, targetCap);
        if (size == capacity()) return false;
        resize(size);
        return true;
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

    private void grow() {
        resize(growFormula(capacity()));
    }

    private void resize(int size) {
        E[] o = items;
        E[] n = (E[])new KeyValue[size];
        System.arraycopy(o,0,n,0,count);
        items = n;
    }
}
