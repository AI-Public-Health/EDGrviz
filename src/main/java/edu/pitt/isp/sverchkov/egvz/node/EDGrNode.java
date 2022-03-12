package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
public abstract class EDGrNode<Variable, Value> implements Comparable<EDGrNode<Variable, Value>> {
    public static final String GREYOUTMOD = " color=gray";
    public static final NumberFormat NF = NumberFormat.getInstance();
    protected final EDGrNode<Variable, Value> parent;
    protected transient MutableTreeNode treeNode;

    public EDGrNode(EDGrNode<Variable, Value> parent) {
        this.parent = parent;
        this.treeNode = null;
    }

    public static <Variable,Value> EDGrNode<Variable,Value> makeNode( Variable var, EDGrLogic<Variable,Value> edgar ){
        return new TopLevelEN<>(var, edgar);
    }

    public abstract double getScore();

    @Override
    public int compareTo(EDGrNode<Variable, Value> o) {
        return Double.compare(getScore(),o.getScore());
    }

    /**
     * Generates the inference children for this node
     * @return A map of the TreeNode-EDGrNode pairs added.
     */
    public abstract Map<TreeNode, EDGrNode<Variable, Value>> generateChildren();

    public abstract void drawPath(GraphAdapter<Variable, Value> ga);

    public void setTreeNode(MutableTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public MutableTreeNode getTreeNode(){
        return treeNode;
    }

    /**
     * Creates tree nodes for children and adds them to the tree node, preserving order
     * @param children
     * @return A map of the TreeNode-EDGrNode pairs added.
     */
    protected Map<TreeNode, EDGrNode<Variable, Value>> addChildrenToTree( List<? extends EDGrNode<Variable,Value>> children){
        Map<TreeNode, EDGrNode<Variable, Value>> addList = new HashMap<>();
        if (null != treeNode){
            for (EDGrNode<Variable, Value> child : children){
                MutableTreeNode node = new DefaultMutableTreeNode( child );
                child.setTreeNode(node);
                treeNode.insert(node, treeNode.getChildCount());
                addList.put(node, child);
            }
        }
        return addList;
    }
}