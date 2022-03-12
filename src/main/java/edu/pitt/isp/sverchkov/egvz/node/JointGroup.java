package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.collections.CollectionTools;
import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class JointGroup<Variable,Value> extends ENode<Variable,Value> {
    public final Variable var;
    public final Value x;
    public final Map<Variable, Value> pi;
    private final JointQuotient<Variable,Value> qPart;

    JointGroup(Variable var, Value x, Map<Variable, Value> pi, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent) {
        super(edgar, conditions, parent);
        this.var = var;
        this.x = x;

        if (CollectionTools.haveCommon(pi.keySet(), conditions.keySet())){
            Map<Variable, Value> tmp = new HashMap<>(pi);
            tmp.keySet().removeAll( conditions.keySet() );
            this.pi = Collections.unmodifiableMap(tmp);
        } else {
            this.pi = pi;
        }

        //this.pi = pi;
        //probs = new ProbabilityLeaf<>(var, CollectionTools.immutableMapUnion(Collections.singletonMap(var,x),pi),conditions,edgar,this);
        qPart = new JointQuotient<>(var,x,this.pi,conditions,edgar,this);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append( var.toString() ).append(" = ").append(x.toString());
        if (!pi.isEmpty()) sb.append(", ").append(pi.toString());
        if (!conditions.isEmpty()) sb.append("|").append(conditions.toString());
        return sb.toString();
    }

    @Override
    public double getScore() {
        return qPart.getScore();
    }

    double getP( Value z ){ return qPart.ps.get(z); }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if (!needToGenerate) return Collections.EMPTY_MAP;

        double d = 0;
        for (double p : qPart.ps.values()) d = p - d;
        d = Math.abs( d );

        ProbabilityLeaf2<Variable,Value> probs = new ProbabilityLeaf2<>( qPart.ps, this);
        LabelLeaf<Variable,Value> dPart = new LabelLeaf<>("Difference: "+NF.format(d), this);

        needToGenerate = false;
        return addChildrenToTree( Arrays.asList( probs, dPart, qPart) );
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        //if( clear) ga.clear();
        parent.drawPath(ga);
        ga.addNode(var, x);
        for (Map.Entry<Variable,Value> e : pi.entrySet()) {
            ga.addNode(e.getKey(),e.getValue());
        }
        for (Map.Entry<Variable,Value> e : conditions.entrySet()) {
            ga.addNode(e.getKey(),e.getValue());
        }
    }
}
