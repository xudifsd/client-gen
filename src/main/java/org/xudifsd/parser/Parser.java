package org.xudifsd.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.xudifsd.ast.ThriftEnum;
import org.xudifsd.ast.ThriftException;
import org.xudifsd.ast.ThriftField;
import org.xudifsd.ast.ThriftFile;
import org.xudifsd.ast.ThriftMethod;
import org.xudifsd.ast.ThriftModifier;
import org.xudifsd.ast.ThriftService;
import org.xudifsd.ast.ThriftStruct;
import org.xudifsd.ast.type.ThriftBasicType;
import org.xudifsd.ast.type.ThriftDualContainer;
import org.xudifsd.ast.type.ThriftNamespace;
import org.xudifsd.ast.type.ThriftSelfDefinedType;
import org.xudifsd.ast.type.ThriftSingleContainer;
import org.xudifsd.ast.type.ThriftType;
import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;
import org.xudifsd.lexer.Token;
import org.xudifsd.lexer.Token.Kind;
import org.xudifsd.util.Utils;

public class Parser {
    private Lexer lexer;
    private Token current;

    public Parser(String inputPath, Reader reader) throws SyntaxException, IOException {
        lexer = new Lexer(inputPath, reader);
        current = lexer.nextToken();
    }

    // /////////////////////////////////////////////
    // utility methods to connect the lexer
    // and the parser.

    private void advance() throws SyntaxException {
        current = lexer.nextToken();
    }

    private void eatToken(Kind kind) throws SyntaxException {
        if (kind == current.kind)
            advance();
        else {
            lexer.throwSyntaxError(String.format("expect %s, got '%s'",
                    kind.toString(), current.literal));
        }
    }

    private void eatTokenOptional(Kind... kinds) throws SyntaxException {
        for (Kind kind : kinds) {
            if (kind == current.kind) {
                advance();
                break;
            }
        }
    }

    private void error(String hint) throws SyntaxException {
        lexer.throwSyntaxError(hint);
    }

    // ////////////////////////////////////////////////////////////
    // below are method for parsing.

    private ThriftType parseType(boolean allowVoid) throws SyntaxException {
        if (current.kind == Kind.TOKEN_BOOL) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.BOOL);
        } else if (current.kind == Kind.TOKEN_I16) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.I16);
        } else if (current.kind == Kind.TOKEN_I32) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.I32);
        } else if (current.kind == Kind.TOKEN_I64) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.I64);
        } else if (current.kind == Kind.TOKEN_BYTE) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.BYTE);
        } else if (current.kind == Kind.TOKEN_BINARY) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.BINARY);
        } else if (current.kind == Kind.TOKEN_DOUBLE) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.DOUBLE);
        } else if (current.kind == Kind.TOKEN_STRING) {
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.STRING);
        } else if (current.kind == Kind.TOKEN_VOID) {
            if (!allowVoid) {
                lexer.throwSyntaxError("do not allow void type here");
            }
            advance();
            return new ThriftBasicType(ThriftBasicType.Type.VOID);
        }
        // container type
        else if (current.kind == Kind.TOKEN_LIST) {
            advance();
            eatToken(Kind.TOKEN_LT);
            ThriftType innerType = parseType(false);
            eatToken(Kind.TOKEN_GT);
            return new ThriftSingleContainer(ThriftSingleContainer.Type.LIST,
                    innerType);
        } else if (current.kind == Kind.TOKEN_SET) {
            advance();
            eatToken(Kind.TOKEN_LT);
            ThriftType innerType = parseType(false);
            eatToken(Kind.TOKEN_GT);
            return new ThriftSingleContainer(ThriftSingleContainer.Type.SET,
                    innerType);
        } else if (current.kind == Kind.TOKEN_MAP) {
            advance();
            eatToken(Kind.TOKEN_LT);
            ThriftType keyType = parseType(false);
            eatToken(Kind.TOKEN_COMMA);
            ThriftType valueType = parseType(false);
            eatToken(Kind.TOKEN_GT);
            return new ThriftDualContainer(keyType, valueType);
        }
        // self defined type, including struct, enum, exception
        else {
            String name = current.literal;
            eatToken(Kind.TOKEN_ID);
            if (current.kind == Kind.TOKEN_DOT) {
                // TODO currently only support included file in same dir, if want to support
                // more, change code generator also.
                eatToken(Kind.TOKEN_DOT);
                String nextLevelName = current.literal;
                eatToken(Kind.TOKEN_ID);
                return new ThriftSelfDefinedType(name, nextLevelName);
            } else {
                return new ThriftSelfDefinedType(null, name);
            }
        }
    }

    // Type id '(' Field* ')' (throws '(' Field* ')' )? ;
    private ThriftMethod parseMethodDef() throws SyntaxException {
        ThriftType returnType = parseType(true);
        String id = current.literal;
        eatToken(Kind.TOKEN_ID);
        ThriftMethod result = new ThriftMethod(id, returnType);
        eatToken(Kind.TOKEN_LPAREN);

        while (current.kind != Kind.TOKEN_RPAREN) {
            result.addParameter(lexer, parseField());
        }

        eatToken(Kind.TOKEN_RPAREN);

        if (current.kind == Kind.TOKEN_THROWS) {
            eatToken(Kind.TOKEN_THROWS);
            eatToken(Kind.TOKEN_LPAREN);

            while (current.kind != Kind.TOKEN_RPAREN) {
                result.addException(lexer, parseField());
            }

            eatToken(Kind.TOKEN_RPAREN);
        }
        eatToken(Kind.TOKEN_SEMI);
        return result;
    }

    // service id { MethodDef* }
    private ThriftService parseServiceDef() throws SyntaxException {
        eatToken(Kind.TOKEN_SERVICE);
        String id = current.literal;
        eatToken(Kind.TOKEN_ID);
        ThriftService result = new ThriftService(id);
        eatToken(Kind.TOKEN_LBRACE);

        while (current.kind != Kind.TOKEN_RBRACE) {
            result.addMethod(lexer, parseMethodDef());
        }

        eatToken(Kind.TOKEN_RBRACE);

        return result;
    }

    // exception id { Field* }
    private ThriftException parseExceptionDef() throws SyntaxException {
        eatToken(Kind.TOKEN_EXCEPTION);
        String id = current.literal;
        eatToken(Kind.TOKEN_ID);
        ThriftException result = new ThriftException(id);
        eatToken(Kind.TOKEN_LBRACE);

        while (current.kind != Kind.TOKEN_RBRACE) {
            result.add(lexer, parseField());
        }

        eatToken(Kind.TOKEN_RBRACE);
        return result;
    }

    // num: [required|option] Type id [= defaultValue]
    private ThriftField parseField() throws SyntaxException {
        eatToken(Kind.TOKEN_NUM); // TODO current ignore field num
        eatToken(Kind.TOKEN_COLON);
        ThriftModifier.Type type = ThriftModifier.Type.DEFAULT;
        if (current.kind == Kind.TOKEN_REQUIRED) {
            type = ThriftModifier.Type.REQUIRED;
            advance();
        }
        if (current.kind == Kind.TOKEN_OPTIONAL) {
            type = ThriftModifier.Type.OPTIONAL;
            advance();
        }
        ThriftModifier modifier = new ThriftModifier(type);
        ThriftType thriftType = parseType(false);
        String name = current.literal;
        eatToken(Kind.TOKEN_ID);
        if (current.kind == Kind.TOKEN_ASSIGN) {
            eatToken(Kind.TOKEN_ASSIGN);
            // TODO current ignore default value
            if (thriftType instanceof ThriftSelfDefinedType) {
                while (current.kind == Kind.TOKEN_ID || current.kind == Kind.TOKEN_DOT) {
                    advance();
                }
            } else {
                advance();
            }
        }
        eatTokenOptional(Kind.TOKEN_COMMA, Kind.TOKEN_SEMI);
        return new ThriftField(name, thriftType, modifier, null);
    }

    // struct id { Field * }
    private ThriftStruct parseStructDef() throws SyntaxException {
        eatToken(Kind.TOKEN_STRUCT);
        String id = current.literal;
        eatToken(Kind.TOKEN_ID);
        ThriftStruct result = new ThriftStruct(id);
        eatToken(Kind.TOKEN_LBRACE);

        while (current.kind != Kind.TOKEN_RBRACE) {
            result.add(lexer, parseField());
        }

        eatToken(Kind.TOKEN_RBRACE);
        return result;
    }

    // id = num[,;]
    private void parseEnumItem(ThriftEnum thriftEnum) throws SyntaxException {
        String key = current.literal;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_ASSIGN);
        String value = current.literal;
        eatToken(Kind.TOKEN_NUM);
        int v = Integer.valueOf(value);
        thriftEnum.addEnumItem(lexer, key, v);
        eatTokenOptional(Kind.TOKEN_COMMA, Kind.TOKEN_SEMI);
    }

    // enum id { EnumItem * }
    private ThriftEnum parseEnum() throws SyntaxException {
        eatToken(Kind.TOKEN_ENUM);
        String id = current.literal;
        eatToken(Kind.TOKEN_ID);
        ThriftEnum result = new ThriftEnum(id);
        eatToken(Kind.TOKEN_LBRACE);

        while (current.kind != Kind.TOKEN_RBRACE) {
            parseEnumItem(result);
        }
        eatToken(Kind.TOKEN_RBRACE);
        return result;
    }

    private class NamespaceScopeBuilder {
        private StringBuilder sb = new StringBuilder();
        private boolean switcher = true;

        public NamespaceScopeBuilder() {
        }

        public void append(Token token) throws SyntaxException {
            Kind kind = token.kind;
            String lexeme = token.literal;
            if (switcher && kind != Kind.TOKEN_ID
                    || (!switcher && kind != Kind.TOKEN_DOT)) {
                lexer.throwSyntaxError("illegal namespace scope '" + sb.toString() + "'");
            }
            switcher = !switcher;
            if (kind == Kind.TOKEN_ID) {
                sb.append(lexeme);
            } else {
                sb.append(".");
            }
        }

        @Override
        public String toString() {
            if (switcher) {
                try {
                    lexer.throwSyntaxError("illegal namespace scope '" + sb.toString() + "'");
                } catch (SyntaxException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

    // namespace lang scope
    // TODO lang can be * https://thrift.apache.org/docs/idl
    private ThriftNamespace parseNamespace() throws SyntaxException {
        eatToken(Kind.TOKEN_NAMESPACE);
        String lang = current.literal;
        if (current.kind != Kind.TOKEN_ID && current.kind != Kind.TOKEN_MUL) {
            lexer.throwSyntaxError(String.format("unexpected token '%s'", lang));
        }
        advance();

        NamespaceScopeBuilder nb = new NamespaceScopeBuilder();

        while (current.kind == Kind.TOKEN_DOT
                || current.kind == Kind.TOKEN_ID) {
            nb.append(current);
            advance();
        }
        return new ThriftNamespace(lang, nb.toString());
    }

    private ThriftFile processInclude() throws SyntaxException, IOException {
        eatToken(Kind.TOKEN_INCLUDE);
        String filePath = current.literal;
        eatToken(Kind.TOKEN_STRING_LITERAL);
        // open it
        String dirPath = Utils.getDirPath(lexer.getInputPath());
        File includeFile = new File(dirPath + File.separator + filePath);
        String canonicalPath = includeFile.getCanonicalPath();

        Reader includeReader = null;
        try {
            includeReader = new InputStreamReader(new FileInputStream(includeFile), "UTF8");
        } catch (FileNotFoundException ex) {
            lexer.throwSyntaxError(String.format("include file '%s' not found in '%s'",
                    filePath, canonicalPath));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("your system do not support utf8 encoding");
        }
        Parser includedParser = new Parser(canonicalPath, includeReader);
        ThriftFile result;
        try {
            result = includedParser.parse(Utils.getFileName(canonicalPath));
        } catch (SyntaxException ex) {
            lexer.throwSyntaxError(ex.getMsg());
            throw new RuntimeException("unreachable code, put here to avoid warning");
        }
        return result;
    }

    // ThriftFile -> ThriftEnum *
    private ThriftFile parseFile(String fileName) throws SyntaxException, IOException {
        ThriftFile result = new ThriftFile(fileName);

        while (current.kind != Kind.TOKEN_EOF) {
            if (current.kind == Kind.TOKEN_ENUM) {
                result.add(parseEnum());
            } else if (current.kind == Kind.TOKEN_STRUCT) {
                result.add(parseStructDef());
            } else if (current.kind == Kind.TOKEN_EXCEPTION) {
                result.add(parseExceptionDef());
            } else if (current.kind == Kind.TOKEN_NAMESPACE) {
                result.add(parseNamespace());
            } else if (current.kind == Kind.TOKEN_SERVICE) {
                result.add(parseServiceDef());
            } else if (current.kind == Kind.TOKEN_INCLUDE) {
                result.add(processInclude());
            } else {
                error("expect struct|enum|exception|namespace, but got " + current.literal);
            }
        }
        return result;
    }

    public ThriftFile parse(String fileName) throws SyntaxException, IOException {
        return parseFile(fileName);
    }
}
