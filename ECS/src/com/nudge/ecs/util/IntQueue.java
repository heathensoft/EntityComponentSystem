package com.nudge.ecs.util;

import com.nudge.ecs.util.exceptions.EmptyCollectionException;

/**
 *
 * Auto-resizeable circular queue.
 * initial cap is 16. Shrinks if empty and cap has reached 128 previously
 *
 * @author Frederik Dahl
 * 23/09/2021
 */


public class IntQueue {

    private int[] q = new int[0x10];
    private int f = 0;
    private int r = 0;
    private int c = 0;

    public void enqueue(int i) {
        if (c == q.length) grow();
        q[r] = i;
        r = (r+1) % q.length;
        c++;
    }

    public int dequeue() {
        if (c == 0)
            throw new EmptyCollectionException("...");
        int i = q[f];
        if (--c == 0) {
            if (q.length >= 0x80)
                q = new int[0x10];
            f = r = c;}
        else f = (f+1) % q.length;
        return i;
    }

    private void grow() {
        int[] tmp = q;
        q = new int[c<<1];
        for (int i = 0; i < c; i++)
            q[i] = tmp[(f+i) % c];
        r = c;
        f = 0;
    }

    public int size() {return c;}

    public boolean isEmpty() {return c == 0;}

}
