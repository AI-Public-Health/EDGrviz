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
class Conditional<Variable,Value> extends ENode<Variable,Value> {
    public final Variable var;
    public final Value x;
    public final Map<Variable,Value> pi;
    public final Map<Value,Double> ps;
    public final Map<Variable,Value> finalConditions;

    Conditional(Variable var, Value x, Map<Variable, Value> pi, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent){
        super(edgar,conditions,parent);
        this.var = var;
        this.x = x;
        this.pi = Collections.unmodifiableMap(pi);
        finalConditions = edgar.cleanup( var, CollectionTools.immutableMapUnion(pi, conditions) );
        Map<Value,Double> tmp = new HashMap<>(2);
        for (Value z : edgar.network.values(edgar.contrast) )
            tmp.put(z, edgar.jointP(var, x, Collections.EMPTY_MAP, finalConditions, z));
        ps = Collections.unmodifiableMap(tmp);
    }

    @Override
    public double getScore() {
        double q = 0;
        for (double p : ps.values()) q = Math.log(p) - q;
        q = Math.exp( Math.abs(q) );
        return q;
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren(){
        if (!needToGenerate) return Collections.EMPTY_MAP;
        return addChildrenToTree( Arrays.asList(
                new LabelLeaf<Variable, Value>(
                        var.toString()+"="+x.toString()+"|"+finalConditions.toString(),
                        this),
                new ProbabilityLeaf2<Variable, Value>(ps, this) ) );
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        parent.drawPath(ga);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("<html>Conditional term<br />");

        double lnQ = 0;
        for( double z : ps.values() ) lnQ = Math.log(z)-lnQ;
        lnQ = Math.abs(lnQ);

        if (lnQ == 0)
            sb.append("(No contribution to the ratio)");
        else
            sb.append("Contribution to the ratio: ").append(NF.format(Math.exp(lnQ)));

        sb.append("</html>");
        return sb.toString();
    }
}
