/**
 * 
 */
package tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author gagazhn
 *
 */
public class PureToData {
	private static HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
	private static int seek = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new FileInputStream("./data/COAE.G.pure.raw"));
			OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("./data/COAE.PURE.data"), "UTF-8");
			Graph g = new Graph();
			map.put(-1, -1);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					String[] attr = line.trim().split(" ");
					
					String[] gs = attr[0].split("@");
					int index = Integer.parseInt(gs[0]);
					int parent = Integer.parseInt(gs[1]);
					
					g.addNode(index, parent, attr);
				} else {
					String s = g.toString();
					if (s.indexOf("[   OP   ]") > -1) {
						System.out.println(s);
						ow.write(s + "\r\n");
					}
					
					g = new Graph();
					map.clear();
					map.put(-1, -1);
					seek = 0;
				}
			}
			
			scanner.close();
			ow.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class Graph {
		public HashMap<Integer, Node> nodes;
		public int root;
		
		public Graph() {
			this.nodes = new HashMap<Integer, Node>();
			this.root = -1;
		}
		
		public void addNode(int index, int parent, String[] attr) {
			Node node = new Node();
			node.index = index;
			node.parent = parent;
			node.attr = attr;
			
			this.nodes.put(index, node);
			
			if (this.root == -1) {
				this.root = index;
			} else {
				nodes.get(parent).children.add(index);
			}
		}
		
		public String toString() {
			return visit(root, map);
		}
		
		public String visit(int n, HashMap<Integer, Integer> map) {
			StringBuilder output = new StringBuilder();
			
			Node node = nodes.get(n);
			String[] attr = node.attr;
			
			//!!
			if (attr[4].equals("true")) {
				System.out.println("EEEEEEEE");
				System.exit(0);
			}
			
			String graph = attr[0];
			String value = attr[1];
			
			n = n;
			int parent = Integer.parseInt(graph.substring(graph.indexOf("@") + 1));
			
			map.put(n, seek++);
			
			Node headWord = nodes.get(Integer.parseInt(attr[3])); 
			Node parentNode = nodes.get(parent);
			Node ancestor = nodes.get(Integer.parseInt(attr[6]));
			Node gov = nodes.get(Integer.parseInt(headWord.attr[9]));			
			
			String op = attr[7];
			String em = attr[8];
			
			// label 合并叶子与次叶子
			String label = null;
			if (attr[5].equals("true")) {
				if (attr[1].equals("PU")) {
					label = "[        ]";
				} else if (headWord.attr[10].equals("OP")) {
					label = "[   OP   ]";
				} else if(headWord.attr[10].equals("ATTR")) {
					label = "[  ATTR  ]";
				} else {
					label = "[        ]";
				}
			} else {
				// 内节点
				/*
//				if ("NP,VP,IP,ADVD".indexOf(value) > -1) {
				if ("".indexOf(value) > -1) {
					if (headWord.attr[10].equals("OP")) {
						label = "{" + value + ":" +"OP";
					} else if (headWord.attr[10].equals("ATTR")) {
						label = "{" + value + ":" +"ATTR";
					} else {
						label = "{" + value + ":" +"NO";
					}
				} else {
					label = "{" + headWord.attr[10];
				}
				*/
				
				// 以op为主
				label = "{" + search(n);
				
				for (int i = label.length(); i < 9; i++) {
					label = label + " " ;
				}
				label = label + "}";
			}
			output.append(label);
			output.append(" --- ");
			
			// graph
			if (value.equals("PU")) {
				output.append(map.get(n) + "@" + map.get(n));
			} else {
				output.append(map.get(n) + "@" + map.get(parent));
			}
			output.append(" ---");
			
			//=============  basic  =============
			// W POS VALUE
			if (attr[5].equals("true")) {
				output.append(" W:" + headWord.attr[1]);
				output.append(" P:" + value);
			} else {
				output.append(" S:" + value);
			}
			
			//============= syntax tree =============
			if (value.equals("PU")) {
				
			} else if (attr[5].equals("true")) {
				
			} else {
				output.append(" #head:" + headWord.attr[1]);
				
				if (parentNode != null) {
					output.append(" #" + value + ":" + parentNode.attr[1]);
				}
				
				if (parentNode != null) {
					Node parentHead = nodes.get(Integer.parseInt(parentNode.attr[3]));
					if (headWord.attr[1].equals(parentHead.attr[1])) {
						output.append(" #HeadToHeadSame");
					} else {
						output.append(" #HeadToHead:" + headWord.attr[1] + ":" + parentHead.attr[1]);
					}
				}
			}
			if (gov != null) {
				output.append(" DepWord:" + gov.attr[1]);
				output.append(" DepPOS:" + gov.attr[2]);
			}
			if (parent != -1 && !attr[2].equals("PU")) {
				output.append(" #Parent:" + parentNode.attr[1]);
			}
			
			//============= dict =============
			if (attr[1].equals("PU")) {
				// nothing
			} else if (attr[5].equals("true")) {
				output.append(" OP:" + attr[7]);
				output.append(" EM:" + attr[8]);
			} else {
				output.append(" HOP:" + headWord.attr[7]);
				output.append(" HEM:" + headWord.attr[8]);
			}
			
			output.append("\r\n");
			
//			 递归时，将叶子与上一层合并
			if (attr[5].equals("true")) {
				
			} else {
				for (int child : node.children) {
					output.append(visit(child, map));
				}
			}
			
			return output.toString();
		}
		
		private String search(int n) {
			String r = null;
			Node node = nodes.get(n);
			if (node.attr[4].equals("true")) {
				if (node.attr[10].equals("OP")) {
					r = "OP";
				} else if (node.attr[10].equals("ATTR")) {
					r = "ATTR";
				}
			}
			
//			if (!node.attr[5].equals("true")) {
				for (int child : node.children) {
					String s = search(child);
					if (r == null) {
						r = s;
					} else {
						if (s != null && s.equals("OP")) {
							r = s;
						}
					}
				}
//			}
			
			return r;
		}
	}
	
	public static class Node {
		public int index;
		public int parent;
		public String[] attr;
		
		public ArrayList<Integer> children;
		
		public Node() {
			this.children = new ArrayList<Integer>();
		}
	}
}
