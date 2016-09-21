package org.xudifsd.lexer;

public class SyntaxException extends Exception {
    private final Lexer.Msg msg;

    public SyntaxException(Lexer.Msg msg) {
        super();
        this.msg = msg;
    }

    public Lexer.Msg getMsg() {
        return msg;
    }
}
