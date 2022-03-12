package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
* Created by y on 5/28/14.
*/
class JointQuotient<Variable,Value> extends ENode<Variable,Value> {
    public final Variable var;
    public final Value x;
    public final Map<Variable,Value> pi;
    public final Map<Value,Double> ps;

    public JointQuotient(Variable var, Value x, Map<Variable, Value> pi, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent) {
        super( edgar, conditions, parent );
        this.var = var;
        this.x = x;
        this.pi = pi;
        Map<Value,Double> tmp = new HashMap<>(2);
        for (Value z : edgar.network.values(edgar.contrast) )
            tmp.put(z, edgar.jointP(var, x, pi, conditions, z));
        ps = Collections.unmodifiableMap(tmp);
    }

    @Override
    public double getScore(){
        Iterator<Double> iter = ps.values().iterator();
        return Math.abs( iter.next() - iter.next() );
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if (needToGenerate){
            if (!pi.isEmpty()){
                List<EDGrNode<Variable,Value>> children = new ArrayList<>(2);
                // Conditional part
                children.add( new Conditional(var,x,pi,conditions,edgar,parent) );
                // JointQuotient parents part
                children.add( new PiJoint(pi,conditions,edgar,parent) );

                Map<TreeNode, EDGrNode<Variable, Value>> addList = addChildrenToTree( children );
                needToGenerate = false;
                return addList;
            }
            needToGenerate = false;
        }
        return Collections.EMPTY_MAP;
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        parent.drawPath(ga);
    }

    @Override
    public String toString(){
        /*StringBuilder sb = new StringBuilder("<html>");
        if (pi.isEmpty()){
            sb.append(var.toString()).append(" has no more parents.");
        } else {
            for (Map.Entry<Value,Double> z : ps.entrySet()){
                sb.append("P(").append(var.toString()).append("=").append(x.toString())
                        .append(", ").append(pi.toString()).append("|");
                if (!conditions.isEmpty())
                    sb.append(conditions.toString()).append(", ");
                sb.append(edgar.contrast).append("=").append(z.getKey())
                        .append(") = ").append(z.getValue().toString()).append("<br />");
            }

            sb.append("Difference = ").append(Double.toString(getScore())).append("<br />");


            sb.append("Ratio = ").append(q);
        }
        sb.append("</html>");
        return sb.toString();*/
        double q = 0;
        for( double z : ps.values() ) q = Math.log(z)-q;
        q = Math.exp( Math.abs(q) );

        return "Ratio: "+NF.format(q);
    }
}
