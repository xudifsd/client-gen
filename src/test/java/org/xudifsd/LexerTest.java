package org.xudifsd;

import org.junit.Assert;
import org.junit.Test;
import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;
import org.xudifsd.lexer.Token;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class LexerTest {
    @Test
    public void testSkipWhitespace() throws IOException, SyntaxException {
        Reader reader = new StringReader(" \r\n\rabcd\r\n\rbc");
        Lexer lexer = new Lexer("stdin", reader);
        Token t = lexer.nextToken();

        Assert.assertEquals(t.kind, Token.Kind.TOKEN_ID);
        Assert.assertEquals(t.literal, "abcd");

        t = lexer.nextToken();
        Assert.assertEquals(t.kind, Token.Kind.TOKEN_ID);
        Assert.assertEquals(t.literal, "bc");

        t = lexer.nextToken();
        Assert.assertEquals(t.kind, Token.Kind.TOKEN_EOF);
    }

    @Test
    public void testSkipComments() throws IOException, SyntaxException {
        Reader reader = new StringReader(" // comment\r\n/*block comment*\rda*/abcd\r//ccc\n\rbc");
        Lexer lexer = new Lexer("stdin", reader);
        Token t = lexer.nextToken();

        Assert.assertEquals(t.kind, Token.Kind.TOKEN_ID);
        Assert.assertEquals(t.literal, "abcd");

        t = lexer.nextToken();
        Assert.assertEquals(t.kind, Token.Kind.TOKEN_ID);
        Assert.assertEquals(t.literal, "bc");

        t = lexer.nextToken();
        Assert.assertEquals(t.kind, Token.Kind.TOKEN_EOF);
    }

    @Test
    public void testSkipCommentsError() throws IOException {
        Reader reader = new StringReader(" // comment\r\n/*block comment\rabc*d\r//ccc\n\rbc");
        Lexer lexer = new Lexer("stdin", reader);
        try {
            lexer.nextToken();
            Assert.fail("should throw SyntaxException");
        } catch (SyntaxException e) {
            //
        }
    }

    @Test
    public void testBuildStringLiteral() throws IOException, SyntaxException {
        Reader reader = new StringReader("\"this is string literal\\\", with\\r\\n\\tquote in it\"");
        Lexer lexer = new Lexer("stdin", reader);
        Token t = lexer.nextToken();

        Assert.assertEquals(t.kind, Token.Kind.TOKEN_STRING_LITERAL);
        Assert.assertEquals(t.literal, "this is string literal\", with\r\n\tquote in it");
    }

    @Test
    public void testBuildId() throws IOException, SyntaxException {
        Reader reader = new StringReader("serviceScheduleStrategy");
        Lexer lexer = new Lexer("stdin", reader);
        Token t = lexer.nextToken();

        Assert.assertEquals(t.kind, Token.Kind.TOKEN_ID);
        Assert.assertEquals(t.literal, "serviceScheduleStrategy");
    }
}
