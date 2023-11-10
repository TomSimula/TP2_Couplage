package org.example;

import org.example.process.Analyzer;
import org.example.process.CallGraph;

import java.io.IOException;
import java.util.Scanner;

public class CLI {

    private final Analyzer anal = new Analyzer();
    private final Scanner scanner = new Scanner(System.in);
    private final String options = "0. Quitter.\n" +
                                   "1. Créer le graphe de couplage.\n" +
                                   "2. Créer un endogramme des classes.\n" +
                                   "3. Créer un endrogramme (entrez une valeur de couplage minimale entre les modules).\n";
    public void run() throws IOException {
        int option = -1;
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
}
