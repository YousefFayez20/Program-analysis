package org.example;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.options.Options;

import java.util.*;

public class PointerAnalysisApp {
    public static void main(String[] args) {
        // Step 1: Configure Soot
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

        // Step 2: Load the Main Class
        SootClass sc = Scene.v().loadClassAndSupport("org.example.TestProgram3");
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod sm = sc.getMethodByName("main");
        Body body = sm.retrieveActiveBody();

        // Step 3: Run Pointer Analysis (Steensgaard's Algorithm)
        PointerAnalysis pointerAnalysis = new PointerAnalysis();
        pointerAnalysis.analyze(body);

        // Step 4: Print Pointer Analysis Results
        pointerAnalysis.printResults();
    }
}

class PointerAnalysis {
    private final Map<String, String> parent = new HashMap<>();

    // Find operation with path compression
    private String find(String var) {
        if (!parent.containsKey(var)) {
            parent.put(var, var); // Initialize if not found
        }
        if (!parent.get(var).equals(var)) {
            parent.put(var, find(parent.get(var))); // Path compression
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
                Value left = stmt.getLeftOp();
                Value right = stmt.getRightOp();

                if (left instanceof Local && right instanceof Local) {
                    // Unify variables
                    union(left.toString(), right.toString());
                } else if (left instanceof Local && right instanceof FieldRef) {
                    // Handle field references
                    union(left.toString(), right.toString());
                }
                // Add more rules as needed
            }
        }
    }

    public void printResults() {
        System.out.println("=== Pointer Analysis Results ===");
        Map<String, Set<String>> equivalenceClasses = new HashMap<>();
        for (String var : parent.keySet()) {
            String root = find(var);
            equivalenceClasses.computeIfAbsent(root, k -> new HashSet<>()).add(var);
        }
        int aliasClassCount = 1;
        for (Map.Entry<String, Set<String>> entry : equivalenceClasses.entrySet()) {
            System.out.println("Alias Class " + aliasClassCount + ":");
            for (String alias : entry.getValue()) {
                System.out.println("  - " + alias);
            }
            aliasClassCount++;
            System.out.println(); // Add spacing between alias classes
        }
    }

}
