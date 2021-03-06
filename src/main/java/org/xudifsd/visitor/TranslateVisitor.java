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
import org.xudifsd.ast.type.ThriftDefaultValue;
import org.xudifsd.ast.type.ThriftDualContainer;
import org.xudifsd.ast.type.ThriftSelfDefinedType;
import org.xudifsd.ast.type.ThriftSingleContainer;
import org.xudifsd.util.Temp;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TranslateVisitor implements Visitor {
    private int indentLevel;
    private PrintStream outputStream;
    private Map<ThriftFile, String> scopeAlias = new HashMap<ThriftFile, String>();

    private ThriftFile currentFile = null;

    // for generate nested fields
    // outerType setup inTemp to json source, and inner Type use inTemp to generate outTemp
    private String inTemp;
    private String outTemp;

    public TranslateVisitor(PrintStream outputStream) {
        this.indentLevel = 0;
        this.outputStream = outputStream;
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

    private String genGetFunctionName(ThriftFile thriftFile, String selfDefinedType) {
        String scope = thriftFile.fileName.replace('.', '_');
        return String.format("get_%s_%s", scope, selfDefinedType);
    }

    private void printGetFunctionDoc(ThriftFile thriftFile, String selfDefinedType) {
        printlnWithIndent(String.format("\"\"\" translate %s \"\"\"", selfDefinedType));
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
    public void visit(ThriftDefaultValue value) {
        // do nothing
    }

    @Override
    public void visit(ThriftBasicType type) {
        // TODO maybe let user add snippet code and invoke here to support binary type?
        outTemp = Temp.next();
        printlnWithIndent(String.format("%s = %s",
                outTemp, inTemp));
    }

    @Override
    public void visit(ThriftSelfDefinedType type) {
        outTemp = Temp.next();
        String scope = type.scope;
        ThriftFile thriftFile = currentFile;
        if (scope != null) {
            thriftFile = currentFile.getIncludedFiles().get(scope);
        }
        printlnWithIndent(String.format("%s = ClientGen.%s(%s)",
                outTemp, genGetFunctionName(thriftFile, type.name), inTemp));
    }

    @Override
    public void visit(ThriftSingleContainer type) {
        // TODO if innerType is basic type, we may pass directly, but what if we want to support user defined
        // transform function for binary?
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
        // TODO if innerType is basic type, we may pass directly, but what if we want to support user defined
        // transform function for binary?
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
        // ignore default value here, underlying library will do it for us
        printlnWithIndent(String.format("if json_value.get(\"%s\") is not None:", field.name));
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
        printlnWithIndent("def " + genGetFunctionName(currentFile, thriftStruct.name) + "(json_value):");
        indent();
        printGetFunctionDoc(currentFile, thriftStruct.name);
        printlnWithIndent("# check required fields");
        boolean hasRequired = false;
        Iterator<ThriftField> allFields = thriftStruct.getAllFields().iterator();
        while (allFields.hasNext()) {
            ThriftField field = allFields.next();
            if (field.modifier.type != ThriftModifier.Type.REQUIRED) {
                break;
            }
            hasRequired = true;
            printlnWithIndent(String.format("if json_value.get(\"%s\") is None:", field.name));
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
        printlnWithIndent("result = " + String.format("%s.%s", scopeAlias.get(currentFile), thriftStruct.name) + "()");

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
        printlnWithIndent("@staticmethod");
        printlnWithIndent("def " + genGetFunctionName(currentFile, thriftEnum.name) + "(json_value):");
        indent();
        printGetFunctionDoc(currentFile, thriftEnum.name);

        printlnWithIndent("str_map = {");
        indent();
        Iterator<String> keyIt = thriftEnum.getEnums().keySet().iterator();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            printSpaces();
            print(String.format("\"%s\": %s.%s.%s", key, scopeAlias.get(currentFile), thriftEnum.name, key));
            if (keyIt.hasNext()) {
                print(",\n");
            } else {
                print("\n");
            }
        }
        unIndent();
        printlnWithIndent("}");

        printlnWithIndent("int_map = {");
        indent();
        Iterator<Map.Entry<String, Integer>> it = thriftEnum.getEnums().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            printSpaces();
            print(String.format("%d: %s.%s.%s", value, scopeAlias.get(currentFile), thriftEnum.name, key));
            if (it.hasNext()) {
                print(",\n");
            } else {
                print("\n");
            }
        }
        unIndent();
        printlnWithIndent("}");

        printlnWithIndent("if type(json_value) == str:");
        indent();

        printlnWithIndent("if str_map.get(json_value) is None:");
        indent();
        printlnWithIndent("raise KeyError(\"unknown \" + str(json_value)");
        indent();
        printlnWithIndent(String.format("+ \" in enum %s\")", thriftEnum.name));
        unIndent();
        unIndent();
        printlnWithIndent("else:");
        indent();
        printlnWithIndent("return str_map[json_value]");
        unIndent();
        unIndent();

        printlnWithIndent("elif type(json_value) == int:");
        indent();

        printlnWithIndent("if int_map.get(json_value) is None:");
        indent();
        printlnWithIndent("raise KeyError(\"unknown \" + str(json_value)");
        indent();
        printlnWithIndent(String.format("+ \" in enum %s\")", thriftEnum.name));
        unIndent();
        unIndent();
        printlnWithIndent("else:");
        indent();
        printlnWithIndent("return int_map[json_value]");
        unIndent();
        unIndent();

        printlnWithIndent("else:");
        indent();
        printlnWithIndent("raise TypeError(\"only support str/int in json as enum value, but get \"");
        indent();
        printlnWithIndent(String.format("+ str(type(json_value)) + \" in enum %s\")", thriftEnum.name));
        unIndent();
        unIndent();

        unIndent();
        print("\n");
    }

    private String getScopeName(ThriftFile file) {
        String result = file.fileName;
        if (file.getNamespaces().get("*") != null) {
            result = file.getNamespaces().get("*").scope;
        }
        if (file.getNamespaces().get("py") != null) {
            result = file.getNamespaces().get("py").scope;
        }
        return result;
    }

    @Override
    public void visit(ThriftFile thriftFile) {
        currentFile = thriftFile;
        for (NamedItem item : thriftFile.getItems().values()) {
            item.accept(this);
        }
        for (ThriftFile includedFile : thriftFile.getIncludedFiles().values()) {
            visit(includedFile);
        }
    }

    private void genInclude(ThriftFile thriftFile) {
        scopeAlias.put(thriftFile, Temp.next());
        printlnWithIndent(String.format("import %s.ttypes as %s",
                getScopeName(thriftFile), scopeAlias.get(thriftFile)));
        for (ThriftFile includedFile : thriftFile.getIncludedFiles().values()) {
            genInclude(includedFile);
        }
    }

    public void translate(ThriftFile thriftFile) {
        printlnWithIndent("#!/usr/bin/env python");
        printlnWithIndent("# -*- coding: utf-8 -*-");
        printlnWithIndent("");
        printlnWithIndent("\"\"\" auto generated code convert json into thrift \"\"\"");
        printlnWithIndent("");
        printlnWithIndent("# this is auto-generated file, do not edit this");
        printlnWithIndent("# generated by ClientGen https://github.com/xudifsd/client-gen");
        printlnWithIndent("");
        genInclude(thriftFile);
        printlnWithIndent("");
        printlnWithIndent("class ClientGen(object):");
        indent();
        printlnWithIndent("\"\"\" auto generated stub class convert json to thrift \"\"\"");
        visit(thriftFile);
    }
}
