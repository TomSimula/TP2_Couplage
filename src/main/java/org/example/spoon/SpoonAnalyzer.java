package org.example.spoon;

import org.example.common.Analyzer;
import org.example.common.CallGraph;

public class SpoonAnalyzer implements Analyzer {
    private SpoonParser parser;
    private ClassProcessor processor;

    public SpoonAnalyzer(String projectPath){
        parser = new SpoonParser(projectPath);
        processor = new ClassProcessor();
        configureParser();

    }

    private void configureParser(){
        parser.setProcessor(processor);
        parser.build();
    }
    @Override
    public CallGraph buildCallGraph() {
        parser.getModel().processWith(processor);
        return new CallGraph(processor.getCouplage());
    }
}
