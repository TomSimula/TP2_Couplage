package org.example.process;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class CallGraph {

    private Map<String ,Map<String, Double>> classNodes;
    private double totalCouplage;
    private Set<String> classPair;

    public CallGraph() {
        classNodes = new HashMap<>();
        classPair = new HashSet<>();
        totalCouplage = 0;
    }


    public void putClassNode(String s, Map<String, Double> m){
        m.remove(s);
        classNodes.put(s, m);
        if (!m.isEmpty())
            classPair.add(s);
        for (Map.Entry<String, Double> entry: m.entrySet()) {
            totalCouplage += entry.getValue();
            classPair.add(entry.getKey());
        }
    }

    /**Retourne le couplage entre deux classe*/
    public double calcCouplage(String c1, String c2){
        int couplage =0;
        //Add nb appelle method c1 from c2
        if (classNodes.containsKey(c1))
            if (classNodes.get(c1).containsKey(c2))
                couplage += classNodes.get(c1).get(c2);

        //Add nb appelle method c2 from c1
        if (classNodes.containsKey(c2))
            if (classNodes.get(c2).containsKey(c1))
                couplage += classNodes.get(c2).get(c1);

        return couplage/totalCouplage;
    }

    /**Crée un svg du graphe pondéré a partir de l'attribut classNode*/
    public void exportGraph(String name) throws IOException {
        Graph<String, DefaultWeightedEdge> graph = getPonderatetWeightedEdgeGraph();

        //Création du .dot
        DOTExporter<String, DefaultWeightedEdge> exporter = new DOTExporter<>();
        exporter.setVertexAttributeProvider(v -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v));
            return map;
        });
        exporter.setEdgeAttributeProvider(v -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(graph.getEdgeWeight(v)));
            return map;
        });


        StringWriter writer = new StringWriter();
        exporter.exportGraph(graph, writer);

        String dotCode = writer.toString();

        //Creation du svg a partir du .dot
        File outputSVG = new File("Graph/" + name + ".svg");
        Graphviz.fromString(dotCode).render(Format.SVG).toFile(outputSVG);

    }

    /**Retourne le graph pondéré en fonction de l'attribut classNode*/
    private Graph<String, DefaultWeightedEdge> getPonderatetWeightedEdgeGraph() {
        Graph<String, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (Map.Entry<String, Map<String, Double>> entry : classNodes.entrySet()) {
            if (!graph.containsVertex(entry.getKey()))
                graph.addVertex(entry.getKey());
            for (Map.Entry<String,Double> entryBis : entry.getValue().entrySet()) {
                if (!graph.containsVertex(entryBis.getKey()))
                    graph.addVertex(entryBis.getKey());
                DefaultWeightedEdge edge = graph.addEdge(entry.getKey(), entryBis.getKey());
                if (edge != null)
                    graph.setEdgeWeight(edge, calcCouplage(entry.getKey(), entryBis.getKey()));
            }
        }
        return graph;
    }

    /**Algorithme de regroupement (clustering) hiérarchique des classes d’une application*/
    public void clustering(boolean debugStep){
        //Map des couples avec leur couplage
        Map<String, Double> couples = createMapCouplages();

        //Liste de chaque module initialiser avec les classes qui ont des relations
        int step = 1;
        List<Module> modules = new ArrayList<>();
        for (String s: classPair) {
            modules.add(new Module("<" + s + ">"));
        }

        while (couples.size() > 0){
            String newCluster = findMaxCluster(couples);
            couples = majCouples(couples, newCluster);

            majModulesTab(modules, newCluster);

            if (debugStep){
                System.out.println("Step " + step + ":" + newCluster);
                step++;
            }
        }

        int count = 1;
        System.out.println("\nSolution :");
        for (Module module : modules) {
            System.out.println("\tModule" + count + " :\n" + module);
            count++;
        }
    }

    /**Algorithme d’indentification des groupes de classes couplées*/
    public void clusteringM2CP(boolean debugStep, double cp){
        Map<String, Double> couples = createMapCouplages();

        //Liste de chaque module initialiser avec les classes qui ont des relations
        int step = 1;
        List<Module> modules = new ArrayList<>();
        for (String s: classPair) {
            modules.add(new Module("<" + s + ">"));
        }

        while ((step <= Math.ceilDiv(classPair.size(), 2) || !checkAvgModule(modules, cp)) && couples.size() > 0){
            String newCluster = findMaxCluster(couples);
            couples = majCouples(couples, newCluster);

            majModulesTab(modules, newCluster);

            if (debugStep){
                System.out.println("Step " + step + ":" + newCluster);
            }
            step++;
        }

        if (!checkAvgModule(modules, cp))
            System.out.println("Impossible to resolve, try with a smaller CP");
        else {
            int count = 1;
            System.out.println("\nSolution :");
            for (Module module : modules) {
                System.out.println("\tModule" + count + " :\n" + module);
                count++;
            }
        }
    }

    /**Met a jour la tables des modules donné en parametre avec le nouveau cluster donné en parametre*/
    private void majModulesTab(List<Module> modules, String newCluster){
        Module module = new Module("<" + newCluster.replace("|", "-") + ">");

        String cluster[] = newCluster.split("[|]");
        int remove = 0;

        for (int i = 0; remove < 2; i++) {
            Module currentModule = modules.get(i);
            if (currentModule.getName().equals(cluster[0])){
                module.setLeftModule(currentModule);
                modules.remove(i);
                i--;
                remove++;
            } else if (currentModule.getName().equals(cluster[1])){
                module.setRightModule(currentModule);
                modules.remove(i);
                i--;
                remove++;
            }
        }

        modules.add(module);
    }

    /**Retourne un boolean vrai si pour tous les modules donné en parametre que la moyenne du couplage de tous les couples(couples avec couplage>0) de classe est supérieur à CP*/
    private boolean checkAvgModule(List<Module> modules, double cp){
        for (Module module: modules) {
            String moduleClass[] = module.getName().replace("<","").replace(">","").split("-");
            int nbCouplage = 0;
            double avgCouplage = 0;

            if (moduleClass.length <= 1)
                return false;

            for (int i = 0; i < moduleClass.length; i++) {
                for (int j = i+1; j < moduleClass.length; j++) {
                    double currentCouplage = calcCouplage(moduleClass[i], moduleClass[j]);
                    if (currentCouplage > 0){
                        avgCouplage += currentCouplage;
                        nbCouplage++;
                    }
                }
            }
            if (avgCouplage/nbCouplage < cp)
                return false;
        }
        return true;
    }

    /**Retourne la Map des couples avec leur couplage sans prendre en compte les classes sans relations*/
    private Map<String, Double> createMapCouplages(){
        Map<String, Double> couplages = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> entry: classNodes.entrySet()) {
            for (Map.Entry<String,Double> entryBis : entry.getValue().entrySet()){
                if (!couplages.containsKey("<" + entryBis.getKey() +  ">|<" + entry.getKey() + ">"))
                    couplages.put("<" + entry.getKey() +  ">|<" + entryBis.getKey() + ">", calcCouplage(entry.getKey(), entryBis.getKey()));
            }
        }
        return couplages;
    }

    /**Retourne le couple avec le plus grand couplage*/
    private String findMaxCluster(Map<String, Double> map){
        double maxValue = 0;
        String res = "";
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                res = entry.getKey();
            }
        }
        return res;
    }

    /**Retourne la mise a jour des couples de la map donné en paramètre en fonction du nouveau cluster donné en parametre*/
    private Map<String, Double> majCouples(Map<String, Double> map, String cluster){
        Map<String, Double> couplages = new HashMap<>();

        map.remove(cluster);
        String modules[] = cluster.split("[|]");

        for (Map.Entry<String, Double> entry : map.entrySet()){
            if (entry.getKey().contains(modules[0]) || entry.getKey().contains(modules[1])) {
                String newKey = entry.getKey().replaceAll("^"+modules[0]+"[|]|[|]"+modules[0]+"$|^"+modules[1]+"[|]|[|]"+modules[1]+"$", "");
                String modifCluster = "<" + cluster + ">";
                newKey += "|" + modifCluster.replaceAll("[|]", "-");
                if (couplages.containsKey(newKey))
                    couplages.replace(newKey, couplages.get(newKey)+entry.getValue());
                else
                    couplages.put(newKey, entry.getValue());
            } else {
                couplages.put(entry.getKey(), entry.getValue());
            }
        }

        return couplages;
    }
    public String toString() {
        String res = "";
        for (Map.Entry<String, Map<String, Double>> m1: classNodes.entrySet()) {
            res += m1.getKey() + "\n";
            for (Map.Entry<String,Double> m2: m1.getValue().entrySet()) {
                res += "\t " + m2.toString() + "\n";
            }
            res += "\n";
        }
        return res;
    }
}
