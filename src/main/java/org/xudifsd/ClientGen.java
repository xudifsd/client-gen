package org.xudifsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import org.xudifsd.ast.ThriftFile;
import org.xudifsd.ast.TranslateVisitor;
import org.xudifsd.control.CommandLine;
import org.xudifsd.control.Control;
import org.xudifsd.lexer.SyntaxException;
import org.xudifsd.parser.Parser;
import org.xudifsd.util.Utils;

public class ClientGen {
    private static ClientGen clientGen;
    private static CommandLine cmd;
    private static Reader reader;
    private ThriftFile file;

    public ThriftFile lexAndParse(String inName) throws IOException, SyntaxException {
        Parser parser;

        File in = new File(inName);
        reader = new InputStreamReader(new FileInputStream(in), "UTF8");
        parser = new Parser(in, reader);

        return parser.parse();
    }

    public void compile(String srcName, String outputName) throws IOException, SyntaxException {
        if (srcName == null) {
            cmd.usage();
            return;
        }
        Control.fileName = srcName;

        if (Control.debug) {
            System.out.println(lexAndParse(srcName));
        } else {
            file = lexAndParse(srcName);
            PrintStream outputFile;
            if (outputName == null) {
                outputFile = System.out;
            } else {
                outputFile = new PrintStream(outputName);
            }
            String thriftFileName = Utils.getFileName(srcName);
            TranslateVisitor translateVisitor = new TranslateVisitor(thriftFileName, outputFile);
            translateVisitor.visit(file);
        }
    }

    public static void main(String[] args) throws IOException {
        clientGen = new ClientGen();
        cmd = new CommandLine();
        String inFile = cmd.scan(args);
        try {
            clientGen.compile(inFile, Control.outputName);
        } catch (SyntaxException e) {
            System.err.println(e.getMsg().toString());
            if (Control.debug) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}
