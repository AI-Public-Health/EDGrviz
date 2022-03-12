package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class ProbabilityLeaf<Variable,Value> extends ENode<Variable,Value> {
    public final Map<Value,Double> ps;
    private final Map<Variable,Value> outcomes;
    private final Variable var;

    ProbabilityLeaf(Variable var, Map<Variable,Value> outcomes, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent) {
        super(edgar, conditions, parent);
        this.var = var;
        this.outcomes = outcomes;

        {
            Map<Value, Double> tmp = new HashMap<>(2);
            for (Value z : edgar.network.values(edgar.contrast))
                tmp.put(z, edgar.jointP(outcomes, conditions, z));
            ps = Collections.unmodifiableMap(tmp);
        }
    }

    double getP( Value z ){ return ps.get(z); }

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
    public String toString(){
        StringBuilder sb = new StringBuilder("<html>");

        for (Map.Entry<Value,Double> z : ps.entrySet()){
            sb.append("P(");
            if (null != var)
                sb
                    .append(var.toString()).append("=")
                    .append(outcomes.get(var).toString());

            for (Map.Entry<Variable,Value> x : outcomes.entrySet())
                if (x.getKey() != var)
                    sb.append(", ").append(x.getKey().toString())
                            .append("=").append(x.getValue().toString());

            sb.append("|");

            if (!conditions.isEmpty())
                sb.append(conditions.toString()).append(", ");

            sb.append(edgar.contrast).append("=").append(z.getKey())
                    .append(") = ").append(z.getValue().toString()).append("<br />");
        }
        sb.append("</html>");
        return sb.toString();
    }
}
