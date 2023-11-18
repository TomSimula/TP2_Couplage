package org.example.spoon;
import spoon.Launcher;
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

    public void setParser(Processor<?> processor) {
        launcher.addProcessor(processor);
    }

    public void run() {
        launcher.run();
    }

    public void build() {
        launcher.buildModel();
    }

}
