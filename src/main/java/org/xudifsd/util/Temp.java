package org.xudifsd.util;

public class Temp {
    private static int count = 0;

    private Temp() {
    }

    public static String next() {
        return "tmp_x_" + (Temp.count++);
    }
}
