package org.xudifsd.control;

import org.xudifsd.util.Arglist;

import java.util.LinkedList;

public class CommandLine {
    interface F<X> {
        void f(X x);
    }

    enum Kind {
        Empty, Bool, Int, String, StringList,
    }

    static class Arg<X> {
        String name;
        String option;
        String description;
        Kind kind;
        F<X> action;

        public Arg(String name, String option, String description, Kind kind,
                   F<X> action) {
            this.name = name;
            this.option = option;
            this.description = description;
            this.kind = kind;
            this.action = action;
        }

    }

    private LinkedList<Arg<Object>> args;

    @SuppressWarnings("unchecked")
    public CommandLine() {
        this.args = new Arglist<Arg<Object>>().addAll(new Arg<Object>("debug", null,
                "show debug msg", Kind.Empty,
                new F<Object>() {
                    @Override
                    public void f(Object s) {
                        Control.debug = true;
                    }
                }), new Arg<Object>("output", "<outfile>",
                "set the name of the output file", Kind.String,
                new F<Object>() {
                    @Override
                    public void f(Object s) {
                        Control.outputName = (String) s;
                    }
                }), new Arg<Object>("verbose", "{0|1|2}", "how verbose to be",
                Kind.Int, new F<Object>() {
            @Override
            public void f(Object n) {
                int i = (Integer) n;
                switch (i) {
                    case 0:
                        Control.verbose = Control.Verbose_t.Silent;
                        break;
                    case 1:
                        Control.verbose = Control.Verbose_t.Pass;
                        break;
                    default:
                        Control.verbose = Control.Verbose_t.Detailed;
                        break;
                }
            }
        }));
    }

    // scan the command line arguments, return the file name
    // in it. The file name should be unique.
    public String scan(String[] args) {
        String filename = null;

        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                if (filename == null) {
                    filename = args[i];
                    continue;
                } else {
                    System.out.println("Error: can only compile one Thrift file a time");
                    System.exit(1);
                }
            } else
                ;

            boolean found = false;
            for (Arg<Object> arg : this.args) {
                if (!arg.name.equals(args[i].substring(1)))
                    continue;

                found = true;
                String theArg = null;
                switch (arg.kind) {
                    case Empty:
                        arg.action.f(null);
                        break;
                    default:
                        if (i >= args.length - 1) {
                            System.out.println(arg.name + ": requires an argument");
                            this.output();
                            System.exit(1);
                        }
                        theArg = args[++i];
                        break;
                }
                switch (arg.kind) {
                    case Bool:
                        if (theArg.equals("true"))
                            arg.action.f(new Boolean(true));
                        else if (theArg.equals("false"))
                            arg.action.f(new Boolean(false));
                        else {
                            System.out.println(arg.name + ": requires a boolean");
                            this.output();
                            System.exit(1);
                        }
                        break;
                    case Int:
                        int num = 0;
                        try {
                            num = Integer.parseInt(theArg);
                        } catch (java.lang.NumberFormatException e) {
                            System.out.println(arg.name + ": requires an integer");
                            this.output();
                            System.exit(1);
                        }
                        arg.action.f(num);
                        break;
                    case String:
                        arg.action.f(theArg);
                        break;
                    case StringList:
                        String[] strArray = theArg.split(",");
                        arg.action.f(strArray);
                        break;
                    default:
                        break;
                }
                break;
            }
            if (!found) {
                System.out.println("undefined switch: " + args[i]);
                this.output();
                System.exit(1);
            }
        }
        return filename;
    }

    private void outputSpace(int n) {
        if (n < 0)
            throw new RuntimeException("bug");

        while (n-- != 0)
            System.out.print(" ");
    }

    public void output() {
        int max = 0;
        for (Arg<Object> a : this.args) {
            int current = a.name.length();
            if (a.option != null)
                current += a.option.length();
            if (current > max)
                max = current;
        }
        System.out.println("Available options:");
        for (Arg<Object> a : this.args) {
            int current = a.name.length();
            System.out.print("   -" + a.name + " ");
            if (a.option != null) {
                current += a.option.length();
                System.out.print(a.option);
            }
            outputSpace(max - current + 1);
            System.out.println(a.description);
        }
    }

    public void usage() {
        System.out.println("The ClientGen compiler. Copyright (C) 2016-, xudifsd.\n"
                        + "Usage: java ClientGen [options] <filename>\n");
        output();
    }
}
