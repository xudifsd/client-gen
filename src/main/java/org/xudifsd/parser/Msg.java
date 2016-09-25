package org.xudifsd.parser;

// help to build beautiful err msg for included file
public class Msg {
    public final String msg;
    public final Msg sub;

    public Msg(String msg) {
        this.msg = msg;
        this.sub = null;
    }

    public Msg(String msg, Msg sub) {
        this.msg = msg;
        this.sub = sub;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Msg cur = this;
        int level = 0;
        while (cur != null) {
            if (level != 0) {
                sb.append('\n');
            }
            for (int i = 0; i < level; ++i) {
                sb.append(' ');
            }
            sb.append(cur.msg);
            cur = cur.sub;
            level++;
        }
        return sb.toString();
    }
}
