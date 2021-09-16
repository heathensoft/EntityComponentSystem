package ecs.util.containers;


import ecs.util.ShortStack;
import ecs.util.containers.exceptions.EmptyCollectionException;
import ecs.util.containers.exceptions.ItemNotFoundException;
import ecs.util.containers.exceptions.KeyStateConflictException;

/**
 *
 * Tightly stacked auto-resizable Key/Value array.
 *
 *
 * Upside:
 * O(1) lookup / removal.               [1]
 * Iterates 2-3x faster than ArrayList. [2]
 *
 * [1] Technically O(A), where A = approx. the number of KVArrays an item inhabits on average.
 *     That would in practice mean that at some point as A increase, it would degrade to be slower than a HashMap.
 *
 * [2] for(int i = 0; i < count; i++) iterator.next(items[i]); That's it.
 *     I tested enough to personally favor it over any Collection when the downsides are acceptable:
 *
 * Downside:
 * Not java Collections.
 * Items must implement KeyValue.
 * Must use an ecs.util.containers.Iterator to iterate.
 * Overhead in items inhabiting multiple KVArrays.
 *
 *
 * Iteration:
 * Does not implement Iterable as using the ecs Iterator is favorable.
 * Try not to create iterator instances anonymously in a loop.
 * Anonymous is perfect for single iterations .ie initializations.
 * But keep them around if you are using them repeatedly.
 *
 *
 * @author Frederik Dahl
 * 29/08/2021
 */

@SuppressWarnings("unchecked")

public class KVArray<E extends KeyValue> {

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
     * @param capacity the estimated use-case capacity of the Array. (auto-resizable).
     *                 It also resets to this capacity on clearAndResize(), or when count reaches 0, on removal of Items;
     */
    public KVArray(int capacity) {
        targetCap = Math.max(1,capacity);
        items = (E[]) new KeyValue[targetCap];
        if (freeIDs.isEmpty()) {
            if (genID == ID_LIMIT)
                throw new IllegalStateException("Out of available id's. Use the free() method to recycle id's.");
            id = genID++;
        } else id = freeIDs.pop();
    }

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
        if (count == 0) {
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

    /**
     * As to avoid potentially multiple calls to arrayCopy() before adding n new items.
     * @param n number of additional items planned to be added
     */
    public void preAllocate(int n) {
        int size = n + count;
        if (size > items.length)
            resize(size);
    }

    public void fit() {
        if (count > 0)
            resize(count);
    }

    public void setTargetCapacity(int cap) {
        this.targetCap = Math.max(Math.max(count,1),cap);
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean notEmpty() {
        return count > 0;
    }

    private int size() {
        return count;
    }

    public int capacity() {
        return items.length;
    }

    private void grow() {
        resize((items.length*3)/2+1);
    }

    private void shrink() {
        resize(Math.max(targetCap,count));
    }

    private void resize(int size) {
        E[] o = items;
        E[] n = (E[])new KeyValue[size];
        System.arraycopy(o,0,n,0,count);
        items = n;
    }
}
