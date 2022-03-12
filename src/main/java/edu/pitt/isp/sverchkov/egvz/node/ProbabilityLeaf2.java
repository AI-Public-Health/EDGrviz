package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Map;

/**
 * Created by y on 5/28/14.
 */
class ProbabilityLeaf2<Variable,Value> extends EDGrNode<Variable,Value> {
    public final Map<Value,Double> ps;

    ProbabilityLeaf2(Map<Value,Double> ps, EDGrNode<Variable, Value> parent) {
        super(parent);
        this.ps = Collections.unmodifiableMap(ps);
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

        for (Map.Entry<Value,Double> z : ps.entrySet())
            sb.append("Probability in ").append(z.getKey())
                    .append(": ").append(NF.format(z.getValue())).append("<br />");

        sb.append("</html>");
        return sb.toString();
    }
}
