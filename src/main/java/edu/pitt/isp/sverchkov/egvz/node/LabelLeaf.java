package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class LabelLeaf<Variable,Value> extends EDGrNode<Variable,Value> {

    public final String label;

    public LabelLeaf(String label, EDGrNode<Variable, Value> parent) {
        super(parent);
        this.label = label;
    }

    @Override
    public double getScore() {
        return parent.getScore();
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        parent.drawPath(ga);
    }

    @Override
    public String toString(){ return label; }
}
