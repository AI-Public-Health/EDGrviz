package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class LabelNonLeaf<Variable,Value> extends EDGrNode<Variable,Value> {

    public final String label;
    private boolean needToGenerate;
    private List<EDGrNode<Variable,Value>> children;

    LabelNonLeaf(String label, List<EDGrNode<Variable,Value>> children, EDGrNode<Variable, Value> parent) {
        super(parent);
        this.label = label;
        needToGenerate = true;
        this.children = children;
    }

    @Override
    public double getScore() {
        return parent.getScore();
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if (!needToGenerate) return Collections.EMPTY_MAP;

        Map<TreeNode, EDGrNode<Variable, Value>> addList = addChildrenToTree( children );
        needToGenerate = false;
        return addList;
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        parent.drawPath(ga);
    }

    @Override
    public String toString(){ return label; }
}
