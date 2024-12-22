package org.example;

import soot.*;
import soot.jimple.AssignStmt;
import soot.options.Options;

import java.util.*;

public class PointerAnalysisApp {
    public static void main(String[] args) {
        // Configure Soot
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\DELL\\IdeaProjects\\untitled\\target\\classes"));

        // Update the Soot classpath to include dependencies
        String sootClassPath = "C:\\Program Files (x86)\\Java\\jre1.8.0_431\\lib\\rt.jar;" +
                "C:\\Users\\DELL\\IdeaProjects\\untitled\\target\\classes;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\soot-oss\\soot\\4.2.0\\soot-4.2.0.jar;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\slf4j\\slf4j-simple\\1.7.36\\slf4j-simple-1.7.36.jar;" +
                "C:\\Users\\DELL\\.m2\\repository\\org\\slf4j\\slf4j-api\\1.7.36\\slf4j-api-1.7.36.jar";
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_output_format(Options.output_format_jimple);

        // Load Main Class
        SootClass sc = Scene.v().loadClassAndSupport("org.example.TestProgramWithNull");
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        // Analyze Main Method
        SootMethod sm = sc.getMethodByName("main");
        Body body = sm.retrieveActiveBody();

        // Run Pointer Analysis
        PointerAnalysis pointerAnalysis = new PointerAnalysis();
        pointerAnalysis.analyze(body);

        // Print Results
        pointerAnalysis.printResults();
    }
}

class PointerAnalysis {
    private final Map<String, String> parent = new HashMap<>();
    private final Set<String> nullPointers = new HashSet<>();
    private final List<String> dereferences = new ArrayList<>();

    // Find operation with path compression
    private String find(String var) {
        if (!parent.containsKey(var)) {
            parent.put(var, var);
        }
        if (!parent.get(var).equals(var)) {
            parent.put(var, find(parent.get(var)));
        }
        return parent.get(var);
    }

    // Union operation
    private void union(String var1, String var2) {
        String root1 = find(var1);
        String root2 = find(var2);
        if (!root1.equals(root2)) {
            parent.put(root1, root2);
        }
    }

    public void analyze(Body body) {
        for (Unit unit : body.getUnits()) {
            if (unit instanceof AssignStmt) {
                AssignStmt stmt = (AssignStmt) unit;
                String left = stmt.getLeftOp().toString();
                String right = stmt.getRightOp().toString();

                // Handle null assignments
                if ("null".equals(right)) {
                    nullPointers.add(left);
                } else {
                    nullPointers.remove(left); // Overwritten variables are no longer null
                }

                // Handle aliasing
                union(left, right);
            }

            // Detect potential dereferences
            String stmtStr = unit.toString();
            if (stmtStr.contains(".")) {
                String derefVar = stmtStr.split("\\.")[0].trim();
                dereferences.add(derefVar);
            }
        }
    }

    public void printResults() {
        System.out.println("=== Pointer Analysis Results ===");

        // Alias Classes
        Map<String, Set<String>> equivalenceClasses = new HashMap<>();
        for (String var : parent.keySet()) {
            String root = find(var);
            equivalenceClasses.computeIfAbsent(root, k -> new HashSet<>()).add(var);
        }

        int classId = 1;
        for (Map.Entry<String, Set<String>> entry : equivalenceClasses.entrySet()) {
            System.out.println("Alias Class " + classId + ":");
            for (String alias : entry.getValue()) {
                System.out.println("  - " + alias);
            }
            classId++;
            System.out.println();
        }

        // Null Pointer Dereference Warnings
        System.out.println("=== Null Pointer Dereferencing Warnings ===");
        for (String deref : dereferences) {
            if (nullPointers.contains(deref)) {
                System.out.println("Warning: Potential null pointer dereference at variable '" + deref + "'");
            }
        }
    }
}
