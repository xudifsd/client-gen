package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftNamespace;
import org.xudifsd.visitor.Visitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThriftFile implements Acceptable {
    public final String fileName; // do not contains dir and .thrift, used for default scope
    private Map<String, ThriftFile> includedFiles = new HashMap<String, ThriftFile>();
    private Map<String, ThriftNamespace> namespaces = new HashMap<String, ThriftNamespace>();
    private Map<String, NamedItem> items = new HashMap<String, NamedItem>();

    public ThriftFile(String fileName) {
        this.fileName = fileName;
    }

    public void add(ThriftNamespace namespace) {
        namespaces.put(namespace.lang, namespace);
    }

    public void add(NamedItem item) {
        if (items.get(item.name) != null) {
            throw new RuntimeException(String.format("duplicated name %s in file", item.name));
        }
        items.put(item.name, item);
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

    public Map<String, NamedItem> getItems() {
        return items;
    }

    public Set<String> getAllNames() {
        return items.keySet();
    }

    @Override
    public String toString() {
        return "ThriftFile{" + "namespaces=" + namespaces + ", items=" + items + '}';
    }
}
