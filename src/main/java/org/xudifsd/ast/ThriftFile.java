package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftNamespace;
import org.xudifsd.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThriftFile implements Acceptable {
    public final String fileName; // do not contains dir and .thrift, used for default scope
    private Map<String, ThriftFile> includedFiles = new HashMap<>();
    private Map<String, ThriftNamespace> namespaces = new HashMap<String, ThriftNamespace>();
    private List<NamedItem> items = new ArrayList<NamedItem>();
    private Set<String> allNames = new HashSet<String>();

    public ThriftFile(String fileName) {
        this.fileName = fileName;
    }

    public void add(ThriftNamespace namespace) {
        namespaces.put(namespace.lang, namespace);
    }

    public void add(NamedItem item) {
        if (allNames.contains(item.name)) {
            throw new RuntimeException(String.format("duplicated name %s in file", item.name));
        }
        allNames.add(item.name);
        items.add(item);
    }

    public void add(ThriftFile includedFile) {
        includedFiles.put(includedFile.fileName, includedFile);
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public Map<String, ThriftNamespace> getNamespaces() {
        return namespaces;
    }

    public Map<String, ThriftFile> getIncludedFiles() {
        return includedFiles;
    }

    public List<NamedItem> getItems() {
        return items;
    }

    public Set<String> getAllNames() {
        return allNames;
    }

    @Override
    public String toString() {
        return "ThriftFile{" + "namespaces=" + namespaces + ", items=" + items + '}';
    }
}
