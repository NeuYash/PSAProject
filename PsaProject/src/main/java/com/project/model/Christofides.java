package com.project.model;

import com.project.utility.DeepCopyUtils;
import com.project.visualization.GraphOperation;

import java.util.*;

public class Christofides {

    public static List<GraphOperation> kruskalgos = new ArrayList<>();

    public static List<GraphOperation> calcGraphOperation(List<Node> nodes) {
        List<GraphOperation> gos = new ArrayList<>();
        for (int i = 0; i < nodes.size() - 1; i++) {
            gos.add(GraphOperation.addEdge(nodes.get(i), nodes.get(i + 1)));
        }
        gos.add(GraphOperation.addEdge(nodes.get(nodes.size() - 1), nodes.get(0)));

        return gos;
    }

    public static List<Edge> findMST(Graph graph) {
        List<Edge> kruskaledges = graph.kruskalMST();
        for (Edge e : kruskaledges) {
            kruskalgos.add(GraphOperation.addEdge(e));
        }
        kruskalgos.add(GraphOperation.addEdge(kruskaledges.get(kruskaledges.size() - 1).getDestination(),
                kruskaledges.get(0).getSource()));

        return kruskaledges;
    }

    public static List<Node> findOddDegreeVertices(Graph graph, List<Edge> mst) {
        Map<Node, Integer> degrees = new HashMap<>();
        for (Node node : graph.nodes) {
            degrees.put(node, 0);
        }
        for (Edge edge : mst) {
            degrees.put(edge.source, degrees.get(edge.source) + 1);
            degrees.put(edge.destination, degrees.get(edge.destination) + 1);
        }
        List<Node> oddDegreeNodes = new ArrayList<>();
        for (Map.Entry<Node, Integer> entry : degrees.entrySet()) {
            if (entry.getValue() % 2 != 0) {
                oddDegreeNodes.add(entry.getKey());
            }
        }
        return oddDegreeNodes;
    }

    public static List<Edge> getMinimumWeightPerfectMatching(List<Node> oddDegreeNodes) {
        Graph subgraph = new Graph(oddDegreeNodes);
        subgraph.connectAllNodes();
        return subgraph.getMinimumWeightPerfectMatching();
    }

    private static void dfs(Node node, List<Node> eulerTour, List<Edge> edges, Set<Node> visited,
                            Map<Node, List<Node>> adjacenyMatrix) {

        visited.add(node);
        for (Node v : adjacenyMatrix.get(node)) {
            if (!visited.contains(v)) {
                eulerTour.add(node);
                eulerTour.add(v);

                Edge matchedEdge = edges.get(0);
                for (Edge edge : edges) {
                    if (edge.source.crimeId == node.crimeId && edge.destination.crimeId == v.crimeId) {
                        matchedEdge = edge;
                        break;
                    }
                }
                edges.remove(matchedEdge);
                dfs(v, eulerTour, edges, visited, adjacenyMatrix);
            }
        }
    }

    // Step 5: Find an Eulerian circuit in the Eulerian graph
    public static List<Node> eulerTour(Graph graph, List<Edge> mst, List<Edge> perfEdges) {
        List<Node> eulerTour = new ArrayList<>();
        List<Edge> combineEdges = new ArrayList<>(mst);
        combineEdges.addAll(perfEdges);
        Map<Node, List<Node>> adjacencyMatrix = graph.adjacencyMatrix();
        Set<Node> visited = new HashSet<>();
        Node startNode = combineEdges.get(0).source;
        dfs(startNode, eulerTour, combineEdges, visited, adjacencyMatrix);
        return eulerTour;
    }

    // Step 6: Remove duplicates to obtain the TSP path
    public static List<Node> generateTSPTour(List<Node> eulerTour) {
        List<Node> hamiltonList = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        for (Node node : eulerTour) {
            if (!visited.contains(node)) {
                visited.add(node);
                hamiltonList.add(node);
            }
        }
        return hamiltonList;
    }

    public static List<Node> randomSwapOptimise(List<Node> tspTour, Integer iterations) {
        List<Node> randomSwapTour = tspTour;
        double currMaxTourLength = calculateTourLength(randomSwapTour);
        for (int i = 0; i < iterations; i++) {
            int randomIndexOne = (int) (Math.random() * tspTour.size());
            int randomIndexTwo = (int) (Math.random() * tspTour.size());
            List<Node> swappedTour = DeepCopyUtils.deepCopy(randomSwapTour);
            swap(swappedTour, randomIndexOne, randomIndexTwo);
            double swappedTourLength = calculateTourLength(swappedTour);
            if (swappedTourLength < currMaxTourLength) {
                currMaxTourLength = swappedTourLength;
                randomSwapTour = DeepCopyUtils.deepCopy(swappedTour);
            }
        }
        return randomSwapTour;
    }

    public static List<Node> twoOpt(List<Node> nodes) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < nodes.size() - 1; i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    double distanceBefore = calculateTourLength(nodes);
                    List<Node> newNodes = twoOptSwap(nodes, i, j);
                    double distanceAfter = calculateTourLength(newNodes);
                    if (distanceAfter < distanceBefore) {
                        nodes = newNodes;
                        improved = true;
                    }
                }
            }
        }
        return nodes;
    }

    private static List<Node> twoOptSwap(List<Node> nodes, int i, int j) {
        List<Node> newNodes = new ArrayList<Node>();
        for (int k = 0; k < i; k++) {
            newNodes.add(nodes.get(k));
        }
        for (int k = j; k >= i; k--) {
            newNodes.add(nodes.get(k));
        }
        for (int k = j + 1; k < nodes.size(); k++) {
            newNodes.add(nodes.get(k));
        }
        return newNodes;
    }

    // 3 OPT
    public static List<Node> threeOpt(List<Node> tour) {
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            for (int i = 0; i < tour.size() - 3; i++) {
                for (int j = i + 2; j < tour.size() - 1; j++) {
                    for (int k = j + 2; k < tour.size(); k++) {
                        double distA = Graph.calculateDistance(tour.get(i), tour.get(i + 1))
                                + Graph.calculateDistance(tour.get(j), tour.get(j + 1))
                                + Graph.calculateDistance(tour.get(k), tour.get(k - 1));
                        double distB = Graph.calculateDistance(tour.get(i), tour.get(j))
                                + Graph.calculateDistance(tour.get(i + 1), tour.get(j + 1))
                                + Graph.calculateDistance(tour.get(k), tour.get(k - 1));
                        double distC = Graph.calculateDistance(tour.get(i), tour.get(j + 1))
                                + Graph.calculateDistance(tour.get(i + 1), tour.get(j))
                                + Graph.calculateDistance(tour.get(k), tour.get(k - 1));
                        double distD = Graph.calculateDistance(tour.get(i), tour.get(j + 1))
                                + Graph.calculateDistance(tour.get(i + 1), tour.get(k))
                                + Graph.calculateDistance(tour.get(j), tour.get(k - 1));
                        double distE = Graph.calculateDistance(tour.get(i), tour.get(k))
                                + Graph.calculateDistance(tour.get(j + 1), tour.get(i + 1))
                                + Graph.calculateDistance(tour.get(j), tour.get(k - 1));
                        if (distB < distA) {
                            reverse(tour, i + 1, j);
                            reverse(tour, j + 1, k);
                            improvement = true;
                        } else if (distC < distA) {
                            swap1(tour, i + 1, j);
                            swap1(tour, j + 1, k);
                            improvement = true;
                        } else if (distD < distA) {
                            swap1(tour, i + 1, k);
                            reverse(tour, j + 1, k);
                            improvement = true;
                        } else if (distE < distA) {
                            swap1(tour, i + 1, j);
                            swap1(tour, k, j + 1);
                            improvement = true;
                        }
                    }
                }
            }
        }
        return tour;
    }

    public static void reverse(List<Node> tour, int start, int end) {
        while (start < end) {
            Node tmp = tour.get(start);
            tour.set(start, tour.get(end));
            tour.set(end, tmp);
            start++;
            end--;
        }
    }

    public static void swap1(List<Node> tour, int i, int j) {
        Node tmp = tour.get(i);
        tour.set(i, tour.get(j));
        tour.set(j, tmp);
    }
    // 3OPT finish

    // 3 OPT Second Method
    public static List<Node> threeOptChristofides(List<Node> nodes) {
        int improvementCount = 0;
        do {
            improvementCount = 0;
            for (int i = 0; i < nodes.size() - 2; i++)
                for (int j = i + 1; j < nodes.size() - 1; j++)
                    for (int k = j + 1; k < nodes.size(); k++) {

                        List<Node> newNodes = Swap(nodes, i, j, k);
                        if (Christofides.calculateTourLength(newNodes) < Christofides.calculateTourLength(nodes)) {
                            nodes = newNodes;
                            improvementCount++;
                        }
                    }
        } while (improvementCount > 0);
        return nodes;
    }

    public static List<Node> Swap(List<Node> nodes, int i, int j, int k) {
        List<Node> newCities = new ArrayList<>();
        for (int x = 0; x <= i; x++)
            newCities.add(nodes.get(x));

        for (int x = j + 1; x <= k; x++)
            newCities.add(nodes.get(x));

        for (int x = i + 1; x <= j; x++)
            newCities.add(nodes.get(x));

        for (int x = k + 1; x < nodes.size(); x++)
            newCities.add(nodes.get(x));

        return newCities;
    }
    // 3 OPT second Method finish

    // K Opt tour
    public static List<Node> kOpt(List<Node> tour, int k) {
        int n = tour.size();
        List<Node> segment = new ArrayList<>(k + 1);
        List<Node> newTour = new ArrayList<>(n);
        List<Node> flipped = new ArrayList<>(k + 1);

        for (Node node : tour) {
            newTour.add(node);
        }

        for (int i = 0; i < n - k; i++) {
            for (int j = i + k; j < n; j++) {
                segment.clear();
                flipped.clear();
                for (int x = 0; x <= k; x++) {
                    segment.add(newTour.get((i + x) % n));
                }

                int count = 0;
                for (int x = k; x >= 0; x--) {
                    flipped.add(segment.get(x));
                    count++;
                }

                count = 0;
                for (int x = i + 1; x < i + k; x++) {
                    newTour.set(x, flipped.get(count));
                    count++;
                }

                count = 0;
                for (int x = i + k; x <= j; x++) {
                    newTour.set(x, segment.get(count));
                    count++;
                }

                count = 0;
                for (int x = j + 1; x < n; x++) {
                    newTour.set(x, tour.get((i + k + 1 + count) % n));
                    count++;
                }

                if (calculateTourLength(newTour) < calculateTourLength(tour)) {
                    tour.clear();
                    for (Node node : newTour) {
                        tour.add(node);
                    }
                }
            }
        }

        return tour;
    }

    // K Opt tour finish

    // SIMULATED ANNEALING
    public static List<Node> simulatedAnnealingOptimizeTour(List<Node> tour) {
        Random rand = new Random();
        double temperature = 700;
        // double coolingRate = 0.003;
        double coolingRate = 0.00001;
        List<Node> currentSolution = new ArrayList<>(tour);
        List<Node> bestSolution = new ArrayList<>(tour);

        while (temperature > 1) {
            List<Node> newSolution = new ArrayList<>(currentSolution);

            // Generate a new neighboring solution by randomly swapping two nodes
            int i = rand.nextInt(newSolution.size() - 1) + 1;
            int j = rand.nextInt(newSolution.size() - 1) + 1;
            Node tmp = newSolution.get(i);
            newSolution.set(i, newSolution.get(j));
            newSolution.set(j, tmp);

            // Compute the cost of the new and current solutions
//            double currentCost = computeTourCost(currentSolution);
//            double newCost = computeTourCost(newSolution);
            double currentCost = calculateTourLength(currentSolution);
            double newCost = calculateTourLength(newSolution);

            // Decide whether to accept the new solution or not
            if (newCost < currentCost) {
                currentSolution = new ArrayList<>(newSolution);
            } else if (Math.exp((currentCost - newCost) / temperature) > rand.nextDouble()) {
                currentSolution = new ArrayList<>(newSolution);
            }

            // Update the best solution found so far
            if (calculateTourLength(currentSolution) < calculateTourLength(bestSolution)) {
                bestSolution = new ArrayList<>(currentSolution);
            }

            // Decrease the temperature
            temperature *= 1 - coolingRate;
        }

        return currentSolution;
    }
    // SIMULATED ANNEALING FINISH

    // ANT COLONY OPTIMIZATION
    // In this example, we're creating an instance of AntColonyOptimization with 10
    // ants, alpha = 1.0, beta = 5.0, evaporation rate = 0.5, and Q = 100.0.
    public static class Ant {

        private final List<Integer> visited;
        private final boolean[] visitedArray;
        private int current;
        private List<Node> nodes;
        private int numNodes;
        private double[][] distances;

        public Ant(int start, List<Node> nodes, double[][] distances) {
            this.distances = distances;
            this.nodes = nodes;
            this.numNodes = nodes.size();
            this.visited = new ArrayList<>(numNodes);
            this.visitedArray = new boolean[numNodes];
            this.current = start;
            visited.add(current);
            visitedArray[current] = true;
            //();
        }

        public boolean isComplete() {
            return visited.size() == numNodes;
        }

        public void move(double[][] distances, double[][] pheromones, double alpha, double beta, Random rand) {
            double[] probabilities = new double[numNodes];
            double totalProb = 0.0;

            // Calculate probabilities for next move
            for (int i = 0; i < numNodes; i++) {
                if (!visitedArray[i]) {
                    double pheromone = Math.pow(pheromones[current][i], alpha);
                    double distance = Math.pow(1.0 / distances[current][i], beta);
                    probabilities[i] = pheromone * distance;
                    totalProb += probabilities[i];
                }
            }

            // Choose next node randomly based on probabilities
            double r = rand.nextDouble() * totalProb;
            double sum = 0.0;
            int next = -1;
            for (int i = 0; i < numNodes; i++) {
                if (!visitedArray[i]) {
                    sum += probabilities[i];
                    if (sum >= r) {
                        next = i;
                        break;
                    }
                }
            }

            // Move to next node
            visited.add(next);
            visitedArray[next] = true;
            current = next;
        }

        public double getTourLength(double[][] distances) {
            double tourLength = 0.0;
            for (int i = 0; i < visited.size() - 1; i++) {
                int from = visited.get(i);
                int to = visited.get(i + 1);
                tourLength += distances[from][to];
            }
            tourLength += distances[visited.get(numNodes - 1)][visited.get(0)];
            return tourLength;
        }

        public List<Node> getTour(List<Node> nodes) {
            List<Node> tour = new ArrayList<>(numNodes);
            for (int i = 0; i < visited.size(); i++) {
                tour.add(nodes.get(visited.get(i)));
            }
            return tour;
        }

        public void reset(int start) {
            visited.clear();
            Arrays.fill(visitedArray, false);
            current = start;
            visited.add(current);
            visitedArray[current] = true;
        }

        public boolean visits(int i, int j) {
            int idx_i = visited.indexOf(i);
            int idx_j = visited.indexOf(j);
            if (idx_i == -1 || idx_j == -1) {
                return false;
            }
            return Math.abs(idx_i - idx_j) == 1 || Math.abs(idx_i - idx_j) == numNodes - 1;
        }

        public int getTourLength() {
            return visited.size();
        }
    }

    public static List<Node> aCOpt(List<Node> nodes) {

        // initialize Attributes
        Random rand = new Random();
        int numNodes = nodes.size();

        double alpha = 1.0;
        double beta = 5.0; // can range in 1.0-5.0
        double evaporationRate = 0.5; // can range in 0.1-0.5
        double initialPheromoneLevel = 1.0;
        int maxIterations = 100;
        int numAnts = 10;

        double[][] pheromones = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                pheromones[i][j] = initialPheromoneLevel;
                pheromones[j][i] = initialPheromoneLevel;
            }
        }
        double distances[][] = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                distances[i][j] = Graph.calculateDistance(nodes.get(i), nodes.get(j));
            }
        }

        // Initialize ants
        Ant[] ants = new Ant[numAnts];
        for (int i = 0; i < numAnts; i++) {
            ants[i] = new Ant(rand.nextInt(numNodes), nodes, distances);
        }

        // Main loop
        for (int iter = 0; iter < maxIterations; iter++) {
            // Move ants
            for (Ant ant : ants) {
                while (!ant.isComplete()) {
                    ant.move(distances, pheromones, alpha, beta, rand);
                }
            }

            // Update pheromones
            for (int i = 0; i < numNodes; i++) {
                for (int j = i + 1; j < numNodes; j++) {
                    double deltaPheromone = 0.0;
                    for (Ant ant : ants) {
                        if (ant.visits(i, j)) {
                            deltaPheromone += 1.0 / ant.getTourLength();
                        }
                    }
                    pheromones[i][j] = (1 - evaporationRate) * pheromones[i][j] + deltaPheromone;
                    pheromones[j][i] = pheromones[i][j];
                }
            }

            // Reset ants
            for (Ant ant : ants) {
                ant.reset(rand.nextInt(numNodes));
            }
        }

        // Return best tour found by any ant
        double bestTourLength = Double.MAX_VALUE;
        List<Node> bestTour = null;
        for (Ant ant : ants) {
            double tourLength = ant.getTourLength(distances);
            if (tourLength < bestTourLength) {
                bestTourLength = tourLength;
                bestTour = ant.getTour(nodes);
            }
        }
        return bestTour;
    }

    // ANT COLONY OPTIMIZATION FINISH

    private static void swap(List<Node> nodes, Integer i, Integer j) {
        Node nodeI = nodes.get(i);
        Node nodeJ = nodes.get(j);
        nodes.set(i, nodeJ);
        nodes.set(j, nodeI);
    }

    public static double calculateTourLength(List<Node> tour) {
        double length = 0;
        for (int i = 0; i < tour.size() - 2; i++) {
            Node source = tour.get(i);
            Node destination = tour.get(i + 1);
            double l = Graph.calculateDistance(source, destination);
            length += l;
        }
        /*
         * Adding the distance between first and last node
         */
        Node source = tour.get(0);
        Node destination = tour.get(tour.size() - 1);
        length += Graph.calculateDistance(source, destination);
        return length;
    }
}