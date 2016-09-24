package org.xudifsd.visitor;

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
    }

    @Override
    public void visit(ThriftSelfDefinedType type) {
    }

    @Override
    public void visit(ThriftSingleContainer type) {
    }

    @Override
    public void visit(ThriftDualContainer type) {
    }

    @Override
    public void visit(ThriftException ex) {
    }

    @Override
    public void visit(ThriftService service) {
    }

    @Override
    public void visit(ThriftMethod method) {
    }

    @Override
    public void visit(ThriftField thriftField) {
    }

    @Override
    public void visit(ThriftStruct thriftStruct) {
    }

    @Override
    public void visit(ThriftEnum thriftEnum) {
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

    @Override
    public void visit(ThriftFile thriftFile) {
        // TODO check if all type has its definition
        currentFile = thriftFile;
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

    public void check(ThriftFile thriftFile) throws SyntaxException{
        // check if has duplicated definition even in included file
        Map<String, Set<String>> allNs = new HashMap<>();
        if (!allowSame(allNs, thriftFile)) {
            checkDoNotContainSameName(thriftFile);
        }

        visit(thriftFile);
        for (ThriftFile includedFile : thriftFile.getIncludedFiles().values()) {
            visit(includedFile);
        }
    }
}
