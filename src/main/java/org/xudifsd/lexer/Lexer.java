package org.xudifsd.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xudifsd.control.Control;
import org.xudifsd.lexer.Token.Kind;
import org.xudifsd.util.Utils;

public class Lexer {
    private String inputPath;
    private Reader reader; // input stream for the above file
    private Lexer includeLexer = null;
    private int lineNo;

    public String getInputPath() {
        return inputPath;
    }

    public int getLineNo() {
        return lineNo;
    }

    public Lexer(String inputPath, Reader reader) throws IOException {
        this.inputPath = inputPath;
        this.reader = new BufferedReader(reader);
        this.lineNo = 1;
    }

    // return true on skipped
    private boolean skipWhitespaces() throws IOException {
        boolean skipped = false;
        reader.mark(1);
        int c = reader.read();
        while (' ' == c || '\t' == c || '\n' == c || '\r' == c) {
            if ('\n' == c) {
                this.lineNo++;
            }
            skipped = true;
            reader.mark(1);
            c = this.reader.read();
        }
        reader.reset();
        return skipped;
    }

    // return true on skipped
    private boolean skipComments() throws IOException, SyntaxException {
        reader.mark(2);
        int c = reader.read();
        if (c != '/') {
            reader.reset();
            return false;
        }
        int next = reader.read();

        if (next == '/') {
            int c1 = 0;
            while (c1 != -1 && c1 != '\n') {
                c1 = this.reader.read();
            }
            lineNo++;
            return true;
        } else if (next == '*') {
            int noOfLine = 0; // do not update lineNo directly, use it to provide useful start of comment msg

            int c1 = 0;
            while (true) {
                while (c1 != -1 && c1 != '*') {
                    if (c1 == '\n') {
                        noOfLine++;
                    }
                    c1 = this.reader.read();
                }
                if (c1 == -1) {
                    throwSyntaxError(String.format("unexpected end of comment, started at %s line %d",
                            inputPath, lineNo));
                } else {
                    // c1 == '*'
                    int c2 = this.reader.read();
                    if (c2 == '\n') {
                        noOfLine++;
                    }
                    if (c2 == '/') {
                        break;
                    } else if (c2 == -1) {
                        throwSyntaxError(String.format("unexpected end of comment, started at %s line %d",
                                inputPath, lineNo));
                    }
                    c1 = 0;
                }
            }
            this.lineNo += noOfLine;
            return true;
        } else {
            reader.reset();
            return false;
        }
    }

    private void skipWhitespacesAndComments() throws IOException, SyntaxException {
        boolean skipped;
        do {
            skipped = skipWhitespaces() || skipComments();
        } while (skipped);
    }

    // When called, return the next token from the input stream.
    // Return TOKEN_EOF when reaching the end of the input stream.
    private Token nextTokenInternal() throws IOException, SyntaxException {
        if (includeLexer != null) {
            Token next;
            try {
                next = includeLexer.nextToken();
            } catch (SyntaxException ex) {
                throw new SyntaxException(new Msg(
                        String.format("in file included from %s line %d: ", inputPath, lineNo),
                        ex.getMsg()));
            }
            if (next.kind == Kind.TOKEN_EOF) {
                includeLexer = null;
            } else {
                return next;
            }
        }

        skipWhitespacesAndComments();

        int c = reader.read();
        if (-1 == c) {
            return new Token(Kind.TOKEN_EOF, "<EOF>", this);
        }

        switch (c) {
            case '=':
                return new Token(Kind.TOKEN_ASSIGN, "=", this);
            case 'b':
                if (expectFollowing("ool"))
                    return new Token(Kind.TOKEN_BOOL, "bool", this);
                else if (expectFollowing("yte"))
                    return new Token(Kind.TOKEN_BYTE, "byte", this);
                else if (expectFollowing("inary"))
                    return new Token(Kind.TOKEN_BINARY, "binary", this);
                break;
            case 'd':
                if (expectFollowing("ouble"))
                    return new Token(Kind.TOKEN_DOUBLE, "double",this);
                break;
            case ',':
                return new Token(Kind.TOKEN_COMMA, ",", this);
            case '.':
                return new Token(Kind.TOKEN_DOT, ".", this);
            case 'e':
                if (expectFollowing("num"))
                    return new Token(Kind.TOKEN_ENUM, "enum", this);
                else if (expectFollowing("xception"))
                    return new Token(Kind.TOKEN_EXCEPTION, "exception", this);
                break;
            case 'f':
                if (expectFollowing("alse"))
                    return new Token(Kind.TOKEN_FALSE, "false", this);
                break;
            case 'i':
                if (expectFollowing("16"))
                    return new Token(Kind.TOKEN_I16, "i16", this);
                else if (expectFollowing("32"))
                    return new Token(Kind.TOKEN_I32, "i32", this);
                else if (expectFollowing("64"))
                    return new Token(Kind.TOKEN_I64,"i64", this);
                else if (expectFollowing("nclude")) {
                    skipWhitespacesAndComments();
                    c = this.reader.read();
                    if ('"' != c) {
                        throwSyntaxError("illegal include statement");
                    }
                    Token filePath = buildStringLiteral();
                    String dirPath = Utils.getDirPath(inputPath);
                    File includeFile = new File(dirPath + File.separator + filePath.literal);
                    Reader includeReader = null;
                    try {
                        includeReader = new InputStreamReader(new FileInputStream(includeFile), "UTF8");
                    } catch (FileNotFoundException ex) {
                        throwSyntaxError(String.format("include file '%s' not found in '%s'",
                                filePath.literal, includeFile.getCanonicalPath()));
                    }
                    includeLexer = new Lexer(includeFile.getCanonicalPath(), includeReader);
                    Token next;
                    try {
                        next = includeLexer.nextToken();
                    } catch (SyntaxException ex) {
                        throw new SyntaxException(new Msg(
                                String.format("in file included from %s line %d: ", inputPath, lineNo),
                                ex.getMsg()));
                    }
                    if (next.kind == Kind.TOKEN_EOF) {
                        includeLexer = null;
                        return nextTokenInternal();
                    }
                    return next;
                }
                break;
            case '{':
                return new Token(Kind.TOKEN_LBRACE, "{", this);
            case '(':
                return new Token(Kind.TOKEN_LPAREN, "(", this);
            case '<':
                return new Token(Kind.TOKEN_LT, "<", this);
            case '>':
                return new Token(Kind.TOKEN_GT, ">", this);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return new Token(Kind.TOKEN_NUM, buildNum(c), this);
            case 'o':
                if (expectFollowing("ptional"))
                    return new Token(Kind.TOKEN_OPTIONAL, "optional", this);
                break;
            case '}':
                return new Token(Kind.TOKEN_RBRACE, "}", this);
            case 'r':
                if (expectFollowing("equired"))
                    return new Token(Kind.TOKEN_REQUIRED, "required", this);
                break;
            case ')':
                return new Token(Kind.TOKEN_RPAREN, ")", this);
            case ';':
                return new Token(Kind.TOKEN_SEMI, ";", this);
            case ':':
                return new Token(Kind.TOKEN_COLON, ":", this);
            case 'l':
                if (expectFollowing("ist"))
                    return new Token(Kind.TOKEN_LIST, "list", this);
                break;
            case 'm':
                if (expectFollowing("ap"))
                    return new Token(Kind.TOKEN_MAP, "map", this);
                break;
            case 'n':
                if (expectFollowing("amespace"))
                    return new Token(Kind.TOKEN_NAMESPACE, "namespace", this);
                break;
            case 's':
                if (expectFollowing("tring"))
                    return new Token(Kind.TOKEN_STRING, "string", this);
                else if (expectFollowing("truct"))
                    return new Token(Kind.TOKEN_STRUCT, "struct", this);
                else if (expectFollowing("et"))
                    return new Token(Kind.TOKEN_SET, "set", this);
                else if (expectFollowing("ervice"))
                    return new Token(Kind.TOKEN_SERVICE, "service", this);
                break;
            case 't':
                if (expectFollowing("rue"))
                    return new Token(Kind.TOKEN_TRUE, "true", this);
                else if (expectFollowing("hrows"))
                    return new Token(Kind.TOKEN_THROWS, "throws", this);
                break;
            case 'v':
                if (expectFollowing("oid"))
                    return new Token(Kind.TOKEN_VOID, "void", this);
                break;
            case '"': {
                return buildStringLiteral();
            }
        }
        return new Token(Kind.TOKEN_ID, buildId(c), this);
    }

    // expect current char is "
    private Token buildStringLiteral() throws IOException, SyntaxException {
        int noOfLine = 0; // for update lineNo
        StringBuilder sb = new StringBuilder();
        int c1 = this.reader.read();
        boolean escape = false;
        while (true) {
            while (c1 != -1 && c1 != '"') {
                if (c1 == '\n') {
                    noOfLine++;
                }
                if (escape) {
                    switch (c1) {
                        case 'r':
                            sb.append("\r");
                            break;
                        case 't':
                            sb.append("\t");
                            break;
                        case 'n':
                            sb.append("\n");
                            break;
                        case '\\':
                            sb.append("\\");
                            c1 = this.reader.read();
                            escape = false;
                            continue;
                        case '\n':
                            break;
                        default:
                            throwSyntaxError("unknown escaped \\" + c1 + " in string literal");
                    }
                    escape = false;
                } else {
                    if (c1 == '\\') {
                        escape = true;
                    } else {
                        sb.append((char) c1);
                    }
                }
                c1 = this.reader.read();
            }
            if (c1 == -1) {
                throwSyntaxError("unexpected end of string literal");
            }
            // c1 == '"'
            if (escape) {
                escape = false;
                sb.append("\"");
                c1 = this.reader.read();
                continue;
            }
            break;
        }

        this.lineNo += noOfLine;
        return new Token(Kind.TOKEN_STRING_LITERAL, sb.toString(), this);
    }

    private boolean expectFollowing(String expectedString) throws IOException {
        this.reader.mark(expectedString.length() + 1);
        for (int i = 0; i < expectedString.length(); i++) {
            if (expectedString.charAt(i) != this.reader.read()) {
                this.reader.reset();
                return false;
            }
        }
        this.reader.mark(1);
        int c = this.reader.read();
        this.reader.reset();
        return c != '_' && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')
                && !(c >= '0' && c <= '9');
    }

    private String buildId(int s) throws IOException, SyntaxException {
        if (!(s == '_' || (s >= 'a' && s <= 'z') || (s >= 'A' && s <= 'Z'))) { // not in [_a-zA-Z]
            throwSyntaxError("unexpected char '" + (char) s + "' when trying to build identifier");
        }
        StringBuilder sb = new StringBuilder();
        sb.append((char) s);
        for (;;) {
            this.reader.mark(1);
            int c = this.reader.read();
            if (c != '_' && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')
                    && !(c >= '0' && c <= '9')) { // not in [_a-zA-Z0-9]
                this.reader.reset();
                break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    private String buildNum(int s) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) s);
        for (;;) {
            this.reader.mark(1);
            int c = this.reader.read();
            if (c < '0' || c > '9') {
                this.reader.reset();
                break;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    public Token nextToken() throws SyntaxException {
        Token t = null;

        try {
            t = this.nextTokenInternal();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // TODO, how to deal with this
        }
        if (Control.debug) {
            System.err.format("nextToken: %s %d '%s' %s\n", inputPath, lineNo, t.literal, t.kind);
        }
        return t;
    }

    // help to build beautiful err msg for included file
    public class Msg {
        public final String msg;
        public final Msg sub;

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

    private Msg buildSyntaxErrorMsg(String msg) {
        if (this.includeLexer == null) {
            return new Msg(String.format("in file %s line %d: %s", inputPath, lineNo, msg), null);
        }
        return new Msg(String.format("in file included from %s line %d: ", inputPath, lineNo),
                includeLexer.buildSyntaxErrorMsg(msg));
    }

    public void throwSyntaxError(String msg) throws SyntaxException {
        Msg errMsg = buildSyntaxErrorMsg(msg);
        throw new SyntaxException(errMsg);
    }
}
