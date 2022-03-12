/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.pitt.isp.sverchkov.egvz;

import edu.pitt.isp.sverchkov.bn.BayesNet;
import edu.pitt.isp.sverchkov.collections.Pair;
import edu.pitt.isp.sverchkov.collections.Tuple;
import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.node.EDGrNode;

import java.io.IOException;
import java.util.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;

/**
 *
 * @author yus24
 * @param <Variable>
 * @param <Value>
 */
public class EDGrTreeController<Variable,Value> implements Runnable, TreeWillExpandListener {
    private static final Comparator<Tuple<Double,?>> COMPARATOR = Collections.reverseOrder( new Tuple.CompareByFirst<Double>() );
    private final EDGrLogic<Variable,Value> edgar;
    private final TreeModel model;
    private final StringBuffer status;
    private volatile int nodesLoaded;
    private final int digits;
    private final DefaultMutableTreeNode root;
    private final Map<TreeNode, EDGrNode<Variable,Value>> nodeMap;

    public EDGrTreeController( String title, BayesNet<Variable,Value> net, Variable contrast) throws IOException {
        edgar = new EDGrLogic<>( net, contrast);
        digits = Integer.toString(edgar.network.size()).length();
        nodesLoaded = 0;
        status = initStatus();
        root = new DefaultMutableTreeNode( new Pair<>(title,status) );
        model = new DefaultTreeModel( root );
        nodeMap = new HashMap<>();
    }

    public TreeSelectionListener makeTSL(final GraphAdapter<Variable, Value> ga) {
        return new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath tp = e.getNewLeadSelectionPath();
                Object o = tp.getLastPathComponent();
                EDGrNode<Variable,Value> en = nodeMap.get(o);
                if (null != en){
                    en.drawPath( ga );
                }
            }
        };
    }

    public TreeModel getModel(){
        return model;
    }

    @Override
    public void run() {
        List<Pair<Double, MutableTreeNode>> list = new ArrayList<>( edgar.network.size() );
        for( Variable node : edgar.network ){
            
            Pair<Double,MutableTreeNode> pair = makeLazyTreeNode( node );
            
            int index = Collections.binarySearch(list, pair, COMPARATOR);
            if (index < 0) index = -index -1;
            
            list.add(index, pair);
            root.insert(pair.second, index);
            ++nodesLoaded;
            updateStatus();
        }
    }

    private StringBuffer initStatus() {
        final int
                n = edgar.network.size();
        return new StringBuffer(String.format("%"+digits+"d of %d loaded", nodesLoaded, n));
    }
    
    private void updateStatus() {
        status.replace(0, digits, String.format("%"+digits+"d",nodesLoaded));
    }

    private Pair<Double,MutableTreeNode> makeLazyTreeNode( Variable node ){
        EDGrNode<Variable,Value> en = EDGrNode.makeNode(node, edgar);
        MutableTreeNode tn = new DefaultMutableTreeNode( en );
        en.setTreeNode( tn );
        //en.generateChildren();
        nodeMap.put( tn, en );
        return new Pair<>(en.getScore(),tn);
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        Object pathNode = event.getPath().getLastPathComponent();
        if (pathNode instanceof TreeNode){
            TreeNode tn = (TreeNode) pathNode;
            if (!tn.isLeaf()){
                Enumeration en = tn.children();
                while( en.hasMoreElements() ){
                    EDGrNode<Variable,Value> child = nodeMap.get( en.nextElement() );
                    if (null != child){
                        nodeMap.putAll( child.generateChildren() );
                    }
                }
            }
        }
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        // Noop
    }
}
