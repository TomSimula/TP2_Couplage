package org.example.spoon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

public class ClassProcessor extends AbstractProcessor<CtClass<?>>{


    private Map<String, Map<String, Integer>> couplage;

    public ClassProcessor() {
        this.couplage = new HashMap<>();
    }


    @Override
    public void process(CtClass<?> element) {
        List<CtInvocation<?>> methodInvocations = new ArrayList<>();
        String[] packageSplit = element.getPackage().getQualifiedName().split("\\.");
        InvocationsFilter filter = new InvocationsFilter(CtInvocation.class, packageSplit[0] + "." + packageSplit[1], element);
        List<CtMethod<?>> methods = element.getElements(new TypeFilter(CtMethod.class));
        for(CtMethod<?> m: methods) {
            List<CtInvocation> invs = m.getElements(filter);
            for(CtInvocation inv: invs) {
                methodInvocations.add(inv);
            }
        }

        this.processInvocations(element, methodInvocations);

    }

    public Map<String, Map<String, Integer>> getCouplage() {
        return couplage;
    }

    private void processInvocations(CtClass element, List<CtInvocation<?>> invocations) {
        Map <String, Integer> calls = new HashMap<>();
        couplage.put(element.getSimpleName(), calls);
        for(CtInvocation<?> inv: invocations) {
            String targetName = inv.getTarget().getType().getSimpleName();
            if(calls.containsKey(targetName)) {
                calls.put(targetName, calls.get(targetName) + 1);
            } else {
                calls.put(targetName, 1);
            }
        }
    }
}
