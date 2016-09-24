package org.xudifsd.ast;

import org.xudifsd.lexer.Lexer;
import org.xudifsd.lexer.SyntaxException;
import org.xudifsd.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

public class ThriftEnum extends NamedItem {
    private Map<String, Integer> enums = new HashMap<String, Integer>();

    public ThriftEnum(String name) {
        super(name);
    }

    public void addEnumItem(Lexer lexer, String key, int value) throws SyntaxException {
        if (enums.containsKey(key)) {
            lexer.throwSyntaxError(key + " already exist in enum " + name + " with value " + enums.get(key));
        }
        enums.put(key, value);
    }

    public Map<String, Integer> getEnums() {
        return enums;
    }

    @Override
    public String toString() {
        return "ThriftEnum[" + "name=" + name + ", enums=" + enums + ']';
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
