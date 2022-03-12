package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class AggregateJoints<Variable,Value> extends ENode<Variable,Value> {
    private Variable var;
    private Value x;

    AggregateJoints(Variable var, Value x, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent) {
        super(edgar, conditions, parent);
        this.var = var;
        this.x = x;
    }

    @Override
    public double getScore() {
        return parent.getScore();
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren(){
        if (needToGenerate) {
            List<JointGroup<Variable,Value>> children = new ArrayList<>();
            for( Map<Variable,Value> pi : edgar.piAssignments(var) )
                if (!EDGrLogic.conflict(pi, conditions))
                    children.add( new JointGroup<>(var, x, pi, conditions, edgar, this ) );

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
    public String toString(){
        return "Terms contributing to the difference";
    }
}