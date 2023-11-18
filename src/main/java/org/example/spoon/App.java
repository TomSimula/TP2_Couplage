package org.example.spoon;

public class App {
    static final String path = "/home/daniel/Documents/M2_git/TP2_Couplage";

    public static void main(String[] args) {
        ClassProcessor p = new ClassProcessor();
        SpoonParser parser = new SpoonParser(path);
        parser.setParser(p);
        parser.build();
        parser.getModel().processWith(p);
        System.out.println(p.getCouplage());
    }
}
