import ecs.util.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Frederik Dahl
 * 21/08/2021
 */


public class Main {
    public static class Type<T>{
        public Type(Class<T> c) {this.c = c;}
        Class<T> c;
    }

    public static class Test extends KVSingle {
        CompA a = new CompA();
        CompB b = new CompB();
        Comp[] arr = {a,b};

        @SuppressWarnings("unchecked")
        public <T extends Comp> T get() {
            return (T)a;
        }

        public <T> T get(Class<T> c) {
            return c.cast(new Object());
        }
        public <T> T get(Type<T> type) {
            return type.c.cast(new Object());
        }

        public CompA getA() {return a;}
        public CompB getB() {return b;}
    }

    public static class Comp{
        void print() {System.out.println("Base Comp");}
    }
    public static class CompA extends Comp{
        void print() {System.out.println("A");}
    }
    public static class CompB extends Comp{
        void print() {System.out.println("A");}
    }

    public static void kvArrayTest() {
        for (int i = 0; i < Integer.MAX_VALUE / 8; i++) {
            if ((i & -i) == i) {
                System.out.println(i);
            }

        }



        KVArray<Test> kvArray = new KVArray<>(10000);
        ArrayList<Test> arrayList = new ArrayList<>(10000);
        Container<Test> container = new Container<>(10000);

        Iterator<Test> itr = t -> {
            Comp c = t.get();
        };

        for (int i = 0; i < 10000; i++) {
            Test t = new Test();
            kvArray.add(t);
            arrayList.add(t);
            container.push(t);
        }

        System.out.println("KVArray:");
        long time = System.nanoTime();
        kvArray.iterate(itr);
        System.out.println(System.nanoTime() - time);



        System.out.println("ArrayList:");
        time = System.nanoTime();
        for (Test t : arrayList) {
            Comp c = t.get();
        }
        System.out.println(System.nanoTime() - time);

        System.out.println("Container:");
        time = System.nanoTime();
        container.iterate(itr);
        System.out.println(System.nanoTime() - time);
    }

    public static void test(short d) {
        System.out.println(d);
    }

    public static void typeCastingTest() {
        long time = System.nanoTime();



        Test test = new Test();
        Type<CompA> compAType = new Type<>(CompA.class);
        Type<CompB> compBType = new Type<>(CompB.class);



        CompA a;
        for (int i = 0; i < 1000000; i++) {
            a = test.getA();
            //a.print();
        }

        System.out.println(System.nanoTime() - time);
        time = System.nanoTime();


        for (int i = 0; i < 1000000; i++) {
            a = test.get();
            //a.print();
        }

        System.out.println(System.nanoTime() - time);


        //IntStack d = new Test().get(IntStack.class);
        Random rnd = new Random();

        Container<Object> bag = new Container<>();
        IntStack stack = new IntStack();

        for (int i = 0; i < 100; i++) {
            int index = rnd.nextInt(128);
            stack.push(index);
            bag.set(new Object(),index);
        }

        bag.fit(false);

        while (!stack.isEmpty()) {
            stack.pop();
        }
    }
    
    public static void main(String[] args) {

        System.out.println(Long.bitCount(7));
    }
}
