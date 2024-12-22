package org.example;

public class TestProgram4 {
    public static void main(String[] args) {
        int x = 0;
        int y = 1;
        int z = 2;
        if (x > 0) {
            y = x + 1;
        } else {
            z = y - 1;
        }
        x = z + y;
    }
}
