package org.example;

import soot.*;
import soot.options.Options;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;

import java.util.*;

public class CFGGenerator {
    public static void main(String[] args) {
        // Step 1: Configure Soot
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\DELL\\IdeaProjects\\untitled\\target\\classes"));
        String sootClassPath = "C:\\Program Files (x86)\\Java\\jre1.8.0_431\\lib\\rt.jar;" +
                "C:\\Users\\DELL\\IdeaProjects\\untitled\\target\\classes;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\soot-oss\\soot\\4.2.0\\soot-4.2.0.jar;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\slf4j\\slf4j-simple\\1.7.36\\slf4j-simple-1.7.36.jar;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.36\\slf4j-api-1.7.36.jar";
        Options.v().set_soot_classpath(sootClassPath);

        Options.v().set_output_format(Options.output_format_jimple);

        // Step 2: Load the Main Class
        SootClass sc = Scene.v().loadClassAndSupport("org.example.TestProgram");
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod sm = sc.getMethodByName("main");
        Body b = sm.retrieveActiveBody();
        Body body = sm.retrieveActiveBody();
        UnitGraph graph = new ExceptionalUnitGraph(body);

        // Print the CFG
        System.out.println("Control Flow Graph:");
        for (Unit unit : graph) {
            System.out.println("Node: " + unit);
            List<Unit> successors = graph.getSuccsOf(unit);
            for (Unit succ : successors) {
                System.out.println("  -> Successor: " + succ);
            }
        }
        System.out.println();
        // Step 3: Generate the CFG
        UnitGraph cfg = new BriefUnitGraph(b);

        // Step 4: Perform Live Variable Analysis
        LiveVariableAnalysis lva = new LiveVariableAnalysis(cfg);

        // Step 5: Print the Analysis Results
        System.out.println("Control Flow Graph with Live Variables:");
        for (Unit u : cfg) {
            System.out.println("Statement: " + u);
            System.out.println("Live-In: " + lva.getLiveIn(u));
            System.out.println("Live-Out: " + lva.getLiveOut(u));
            System.out.println();
        }
    }
}

// Live Variable Analysis Implementation
class LiveVariableAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<String>> {
    private final Map<Unit, Set<String>> use = new HashMap<>();
    private final Map<Unit, Set<String>> def = new HashMap<>();

    public LiveVariableAnalysis(UnitGraph graph) {
        super(graph);
        initializeUseAndDef(graph);
        doAnalysis();
    }

    private void initializeUseAndDef(UnitGraph graph) {
        for (Unit u : graph) {
            Set<String> useSet = new HashSet<>();
            Set<String> defSet = new HashSet<>();

            for (ValueBox box : u.getUseBoxes()) {
                if (box.getValue() instanceof Local) {
                    useSet.add(box.getValue().toString());
                }
            }

            for (ValueBox box : u.getDefBoxes()) {
                if (box.getValue() instanceof Local) {
                    defSet.add(box.getValue().toString());
                }
            }

            use.put(u, useSet);
            def.put(u, defSet);

            // Debugging output
            System.out.println("Statement: " + u);
            System.out.println("Use: " + useSet);
            System.out.println("Def: " + defSet);
            System.out.println();
        }
    }

    @Override
    protected void flowThrough(FlowSet<String> in, Unit unit, FlowSet<String> out) {
        // Copy Live-Out to temporary set
        FlowSet<String> temp = out.clone();

        // Remove variables defined by the current unit
        for (String defVar : def.get(unit)) {
            temp.remove(defVar);
        }

        // Add variables used by the current unit
        for (String useVar : use.get(unit)) {
            temp.add(useVar);
        }

        // Assign the result to Live-In
        in.clear();
        in.union(temp);
    }


    @Override
    protected FlowSet<String> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<String> entryInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected void merge(FlowSet<String> in1, FlowSet<String> in2, FlowSet<String> out) {
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet<String> source, FlowSet<String> dest) {
        source.copy(dest);
    }

    public Set<String> getLiveIn(Unit unit) {
        return new HashSet<>(getFlowBefore(unit).toList());
    }

    public Set<String> getLiveOut(Unit unit) {
        return new HashSet<>(getFlowAfter(unit).toList());
    }
}

