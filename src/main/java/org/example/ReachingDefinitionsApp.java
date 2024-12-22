package org.example;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.jimple.*;

import java.util.*;

public class ReachingDefinitionsApp {
    public static void main(String[] args) {
        // Step 1: Configure Soot
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\DELL\\IdeaProjects\\untitled\\target\\classes"));
        Options.v().set_output_format(Options.output_format_jimple);

        String sootClassPath = "C:\\Program Files (x86)\\Java\\jre1.8.0_431\\lib\\rt.jar;" +
                "C:\\Users\\DELL\\IdeaProjects\\untitled\\target\\classes;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\soot-oss\\soot\\4.2.0\\soot-4.2.0.jar;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\slf4j\\slf4j-simple\\1.7.36\\slf4j-simple-1.7.36.jar;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.36\\slf4j-api-1.7.36.jar";
        Options.v().set_soot_classpath(sootClassPath);

        // Step 2: Load and Analyze the Main Class
        SootClass sc = Scene.v().loadClassAndSupport("org.example.TestProgram4");
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod sm = sc.getMethodByName("main");
        Body body = sm.retrieveActiveBody();

        // Step 3: Build Control Flow Graph
        ExceptionalUnitGraph cfg = new ExceptionalUnitGraph(body);

        // Step 4: Perform Reaching Definitions Analysis
        ReachingDefinitions analysis = new ReachingDefinitions(cfg);

        // Step 5: Print Reaching Definitions Results
        analysis.printResults();
    }
}

class ReachingDefinitions extends ForwardFlowAnalysis<Unit, Set<String>> {
    private final Map<Unit, Set<String>> genSet = new HashMap<>();
    private final Map<Unit, Set<String>> killSet = new HashMap<>();

    public ReachingDefinitions(UnitGraph cfg) {
        super(cfg);
        initializeGenAndKillSets(cfg);
        doAnalysis();
    }

    private void initializeGenAndKillSets(UnitGraph cfg) {
        for (Unit unit : cfg) {
            Set<String> gen = new HashSet<>();
            Set<String> kill = new HashSet<>();

            if (unit instanceof AssignStmt) {
                AssignStmt stmt = (AssignStmt) unit;
                Value left = stmt.getLeftOp();

                if (left instanceof Local) {
                    String var = left.toString();
                    gen.add(var);

                    // Kill all previous definitions of the same variable
                    for (Unit other : cfg) {
                        if (other instanceof AssignStmt) {
                            AssignStmt otherStmt = (AssignStmt) other;
                            Value otherLeft = otherStmt.getLeftOp();
                            if (otherLeft instanceof Local && otherLeft.toString().equals(var)) {
                                kill.add(var);
                            }
                        }
                    }
                }
            }
            genSet.put(unit, gen);
            killSet.put(unit, kill);
        }
    }

    @Override
    protected void flowThrough(Set<String> in, Unit unit, Set<String> out) {
        // Out = Gen ∪ (In - Kill)
        Set<String> gen = genSet.getOrDefault(unit, new HashSet<>());
        Set<String> kill = killSet.getOrDefault(unit, new HashSet<>());

        out.clear();
        out.addAll(in);
        out.removeAll(kill);
        out.addAll(gen);
    }

    @Override
    protected Set<String> newInitialFlow() {
        return new HashSet<>(); // Start with an empty set of definitions
    }

    @Override
    protected Set<String> entryInitialFlow() {
        return new HashSet<>(); // Entry node starts with an empty set
    }

    @Override
    protected void merge(Set<String> in1, Set<String> in2, Set<String> out) {
        out.clear();
        out.addAll(in1);
        out.addAll(in2); // Merge by union
    }

    @Override
    protected void copy(Set<String> source, Set<String> dest) {
        dest.clear();
        dest.addAll(source);
    }

    public void printResults() {
        System.out.println("=== Reaching Definitions Analysis ===");
        for (Unit unit : graph) {
            System.out.println("Statement: " + unit);
            System.out.println("Reaching Definitions In: " + getFlowBefore(unit));
            System.out.println("Reaching Definitions Out: " + getFlowAfter(unit));
            System.out.println();
        }
    }
}
