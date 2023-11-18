package org.example.spoon;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

public class InvocationsFilter extends TypeFilter<CtInvocation> {

    private String projectRoot;
    private CtType source;

    public InvocationsFilter(Class<? super CtInvocation> type, String projectRoot, CtClass source) {
		super(type);
        this.projectRoot = projectRoot;
        this.source = source;
    }

	@Override
    public boolean matches(CtInvocation element) {
        // filter calls where the target belongs to the project
        // and where the target is different from the class where the call happens
        boolean val =
            element.getTarget() != null
            && element.getTarget().getType() != null
            && !element.getTarget().getType().getQualifiedName().equals(source.getQualifiedName())
            && element.getTarget().getType().getPackage() != null
            && element.getTarget().getType().getPackage().getQualifiedName().startsWith(projectRoot);
        return val;
    }

}
