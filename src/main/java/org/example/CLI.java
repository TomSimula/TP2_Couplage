package org.example;

import org.example.Config.Config;
import org.example.common.Analyzer;
import org.example.jdt.JdtAnalyzer;
import org.example.common.CallGraph;
import org.example.spoon.SpoonAnalyzer;

import java.io.IOException;
import java.util.Scanner;

public class CLI {

    private Analyzer anal;
    private final Scanner scanner = new Scanner(System.in);

    private final String projectPathChoice = "Entrez le chemin du projet à analyser (null pour analyser ce projet): ";
    private final String analyzerChoice = "Analiser ce projet avec:\n" +
                                          "1. Spoon\n" +
                                          "2. JDT\n";
    private final String options = "0. Quitter.\n" +
                                   "1. Créer le graphe de couplage.\n" +
                                   "2. Créer un endogramme des classes.\n" +
                                   "3. Créer un endrogramme (entrez une valeur de couplage minimale entre les modules).\n";
    public void run() throws IOException {
        int option = -1;
        setProjectPath();
        setAnalyzer();
        System.out.println("Analyse du projet en cours...");
        CallGraph cg = anal.buildCallGraph();
        while(option != 0){
            System.out.println(options);
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Enregistrer le graphe ponderé en format svg? y/n");
                    String choice = scanner.next();
                    if(choice.startsWith("y")) {
                        System.out.println("Nommer le graphe:");
                        choice = scanner.next();
                        cg.exportGraph(choice);
                        System.out.println("Graphe enregistré à Graph" + choice + ".svg");
                    }
                    break;
                case 2:
                    System.out.println("Afficher les étapes de clustering? y/n");
                    choice = scanner.next();
                    cg.clustering(choice.startsWith("y"));
                    break;
                case 3:
                    System.out.println("Entrez une valeur de couplage (CP) minimale entre les modules. (valeur entre 0 et 1)");
                    double minCouplage = scanner.nextDouble();
                    while(minCouplage < 0 || minCouplage > 1) {
                        System.out.println("Valeur de couplage invalide");
                        minCouplage = scanner.nextDouble();
                    }
                    System.out.println("Afficher les étapes de clustering? y/n");
                    choice = scanner.next();
                    cg.clusteringM2CP(choice.startsWith("y"), minCouplage);
                    break;
            }

        }

    }

    private void setProjectPath() {
        System.out.println(projectPathChoice);
        String path = scanner.nextLine();
        if(!path.isEmpty())
            Config.projectSourcePath = path;

    }

    private void setAnalyzer() {
        System.out.println(analyzerChoice);
        int option = scanner.nextInt();
        if(option == 1)
            anal = new SpoonAnalyzer(Config.projectSourcePath);
        else
            anal = new JdtAnalyzer(Config.projectSourcePath);
    }
}
