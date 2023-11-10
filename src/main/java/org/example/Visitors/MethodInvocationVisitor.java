package org.example.Visitors;

import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

public class MethodInvocationVisitor extends AbstractVisitor {
    List<MethodInvocation> methodInvocations = new ArrayList<>();

    @Override
    public boolean visit(MethodInvocation node) {
        methodInvocations.add(node);
        return super.visit(node);
    }

    public List<MethodInvocation> getMethodInvocations() {
        return methodInvocations;
    }


}
