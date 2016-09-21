package org.xudifsd.control;

public class Control {
    public static String fileName = null;
    public static boolean debug = false;
    public static String outputName = null;
    public static Verbose_t verbose = Verbose_t.Silent;

    public enum Verbose_t {
        Silent, Pass, Detailed
    }
}
