package org.xudifsd.ast;

import org.xudifsd.ast.type.ThriftNamespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThriftFile implements Acceptable {
    private Map<String, ThriftNamespace> namespaces = new HashMap<String, ThriftNamespace>();
    private List<NamedItem> items = new ArrayList<NamedItem>();
    private Set<String> allNames = new HashSet<String>();

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

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    public List<NamedItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ThriftFile{" + "namespaces=" + namespaces + ", items=" + items + '}';
    }
}
