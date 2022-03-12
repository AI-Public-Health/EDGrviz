package edu.pitt.isp.sverchkov.egvz;

import edu.pitt.isp.sverchkov.bn.BayesNet;
import edu.pitt.isp.sverchkov.graph.DAG;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by y on 5/20/14.
 */
public class GraphAdapter<Variable,Value> {
    private final DirectedGraph<Node<Variable,Value>,Integer> g;
    private final VisualizationViewer<Node<Variable,Value>,Integer> vv;
    private final AbstractLayout<Node<Variable,Value>,Integer> al;
    private final Map<Variable,Node<Variable,Value>> labels;
    private final Map<Variable,Point2D> locations;
    private int counter = 0;
    private DAG<Variable> net;

    public GraphAdapter(DirectedGraph<Node<Variable,Value>, Integer> g, VisualizationViewer<Node<Variable,Value>, Integer> vv, AbstractLayout<Node<Variable,Value>,Integer> al) {
        this.g = g;
        this.vv = vv;
        this.al = al;
        labels = new HashMap<>();
        locations = new HashMap<>();
    }

    public void anchor(Variable var) {
        Node<Variable,Value> node = labels.get(var);
        if (null != node)
            al.lock(node, true);
    }

    public void addNode(Variable var){
        addNode( var, null );
    }

    public void addNode(Variable var, Value x){
        if (!labels.containsKey( var )) {
            Node<Variable, Value> n = new Node<>();

            n.node = var;
            n.value = x;

            labels.put(var, n);
            g.addVertex(n);

            if (locations.containsKey(var))
                al.setLocation(n, locations.get(var));

            for (Variable parent : net.parents(var) )
                if (labels.containsKey(parent)) addEdge(parent, var);

            for (Variable child : labels.keySet() )
                if (net.parents(child).contains(var)) addEdge(var, child);

        } else {
            labels.get( var ).value = x;
        }
        vv.repaint();
    }

    public void clear(){
        for (Node<Variable,Value> n : labels.values() ) {
            locations.put(n.node, new Point2D.Double( al.getX( n ), al.getY( n ) ));
            g.removeVertex(n);
        }
        labels.clear();
        counter = 0;
        vv.repaint();
    }

    private void addEdge(Variable parent, Variable child){
        g.addEdge(counter++, labels.get(parent), labels.get(child));
        vv.repaint();
    }

    public void placeNearBottom(Variable var) {
        Node<Variable,Value> node = labels.get(var);
        if (null != node){
            Dimension d = al.getSize();
            al.setLocation(node, new Point2D.Double( d.getWidth()/2, d.getHeight()*2/3 ));
        }
    }

    public void placeNearTop(Variable var) {
        Node<Variable,Value> node = labels.get(var);
        if (null != node){
            Dimension d = al.getSize();
            al.setLocation(node, new Point2D.Double( d.getWidth()/2, d.getHeight()/3 ));
        }
    }

    public void setNet(DAG<Variable> net) {
        this.net = net;
    }

    public static class Node<Variable,Value> {
        private Variable node;
        private Value value;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder( node.toString() );
            if( null != value ) sb.append(" = ").append(value.toString());
            return sb.toString();
        }
    }
}
