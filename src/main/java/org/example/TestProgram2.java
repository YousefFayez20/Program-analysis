package org.example;

public class TestProgram2 {
    public static void main(String[] args) {
        // Initialize some objects
        MyObject obj1 = new MyObject();
        MyObject obj2 = obj1; // Alias created
        MyObject obj3 = new MyObject();

        // Modify fields through aliases
        obj1.value = 10;
        obj2.value = 20;

        // Conditional aliasing
        if (args.length > 0) {
            obj3 = obj1; // obj3 aliases obj1
        }

        System.out.println(obj1.value); // Should reflect changes via aliasing
        System.out.println(obj3.value);
    }
}

class MyObject {
    int value;
}
