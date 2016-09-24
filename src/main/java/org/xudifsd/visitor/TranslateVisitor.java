package org.xudifsd.visitor;

import org.xudifsd.ast.NamedItem;
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
import org.xudifsd.ast.type.ThriftSelfDefinedType;
import org.xudifsd.ast.type.ThriftSingleContainer;
import org.xudifsd.util.Temp;
import org.xudifsd.util.Utils;
import org.xudifsd.visitor.Visitor;

import java.io.PrintStream;
import java.util.Iterator;

public class TranslateVisitor implements Visitor {
    private int indentLevel;
    private PrintStream outputStream;
    private String thriftFileName;

    // for generate nested fields
    // outerType setup inTemp to json source, and inner Type use inTemp to generate outTemp
    private String inTemp;
    private String outTemp;

    public TranslateVisitor(String thriftFileName, PrintStream outputStream) {
        this.indentLevel = 0;
        this.outputStream = outputStream;
        this.thriftFileName = thriftFileName;
    }

    private void indent() {
        this.indentLevel += 4;
    }

    private void unIndent() {
        this.indentLevel -= 4;
    }

    private void printSpaces() {
        int i = this.indentLevel;
        while (i-- != 0)
            this.print(" ");
    }

    private void printlnWithIndent(String s) {
        printSpaces();
        outputStream.println(s);
    }

    private void print(String s) {
        outputStream.print(s);
    }

    private String genGetFunctionName(String selfDefinedType) {
        return "get" + Utils.capitalize(selfDefinedType);
    }

    @Override
    public void visit(ThriftException ex) {
        // do nothing
    }

    @Override
    public void visit(ThriftService service) {
        // do nothing
    }

    @Override
    public void visit(ThriftMethod method) {
        // do nothing
    }

    @Override
    public void visit(ThriftBasicType type) {
        outTemp = Temp.next();
        printlnWithIndent(String.format("%s = %s",
                outTemp, inTemp));
    }

    @Override
    public void visit(ThriftSelfDefinedType type) {
        outTemp = Temp.next();
        printlnWithIndent(String.format("%s = ClientGen.%s(%s)",
                outTemp, genGetFunctionName(type.name), inTemp));
    }

    @Override
    public void visit(ThriftSingleContainer type) {
        String myOutTemp = Temp.next();
        String addMethod;
        if (type.type == ThriftSingleContainer.Type.LIST) {
            printlnWithIndent(String.format("%s = []", myOutTemp));
            addMethod = "append";
        } else if (type.type == ThriftSingleContainer.Type.SET) {
            printlnWithIndent(String.format("%s = set()", myOutTemp));
            addMethod = "add";
        } else {
            throw new UnsupportedOperationException(type.type + "is not implemented");
        }
        String myInTemp = inTemp;

        inTemp = Temp.next();
        printlnWithIndent(String.format("for %s in %s:", inTemp, myInTemp));
        indent();
        type.innerType.accept(this);
        printlnWithIndent(String.format("%s.%s(%s)", myOutTemp, addMethod, outTemp));
        unIndent();
        outTemp = myOutTemp;
    }

    @Override
    public void visit(ThriftDualContainer type) {
        String myOutTemp = Temp.next();
        printlnWithIndent(String.format("%s = {}", myOutTemp));

        String keyIn = Temp.next();
        String valueIn = Temp.next();
        printlnWithIndent(String.format("for (%s, %s) in %s.items():", keyIn, valueIn, inTemp));
        indent();

        printlnWithIndent("# gen for key");
        inTemp = keyIn;
        type.key.accept(this);
        String keyOut = outTemp;

        printlnWithIndent("# gen for value");
        inTemp = valueIn;
        type.value.accept(this);
        String valueOut = outTemp;

        printlnWithIndent(String.format("%s[%s] = %s", myOutTemp, keyOut, valueOut));

        outTemp = myOutTemp;
        unIndent();
    }

    @Override
    public void visit(ThriftField field) {
        printlnWithIndent(String.format("if json_value.get(\"%s\") != None:", field.name));
        indent();
        inTemp = Temp.next();
        printlnWithIndent(String.format("%s = json_value[\"%s\"]", inTemp, field.name));
        field.type.accept(this);
        printlnWithIndent(String.format("result.%s = %s", field.name, outTemp));
        unIndent();
    }

    @Override
    public void visit(ThriftStruct thriftStruct) {
        printlnWithIndent("@staticmethod");
        printlnWithIndent("def get" + Utils.capitalize(thriftStruct.name) + "(json_value):");
        indent();
        printlnWithIndent("# check required fields");
        boolean hasRequired = false;
        Iterator<ThriftField> allFields = thriftStruct.getAllFields().iterator();
        while (allFields.hasNext()) {
            ThriftField field = allFields.next();
            if (field.modifier.type != ThriftModifier.Type.REQUIRED) {
                break;
            }
            hasRequired = true;
            printlnWithIndent(String.format("if json_value.get(\"%s\") == None:", field.name));
            indent();
            printlnWithIndent(String.format("raise KeyError(\"%s is required in struct %s\")",
                    field.name, thriftStruct.name));
            unIndent();
        }
        if (!hasRequired) {
            printlnWithIndent("pass # no required field");
        }

        print("\n");
        printlnWithIndent("# fill in fields");
        printlnWithIndent("result = " + thriftStruct.name + "()");

        allFields = thriftStruct.getAllFields().iterator();
        while (allFields.hasNext()) {
            ThriftField field = allFields.next();
            field.accept(this);

            if (allFields.hasNext()) {
                print("\n");
            }
        }

        printlnWithIndent("return result");

        unIndent();
        print("\n");
    }

    @Override
    public void visit(ThriftEnum thriftEnum) {
        // TODO current ignore namespace
        printlnWithIndent("@staticmethod");
        printlnWithIndent("def " + genGetFunctionName(thriftEnum.name) + "(json_value):");
        indent();
        printlnWithIndent("type_map = {");
        indent();
        Iterator<String> keyIt = thriftEnum.getEnums().keySet().iterator();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            printSpaces();
            print(String.format("\"%s\": %s.%s", key, thriftEnum.name, key));
            if (keyIt.hasNext()) {
                print(",\n");
            } else {
                print("\n");
            }
        }
        unIndent();
        printlnWithIndent("}");
        printlnWithIndent("if type_map.get(json_value) == None:");
        indent();
        printlnWithIndent(String.format("raise KeyError(\"unknown \" + str(json_value) + \" in enum %s\")",
                thriftEnum.name));
        unIndent();
        printlnWithIndent("else:");
        indent();
        printlnWithIndent("return type_map[json_value]");
        unIndent();
        unIndent();
        print("\n");
    }

    @Override
    public void visit(ThriftFile thriftFile) {
        printlnWithIndent("#!/usr/bin/env python");
        printlnWithIndent("# -*- coding: utf-8 -*-");
        printlnWithIndent("");
        printlnWithIndent(String.format("from %s.ttypes import *", thriftFileName));
        printlnWithIndent("");
        printlnWithIndent("class ClientGen:");
        indent();
        for (NamedItem item : thriftFile.getItems()) {
            item.accept(this);
        }
    }
}
