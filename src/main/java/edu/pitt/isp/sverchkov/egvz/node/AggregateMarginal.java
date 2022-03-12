package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class AggregateMarginal<Variable,Value> extends ENode<Variable,Value> {
    private final List<SingleMarginal<Variable,Value>> children;
    private final String label;

    AggregateMarginal(String label, EDGrLogic<Variable, Value> edgar, List<SingleMarginal<Variable, Value>> children, TopLevelEN<Variable, Value> parent){
        super( edgar, Collections.EMPTY_MAP, parent );
        this.children = children;
        this.label = label;
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if (needToGenerate) {
            Collections.sort(children, Collections.reverseOrder());
            Map<TreeNode, EDGrNode<Variable, Value>> addList = addChildrenToTree( children );
            needToGenerate = false;
            return addList;
        }
        return Collections.EMPTY_MAP;
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        parent.drawPath(ga);
    }

    @Override
    public String toString() {
        return label;
        /*StringBuilder sb = new StringBuilder("<html><font");
        if (score<edgar.dThreshold) sb.append(GREYOUTMOD);
        sb.append(">").append(var.toString())
                .append(" (max P difference of ")
                .append(Double.toString(score))
                .append(")</font></html>");
        return sb.toString();*/
    }

    @Override
    public double getScore() {
        return Collections.max( children ).getScore();
    }
}