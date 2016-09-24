package org.xudifsd.lexer;

import org.xudifsd.parser.Msg;

public class SyntaxException extends Exception {
    private final Msg msg;

    public SyntaxException(Msg msg) {
        super();
        this.msg = msg;
    }

    public Msg getMsg() {
        return msg;
    }
}
