package com.github.hhhzzzsss.hbot.util.kdtree;

public class Node {
    private double[] coords_;
    public Node left_ = null;
    public Node right_ = null;

    public Node(double[] coords) {
        coords_ = coords;
    }
    public Node(double x, double y) {
        this(new double[]{x, y});
    }
    public Node(double x, double y, double z) {
        this(new double[]{x, y, z});
    }
    public double get(int index) {
        return coords_[index];
    }
    public double distance(Node node) {
        double dist = 0;
        for (int i = 0; i < coords_.length; ++i) {
            double d = coords_[i] - node.coords_[i];
            dist += d * d;
        }
        return dist;
    }
    public String toString() {
        StringBuilder s = new StringBuilder("(");
        for (int i = 0; i < coords_.length; ++i) {
            if (i > 0)
                s.append(", ");
            s.append(coords_[i]);
        }
        s.append(')');
        return s.toString();
    }
}