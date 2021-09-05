package ecs.util;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public abstract class Pool<T> {

    private int peak;
    private final int max;
    private int newInstanceCount = 0;
    protected final Container<T> free;

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
        newInstanceCount += n;
    }

    public boolean fit() {
        return free.fit(false);
    }

    abstract protected T newObject();

    public final T obtain() {
        T object;
        if (size() == 0) {
            object = newObject();
            newInstanceCount++;
            onObjectObtained(object,true);
        }
        else {
            object = free.pop();
            onObjectObtained(object,false);
        }
        return object;
    }

    public void free(T object) {
        if (object == null) throw new IllegalArgumentException("object cannot be null.");
        if (size() < max) {
            free.push(object);
            peak = Math.max(peak, size());
            onObjectPooled(object);
            if (object instanceof Poolable)
                ((Poolable)object).onPooled();
        } else onObjectDiscarded(object);
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
                    onObjectPooled(object);
                    if (object instanceof Poolable)
                        ((Poolable)object).onPooled();
                } else onObjectDiscarded(object);
            }
        }
        else {
            int objectCount = objects.itemCount();
            for (int i = 0; i < objectCount; i++) {
                T object = objects.get(i);
                if (object == null) continue;
                if (size() < max) {
                    freeObjects.push(object);
                    onObjectPooled(object);
                    if (object instanceof Poolable)
                        ((Poolable)object).onPooled();
                } else onObjectDiscarded(object);
            }
        }
        peak = Math.max(peak, size());
    }

    protected void onObjectPooled(T object) { }

    protected void onObjectDiscarded(T object) { }

    protected void onObjectObtained(T object, boolean newInstance) { }

    public void clear(boolean resize) {
        int n = size();
        for (int i = 0; i < n; i++)
            onObjectDiscarded(free.get(i));
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

    public int newInstancesCreated() {
        return newInstanceCount;
    }
}
