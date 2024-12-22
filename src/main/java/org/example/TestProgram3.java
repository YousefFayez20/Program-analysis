package org.example;

public class TestProgram3 {
    static MyObject2 staticObj;

    public static void main(String[] args) {
        MyObject2 localObj1 = new MyObject2();
        staticObj = localObj1; // Global aliasing

        MyObject2 localObj2 = new MyObject2();
        if (args.length > 0) {
            localObj2 = localObj1; // Conditional aliasing
        }

        staticObj.value = 42;
        System.out.println(localObj2.value); // Output depends on aliasing
    }
}

class MyObject2 {
    int value;
}
