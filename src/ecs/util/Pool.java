package ecs.util;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public abstract class Pool<T> { // pass in memory manager (should time the usage. fit if not used for 5 min)

    private final int max;
    private int peak;

    protected final Container<T> free;

    public Pool(int initialCap, int max) {
        free = new Container<T>(initialCap);
        this.max = max;
    }

    public Pool(int initialCap) {
        this(initialCap,Integer.MAX_VALUE);
    }

    public void fill (int n) {
        n = Math.min(n+ size(),max);
        for (int i = 0; i < n; i++) {
            free.push(newObject());
        } peak = Math.max(peak, size());
    }

    public boolean fit() {
        return free.fit(false);
    }

    abstract protected T newObject();

    public final T obtain() {return size() == 0 ? newObject() : free.pop();}

    public void free(T object) {
        if (object == null) throw new IllegalArgumentException("object cannot be null.");
        if (size() < max) {
            free.push(object);
            peak = Math.max(peak, size());
            reset(object);
            if (object instanceof Poolable)
                ((Poolable)object).reset();
        } else discard(object);
    }

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
                        ((Poolable)object).reset();
                } else discard(object);
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
                        ((Poolable)object).reset();
                } else discard(object);
            }
        }
        peak = Math.max(peak, size());
    }

    protected void reset(T object) { };

    protected void discard(T object) { };

    public void clear(boolean resize) {
        int n = size();
        for (int i = 0; i < n; i++)
            discard(free.get(i));
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
}
