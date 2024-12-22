package org.example;

public class TestProgramWithNull {
    public static void main(String[] args) {
        MyObject3 obj = null; // Null assignment
        System.out.println(obj.value); // Dereference null (should trigger warning)
    }
}

class MyObject3 {
    int value = 10;
}
