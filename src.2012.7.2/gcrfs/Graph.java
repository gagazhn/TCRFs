package gcrfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import type.FeatureSet;
import type.Instance;
import type.Label;
import type.LabelSet;

/**
 * @Description: 
 *
 */
public class Graph {
	private Instance instance;
	private HashMap<Integer, Node> nodes;
	private FeatureSet featureSet;
	private LabelSet labelSet;
	private Node root;
	
	public final static int ROOT = -1;
	
	public Graph(Instance instance, FeatureSet featureSet, LabelSet labelSet) {
		this.nodes = new HashMap<Integer, Graph.Node>();
		this.instance = instance;
		this.featureSet = featureSet;
		this.labelSet = labelSet;
		this.root = new Node(-1);
		this.root.parent = -2;
		this.nodes.put(-1, this.root);
	}
	
	public void addNode(int parent, int t) {
		Node node = node(t);
		if (node == null) {
			node = new Node(t);
			nodes.put(t, node);
		}
		node.parent = parent;
		
		Node parentNode = node(parent);
		if (parentNode == null) {
			parentNode = new Node(parent);
			nodes.put(parent, parentNode);
		}
		
		parentNode.children.add(t);
	}
	
	public int parent(int t) {
		return this.node(t).parent;
	}
	
	public ArrayList<Integer> childrenList(int t) {
		return node(t).children;
	}
	
	public boolean isLeaf(int t) {
		return node(t).children.size() == 0;
	}
	
	public boolean isCross(int t) {
		return node(t).children.size() > 1;
	}
	
	public Node node(int t) {
		return this.nodes.get(t);
	}
	
	public String toString() {
		return nodes.toString();
	}
	
	public class Node{
/*		double[][] transp;
		double[] V;*/
//		int[] path;
		int parent;
		ArrayList<Integer> children;
		int timestamp;
		
		Node(int t) {
			this.children = new ArrayList<Integer>();
			this.timestamp = t;
/*			this.V = new double[labelSize];
			this.transp = new double[labelSize][labelSize];*/
//			this.path = new int[labelSize];
		}
		
		public String toString() {
			return "" + this.timestamp + "@" + this.parent;
		}
	}
	
}
