package ecs.util;

/**
 * @author Frederik Dahl
 * 29/08/2021
 */


public class IntStack {

    private int[] s = new int[0x10];
    private int t = -1;

    public void push(int i) {
        s[++t] = i;
        if (t == s.length-1) {
            int[] a = s;
            s = new int[a.length<<1];
            System.arraycopy(a,0,s,0,a.length);
        }
    }
    public int pop() {
        int r = s[t--];
        if (t<0)
            if (s.length == 0x80)
                s = new int[0x10];
        return r;
    }

    public int size() {return t+1;}

    public boolean isEmpty() {return t<0;}
}
