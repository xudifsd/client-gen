package org.xudifsd.visitor;

import org.xudifsd.ast.NamedItem;
import org.xudifsd.ast.ThriftEnum;
import org.xudifsd.ast.ThriftException;
import org.xudifsd.ast.ThriftField;
import org.xudifsd.ast.ThriftFile;
import org.xudifsd.ast.ThriftMethod;
import org.xudifsd.ast.ThriftService;
import org.xudifsd.ast.ThriftStruct;
import org.xudifsd.ast.type.ThriftBasicType;
import org.xudifsd.ast.type.ThriftDualContainer;
import org.xudifsd.ast.type.ThriftNamespace;
import org.xudifsd.ast.type.ThriftSelfDefinedType;
import org.xudifsd.ast.type.ThriftSingleContainer;
import org.xudifsd.ast.type.ThriftType;
import org.xudifsd.lexer.SyntaxException;
import org.xudifsd.parser.Msg;
import org.xudifsd.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SanityCheckVisitor implements Visitor {
    private Set<String> allDefinition = new HashSet<>();
    private ThriftFile currentFile = null;

    @Override
    public void visit(ThriftBasicType type) {
        // do nothing
    }

    @Override
    public void visit(ThriftSelfDefinedType type) {
        if (type.scope == null) {
            if (!currentFile.getAllNames().contains(type.name)) {
                throw new SanityCheckException(
                        String.format("type %s not defined", type.name));
            }
        } else {
            ThriftFile includedFile = currentFile.getIncludedFiles().get(type.scope);
            if (includedFile == null) {
                throw new SanityCheckException(
                        String.format("type %s.%s not defined", type.scope, type.name));
            }
            if (!includedFile.getAllNames().contains(type.name)) {
                throw new SanityCheckException(
                        String.format("type %s.%s not defined", type.name, type.scope));
            }
        }
    }

    @Override
    public void visit(ThriftSingleContainer type) {
        ThriftType innerType = type.innerType;
        innerType.accept(this);
    }

    @Override
    public void visit(ThriftDualContainer type) {
        ThriftType key = type.key;
        ThriftType value = type.value;
        key.accept(this);
        value.accept(this);
    }

    @Override
    public void visit(ThriftException ex) {
        for (ThriftField field : ex.getAllFields()) {
            field.accept(this);
        }
    }

    @Override
    public void visit(ThriftService service) {
        for (ThriftMethod method : service.getMethods().values()) {
            method.accept(this);
        }
    }

    @Override
    public void visit(ThriftMethod method) {
        for (ThriftField field : method.getParList()) {
            field.accept(this);
        }
        for (ThriftField field : method.getExceptionList()) {
            field.accept(this);
        }
    }

    @Override
    public void visit(ThriftField thriftField) {
        ThriftType type = thriftField.type;
        type.accept(this);
        // TODO check default value if added support for it
    }

    @Override
    public void visit(ThriftStruct thriftStruct) {
        for (ThriftField field : thriftStruct.getAllFields()) {
            field.accept(this);
        }
    }

    @Override
    public void visit(ThriftEnum thriftEnum) {
        // do nothing
    }

    @Override
    public void visit(ThriftFile thriftFile) {
        currentFile = thriftFile;

        for (NamedItem item : thriftFile.getItems()) {
            item.accept(this);
        }

        for (ThriftFile includedFile : thriftFile.getIncludedFiles().values()) {
            includedFile.accept(this);
        }
    }

    // check if same lang has same scope
    private boolean allowSame(Map<String, Set<String>> allNs, ThriftFile file) {
        if (file == null) {
            return true;
        }
        for (Map.Entry<String, ThriftNamespace> entry :file.getNamespaces().entrySet()) {
            String lang = entry.getKey();
            String scope = entry.getValue().scope;
            if (allNs.get(lang) == null) {
                allNs.put(lang, new HashSet<String>());
            }
            if (allNs.get(lang).contains(scope)) {
                return false;
            }
            if (lang.equals("*")) {
                for (Set<String> s : allNs.values()) {
                    if (s.contains(scope)) {
                        return false;
                    }
                }
            }
            if (allNs.get("*") != null && allNs.get("*").contains(scope)) {
                return false;
            }
            allNs.get(lang).add(scope);
        }
        for (ThriftFile includedFile : file.getIncludedFiles().values()) {
            if (!allowSame(allNs, includedFile)) {
                return false;
            }
        }
        return true;
    }

    private void checkDoNotContainSameName(ThriftFile thriftFile) throws SyntaxException {
        Set<String> intersect = Utils.intersection(allDefinition, thriftFile.getAllNames());
        if (!intersect.isEmpty()) {
            throw new SyntaxException(new Msg(String.format("duplicated name %s", intersect), null));
        }
        allDefinition.addAll(thriftFile.getAllNames());
        for (ThriftFile includedFile : thriftFile.getIncludedFiles().values()) {
            checkDoNotContainSameName(includedFile);
        }
    }

    private class SanityCheckException extends RuntimeException {
        // used to avoid add exception list to Visitor methods

        public SanityCheckException(String message) {
            super(message);
        }
    }

    public void check(ThriftFile thriftFile) throws SyntaxException {
        try {
            // check if has duplicated definition even in included file
            Map<String, Set<String>> allNs = new HashMap<>();
            if (!allowSame(allNs, thriftFile)) {
                checkDoNotContainSameName(thriftFile);
            }

            visit(thriftFile);
            for (ThriftFile includedFile : thriftFile.getIncludedFiles().values()) {
                visit(includedFile);
            }
        } catch (SanityCheckException ex) {
            throw new SyntaxException(new Msg(ex.getMessage()));
        }
    }
}
