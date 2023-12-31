package org.example.jdt;


import org.example.Visitors.*;
import org.example.common.Analyzer;
import org.eclipse.jdt.core.dom.*;
import org.example.common.CallGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class JdtAnalyzer implements Analyzer {
    private JdtParser parser;
    private ArrayList<File> javaFiles;

    private ClassDeclarationVisitor visitor;


    public JdtAnalyzer(String projectPath) {
        visitor = new ClassDeclarationVisitor();
        parser = new JdtParser(projectPath);
        File folder = new File(projectPath);
        this.javaFiles = listJavaFilesForFolder(folder);
    }
    //Operations

    //Merge every call graph of each class
    public CallGraph buildCallGraph(){
        CallGraph graph = new CallGraph();

        try {
            visitProject();
            for(TypeDeclaration type: visitor.getClasses()) {
                graph.putClassNode(type.getName().toString(), buildClassCallGraph(type));
            }

        } catch (IOException e) {
            System.err.println("Invalid project path");
        }
        return graph;
    }

    //Create and return the call graph of a class
    private Map<String, Double> buildClassCallGraph(TypeDeclaration clazz){
        Map<String, Double> relation = new HashMap<>();
        for(MethodDeclaration method: clazz.getMethods()) {
            MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
            method.accept(methodInvocationVisitor);
            List<MethodInvocation> methodInvocations = methodInvocationVisitor.getMethodInvocations();
            for (MethodInvocation methodInvocation : methodInvocations) {
                Expression expression = methodInvocation.getExpression();
                ITypeBinding typeBinding;
                String calleeFullName = "";
                if (expression == null) {
                    // appel de méthode avec 'this' implicite
                    continue;
                } else {
                    typeBinding = expression.resolveTypeBinding();
                }
                if (typeBinding != null) {
                    // on ne s'interesse pas aux classes à l'exterieur du projet
                    if (!isTypeInProject(typeBinding.getName())) continue;
                    calleeFullName = typeBinding.getName();
                } else {
                    // suite d'appels de méthodes i.e. m1().m2().m3();
                    // le type de m1().m2() ne peut pas être déduit
                    if (!expression.toString().contains(".")) {
                        if (!isTypeInProject(expression.toString())) continue;
                        calleeFullName = expression.toString();
                    }

                }
                if(!Objects.equals(calleeFullName, "")) {
                    if (relation.containsKey(calleeFullName)) {
                        relation.put(calleeFullName, relation.get(calleeFullName) + 1);
                    } else {
                        relation.put(calleeFullName, 1.0);
                    }
                }
            }
        }
        return relation;

    }


    private boolean isTypeInProject(String name){
        return visitor.getclassByName(name) != null;
    }

    //Find and return all file in a directory
    private ArrayList<File> listJavaFilesForFolder(final File folder) {
        ArrayList<File> javaFiles = new ArrayList<File>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                javaFiles.addAll(listJavaFilesForFolder(fileEntry));
            } else if (fileEntry.getName().contains(".java")) {
                javaFiles.add(fileEntry);
            }
        }

        return javaFiles;
    }

    //Visit every file with a visitor
    private void visitProject() throws IOException {
        for (File fileEntry : javaFiles) {
            CompilationUnit parse = parser.parse(fileEntry);
            parse.accept(visitor);
        }
    }

}
