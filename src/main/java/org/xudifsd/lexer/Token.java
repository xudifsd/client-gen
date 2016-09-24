package org.xudifsd.lexer;

public class Token {
    public enum Kind {
        // type
        TOKEN_EOF, // EOF
        TOKEN_BOOL, // "bool"
        TOKEN_STRING, // "string"
        TOKEN_I16, // "i16"
        TOKEN_I32, // "i32"
        TOKEN_I64, // "i64"
        TOKEN_DOUBLE, // "double"
        TOKEN_BYTE, // "byte"
        TOKEN_BINARY, // "binary"
        TOKEN_LIST, // "list"
        TOKEN_SET, // "set"
        TOKEN_MAP, // "map"
        TOKEN_VOID, // "void"

        // key words
        TOKEN_STRUCT, // "struct"
        TOKEN_ENUM, // "enum"
        TOKEN_EXCEPTION, // "exception"
        TOKEN_NAMESPACE, // "namespace"
        TOKEN_THROWS, // "throws"
        TOKEN_SERVICE, // "service"

        // key words modifier
        TOKEN_REQUIRED, // "required"
        TOKEN_OPTIONAL, // "optional"

        // literal
        TOKEN_FALSE, // "false"
        TOKEN_TRUE, // "true"
        TOKEN_STRING_LITERAL, // string literal
        TOKEN_ID, // Identifier
        TOKEN_NUM, // IntegerLiteral

        TOKEN_DOT, // "."
        TOKEN_COMMA, // ","
        TOKEN_ASSIGN, // "="
        TOKEN_LBRACE, // "{"
        TOKEN_LPAREN, // "("
        TOKEN_RBRACE, // "}"
        TOKEN_RPAREN, // ")"
        TOKEN_SEMI, // ";"
        TOKEN_COLON, // ":"
        TOKEN_LT, // "<"
        TOKEN_GT, // ">"
    }

    public final Kind kind; // kind of the token
    public final String literal; // extra literal for this token
    public final Lexer lexer; // from which Lexer this token generated, used to provide debug msg

    public Token(Kind kind, String literal, Lexer lexer) {
        this.kind = kind;
        this.literal = literal;
        this.lexer = lexer;
    }

    @Override
    public String toString() {
        return "Token{" + "kind=" + kind + ", literal='" + literal + '\'' + ", lineNum=" + lexer.getLineNo()
                + ", fileName='" + lexer.getInputPath() + '\'' + '}';
    }
}
