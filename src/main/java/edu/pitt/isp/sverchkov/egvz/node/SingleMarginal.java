package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* Created by y on 5/28/14.
*/
class SingleMarginal<Variable,Value> extends ENode<Variable,Value> {
    public final Variable var;
    public final Value x;
    private final MarginalDifference<Variable,Value> diffs;

    SingleMarginal(Variable var, Value x, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent){
        super( edgar, conditions, parent );
        this.var = var;
        this.x = x;
        diffs = new MarginalDifference<>(var, x, conditions, edgar, this );
    }

    SingleMarginal(Variable var, Value x, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent){
        this( var, x, Collections.EMPTY_MAP, edgar, parent );
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren(){
        if (!needToGenerate) return Collections.EMPTY_MAP;

        double lnQ = 0;
        for (Double p : diffs.ps.values() )
            lnQ = Math.log(p) - lnQ;
        lnQ = Math.abs( lnQ );

        ProbabilityLeaf2<Variable,Value> probs = new ProbabilityLeaf2<>(diffs.ps, this);
        LabelLeaf<Variable,Value> qlabel = new LabelLeaf<>("Ratio: "+NF.format(Math.exp(lnQ)),this);

        List<EDGrNode<Variable,Value>> children = Arrays.asList(
                probs,
                qlabel,
                diffs
                //new AggregateJoints<>(var, x, conditions, edgar, this)
        );

        Map<TreeNode, EDGrNode<Variable, Value>> addList = addChildrenToTree( children );
        needToGenerate = false;
        return addList;

    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        //ga.clear();
        parent.drawPath(ga);
        ga.addNode(var, x);
        for (Variable p : edgar.getOrderedParents(var)) {
            ga.addNode(p);
        }
        for (Map.Entry<Variable,Value> e : conditions.entrySet())
            ga.addNode(e.getKey(),e.getValue());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(var.toString());
        sb.append(" = ").append(x.toString());
        if (!conditions.isEmpty()) sb.append("|").append(conditions.toString());
        return sb.toString();
    }

    @Override
    public double getScore() {
        return diffs.getScore();
    }

    double getP( Value z ){
        return diffs.getP(z);
    }

}
