package org.xudifsd.util;

public class Arglist<X> {
    public java.util.LinkedList<X> addAll(
            @SuppressWarnings("unchecked") X... args) {
        java.util.LinkedList<X> list = new java.util.LinkedList<X>();
        for (X arg : args)
            list.addLast(arg);
        return list;
    }
}
