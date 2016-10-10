package org.xudifsd.util;

import java.util.Set;
import java.util.TreeSet;

public class Utils {
    public static String capitalize(String str) {
        if (str.length() <= 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String getFileName(String path) {
        String result = path;
        int pos = result.indexOf('.');
        if (pos != -1) {
            result = result.substring(0, pos);
        }
        pos = path.lastIndexOf('/');
        if (pos != -1) {
            result = result.substring(pos + 1, result.length());
        }
        return result;
    }
    public static String getDirPath(String filePath) {
        if (filePath.lastIndexOf('/') == -1) {
            return "./";
        } else {
            return filePath.substring(0, filePath.lastIndexOf('/'));
        }
    }

    public static Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> d = new TreeSet<String>(a);
        d.retainAll(b);
        return d;
    }

}
