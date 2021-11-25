package com.github.hhhzzzsss.hbot.util.kdtree;

import java.util.*;

public class KdTree {
    private int dimensions_;
    private Node root_ = null;
    private Node best_ = null;
    private double bestDistance_ = 0;
    private int visited_ = 0;
 
    public KdTree(int dimensions, List<Node> nodes) {
        dimensions_ = dimensions;
        root_ = makeTree(nodes, 0, nodes.size(), 0);
    }
 
    public Node findNearest(Node target) {
        if (root_ == null)
            throw new IllegalStateException("Tree is empty!");
        best_ = null;
        visited_ = 0;
        bestDistance_ = 0;
        nearest(root_, target, 0);
        return best_;
    }
 
    public int visited() {
        return visited_;
    }
 
    public double distance() {
        return Math.sqrt(bestDistance_);
    }
 
    private void nearest(Node root, Node target, int index) {
        if (root == null)
            return;
        ++visited_;
        double d = root.distance(target);
        if (best_ == null || d < bestDistance_) {
            bestDistance_ = d;
            best_ = root;
        }
        if (bestDistance_ == 0)
            return;
        double dx = root.get(index) - target.get(index);
        index = (index + 1) % dimensions_;
        nearest(dx > 0 ? root.left_ : root.right_, target, index);
        if (dx * dx >= bestDistance_)
            return;
        nearest(dx > 0 ? root.right_ : root.left_, target, index);
    }
 
    private Node makeTree(List<Node> nodes, int begin, int end, int index) {
        if (end <= begin)
            return null;
        int n = begin + (end - begin)/2;
        Node node = QuickSelect.select(nodes, begin, end - 1, n, new NodeComparator(index));
        index = (index + 1) % dimensions_;
        node.left_ = makeTree(nodes, begin, n, index);
        node.right_ = makeTree(nodes, n + 1, end, index);
        return node;
    }
    
    public void insert(Node newNode) {
    	if (root_ == null) {
    		root_ = newNode;
    		return;
    	}
    	int index = 0;
    	Node node = root_;
    	for (;;) {
    		if ((new NodeComparator(index)).compare(newNode, node) < 0) {
    			if (node.left_ == null) {
    				node.left_ = newNode;
    				break;
    			}
    			else {
    				node = node.left_;
    			}
    		}
    		else {
    			if (node.right_ == null) {
    				node.right_ = newNode;
    				break;
    			}
    			else {
    				node = node.right_;
    			}
    		}
    		index = (index + 1) % dimensions_;
    	}
    }
 
    private static class NodeComparator implements Comparator<Node> {
        private int index_;
 
        private NodeComparator(int index) {
            index_ = index;
        }
        public int compare(Node n1, Node n2) {
            return Double.compare(n1.get(index_), n2.get(index_));
        }
    }
}