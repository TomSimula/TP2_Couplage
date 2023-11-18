package org.example.common;

import org.example.process.CallGraph;

public interface Analyzer {
    public CallGraph buildCallGraph();
}
