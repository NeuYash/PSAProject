package com.project;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.project.model.Christofides;
import com.project.model.Edge;
import com.project.model.Graph;
import com.project.model.Node;

public class RandomTourOptTest {

	public static List<Node> ChrisTSP() {
		Graph graph = App.getNodesFromDataset();
		graph.connectAllNodes();
		List<Edge> mst = Christofides.findMST(graph);
		List<Node> oddDegrNodes = Christofides.findOddDegreeVertices(graph, mst);
		List<Edge> perfectMatchingEdges = Christofides.getMinimumWeightPerfectMatching(oddDegrNodes);
		List<Node> eulerTour = Christofides.eulerTour(graph, mst, perfectMatchingEdges);
		return Christofides.generateTSPTour(eulerTour);
	}
	
	public static List<Node> ChrisTSPNew(Graph graph) {
		graph.connectAllNodes();
		List<Edge> mst = Christofides.findMST(graph);
		List<Node> oddDegrNodes = Christofides.findOddDegreeVertices(graph, mst);
		List<Edge> perfectMatchingEdges = Christofides.getMinimumWeightPerfectMatching(oddDegrNodes);
		List<Node> eulerTour = Christofides.eulerTour(graph, mst, perfectMatchingEdges);
		return Christofides.generateTSPTour(eulerTour);
	}
	
	@Test
	public void testTwoNodeGraph() {
		Graph graph = new Graph();

		Node node1 = new Node("CrimeID1", -0.009691, 51.483548);
		Node node2 = new Node("CrimeID2", -0.118888, 51.513075);
		graph.addNode(node1);
		graph.addNode(node2);
		graph.connectAllNodes();
		List<Node> tsp = ChrisTSPNew(graph);
		assertEquals(Graph.calculateDistance(node1, node2), Christofides.calculateTourLength(Christofides.randomSwapOptimise(tsp, 1)), 0.01);
		assertEquals(node1, tsp.get(0));
		assertEquals(node2, tsp.get(1));
	}
	
	@Test
	public void testThreeNodeGraph() {
		Graph graph = new Graph();

		Node node1 = new Node("CrimeID1", -0.009691, 51.483548);
		Node node2 = new Node("CrimeID2", -0.118888, 51.513075);
		Node node3 = new Node("CrimeID3", 0.076327, 51.540042);
		graph.addNode(node1);
		graph.addNode(node2);
		graph.addNode(node3);
		graph.connectAllNodes();
		List<Node> tsp = ChrisTSPNew(graph);
		assertEquals(Christofides.calculateTourLength(tsp), Christofides.calculateTourLength(Christofides.randomSwapOptimise(tsp, 2)), 0.01);
		assertEquals(node1, tsp.get(0));
		assertEquals(node2, tsp.get(1));
	}
	
	@Test
	public void RandomSwapOPT() {
		List<Node> tsp = ChrisTSP();
		assertEquals(tsp.size(), Christofides.randomSwapOptimise(tsp, 1).size(), 0.01);
		assertTrue(Christofides.calculateTourLength(tsp)>Christofides.calculateTourLength(Christofides.randomSwapOptimise(tsp, 10000)));
	}

}