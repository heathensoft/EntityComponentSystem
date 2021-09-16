package ecs.util.containers;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public abstract class Pool<T> {

    private int peak;
    private final int max;
    protected final Container<T> free;

    protected int newInstances = 0;
    protected int discarded = 0;
    protected long obtained = 0;



    public Pool(int initialCapacity, int max) {
        free = new Container<>(initialCapacity);
        this.max = max;
    }

    public Pool(int initialCapacity) {
        this(initialCapacity,Integer.MAX_VALUE);
    }

    public void fill (int n) {
        n = Math.min(n+ size(),max);
        for (int i = 0; i < n; i++)
            free.push(newObject());
        peak = Math.max(peak, size());
        newInstances += n;
    }

    public boolean fit() {
        return free.fit(false);
    }

    abstract protected T newObject();

    public final T obtain() {
        T object;
        if (size() == 0) {
            object = newObject();
            newInstances++;
            obtained(object,true);
        } else {
            object = free.pop();
            obtained(object,false);
        }
        obtained++;
        return object;
    }

    public void free(T object) {
        if (object == null) throw new IllegalArgumentException("object cannot be null.");
        if (size() < max) {
            free.push(object);
            peak = Math.max(peak, size());
            reset(object);
            if (object instanceof Poolable)
                ((Poolable)object).onPooled();
        } else {
            discarded++;
            discard(object);
        }
    }

    // todo: Add equivalent for Collections
    /**
     *
     * @param objects to be freed
     * @param consume Whether to nullify and clear the argument objects container
     */
    public void freeAll (Container<T> objects, boolean consume) {
        if (objects == null) throw new IllegalArgumentException("objects cannot be null.");
        Container<T> freeObjects = this.free;
        int max = this.max;
        if (consume) {
            while (!objects.isEmpty()) {
                T object = objects.pop();
                if (object == null) continue;
                if (size() < max) {
                    freeObjects.push(object);
                    reset(object);
                    if (object instanceof Poolable)
                        ((Poolable)object).onPooled();
                } else {
                    discarded++;
                    discard(object);
                }
            }
        }
        else {
            int objectCount = objects.itemCount();
            for (int i = 0; i < objectCount; i++) {
                T object = objects.get(i);
                if (object == null) continue;
                if (size() < max) {
                    freeObjects.push(object);
                    reset(object);
                    if (object instanceof Poolable)
                        ((Poolable)object).onPooled();
                } else {
                    discarded++;
                    discard(object);
                }
            }
        }
        peak = Math.max(peak, size());
    }

    /**
     * Called when an object gets pooled
     *
     * @param object the object to be reset
     */
    protected void reset(T object) { }

    /**
     * Called when an object gets discarded
     *
     * @param object the object to be discarded
     */
    protected void discard(T object) { }

    /**
     * Called when an object gets obtained
     *
     * @param object the object to be obtained
     * @param newInstance whether the object is a new instance
     */
    protected void obtained(T object, boolean newInstance) { }

    public void clear(boolean resize) {
        int n = size();
        for (int i = 0; i < n; i++)
            discard(free.get(i));
        discarded += n;
        free.clear();
        if (resize) free.fit(false);
    }

    public int size() {
        return free.itemCount();
    }

    public int max() {
        return max;
    }

    public int peak() {
        return peak;
    }

    public int newCreated() {
        return newInstances;
    }

    public int discarded() {
        return discarded;
    }

    public long obtained() {
        return obtained;
    }

    public int capacity() {
        return free.capacity();
    }

    public float loadFactor() {
        return free.loadFactor();
    }

    public int objectsInMemory() {
        return newInstances - discarded;
    }
}
