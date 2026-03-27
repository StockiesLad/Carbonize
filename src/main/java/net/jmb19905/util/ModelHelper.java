package net.jmb19905.util;

public class ModelHelper {
    public static int shift(int value, int shift, int size) {
        if (shift > 0) return (value + shift) % size;
        return (int) ((value + shift + size * (Math.ceil((double) Math.abs(value + shift) / size))) % size);
    }
}
