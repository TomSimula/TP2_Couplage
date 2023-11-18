package org.example.spoon;
import org.example.Visitors.AbstractVisitor;
import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.processing.Processor;
import spoon.reflect.CtModel;

public class SpoonParser {

    private Launcher launcher;

    public SpoonParser(String path) {
        this.launcher = new Launcher();
        this.launcher.addInputResource(path);
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    public CtModel getModel() {
        return launcher.getModel();
    }

    public void setProcessor(AbstractProcessor<?> processor) {
        launcher.addProcessor(processor);
    }

    public void build() {
        launcher.buildModel();
    }

}
