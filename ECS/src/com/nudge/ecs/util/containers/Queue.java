package com.nudge.ecs.util.containers;

import com.nudge.ecs.util.exceptions.EmptyCollectionException;

/**
 *
 * Could use a linked list, but I prefer arrays. Circular.
 *
 * !! target capacity is important for all ECSArrays. They are auto-resizable
 * and the targetCap value is the "restingCap" for the arrays.
 * In most cases, when you remove the final item and the array length is greater
 * than the targetCap it will shrink back to that "restingCap".
 * The fit() method too, will consider the targetCap. Unless you fit absolute.
 * You can set the targetCap after creating a ECSArray.
 *
 * @author Frederik Dahl
 * 23/09/2021
 */


public class Queue<E> implements ECSArray<E> {

    private Object[] queue;
    private int targetCap;
    private int count = 0;
    private int front = 0;
    private int rear = 0;

    public Queue(int targetCap) {
        this.targetCap = Math.max(targetCap,1);
        this.queue = new Object[this.targetCap];
    }

    public Queue() {
        this(DEFAULT_CAPACITY);
    }


    @Override @SuppressWarnings("unchecked")
    public void iterate(Iterator<E> itr) {
        for (int i = 0; i < count; i++) {
            itr.next((E) queue[(front+i) % queue.length]);
        }
    }

    public void enqueue(E item) {
        if (item != null) {
            if (count == queue.length)
                resize(growFormula(capacity()));
            queue[rear] = item;
            rear = (rear+1) % queue.length;
            count++;
        }
    }

    /**
     * Will shrink the underlying array to match the target capacity
     * if the queue is empty and its capacity > target capacity.
     *
     * @return the item
     */
    @SuppressWarnings("unchecked")
    public E dequeue() {
        if (count == 0)
            throw new EmptyCollectionException("...");
        E result = (E) queue[front];
        queue[front] = null;
        if (--count == 0) {
            if (queue.length >= targetCap)
                queue = new Object[targetCap];
            front = rear = count;}
        else front = (front+1) % queue.length;
        return result;
    }

    /**
     * This will nullify all elements, will not shrink the array (use fit)
     */
    @Override
    public void clear() {
        if (notEmpty()) {
            for (int i = 0; i < count; i++)
                queue[(front+i) % queue.length] = null;
            front = rear = count = 0;
        }
    }

    /**
     * Ensure that the queue has capacity before enqueuing many items
     * to avoid multiple calls to resize.
     *
     * @param n the number of new items you intend to enqueue
     */
    @Override
    public void ensureCapacity(int n) {
        int cap = n + count;
        if (cap > capacity()) {
            resize(cap);
        }
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
        return (float) count / queue.length;
    }

    @Override
    public int capacity() {
        return queue.length;
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

    private void resize(int size) {
        Object[] tmp = queue;
        queue = new Object[size];
        for (int i = 0; i < count; i++)
            queue[i] = tmp[(front+i)%tmp.length];
        rear = count;
        front = 0;
    }


}
