package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.collections.CollectionTools;
import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;
import edu.pitt.isp.sverchkov.graph.GraphTools;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
* Created by y on 5/28/14.
*/
class PiJoint<Variable,Value> extends ENode<Variable,Value> {

    private final Map<Variable,Value> pi;
    private final Map<Value,Double> ps;

    PiJoint(Map<Variable, Value> pi, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent){
        super(edgar,conditions,parent);
        this.pi = pi;
        ps = new HashMap<>(2);
        for (Value z : edgar.network.values(edgar.contrast) )
            ps.put(z, edgar.jointP(pi, conditions, z));
    }

    @Override
    public double getScore() {
        double q = 0;
        for (double p : ps.values()) q = Math.log(p) - q;
        q = Math.exp( Math.abs(q) );
        return q;
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if( needToGenerate ){
            List<SingleMarginal<Variable,Value>> children =
                    findBestMarginals(
                            /*new HashMap<>(pi),
                            conditions,
                            new ArrayList<SingleMarginal<Variable,Value>>(pi.size())/**/
                    );

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

    private List<SingleMarginal<Variable,Value>> findBestMarginals( Map<Variable,Value> parents, Map<Variable,Value> inConditions, List<SingleMarginal<Variable,Value>> result ){
        if (!parents.isEmpty()){
            SingleMarginal<Variable,Value> best = null;
            double bestCD = Double.POSITIVE_INFINITY;
            for (Map.Entry<Variable,Value> x : parents.entrySet() ){
                // make
                Map<Variable,Value> newConditions = edgar.cleanup(x.getKey(), inConditions);
                SingleMarginal<Variable,Value> candidate = new SingleMarginal(x.getKey(),x.getValue(),newConditions,edgar,this);
                // compare and assign
                double candidateCD = contributionDistance( candidate );
                if (candidateCD < bestCD){
                    bestCD = candidateCD;
                    best = candidate;
                }
            }
            result.add(best);
            parents.remove(best.var);
            result = findBestMarginals(
                    parents,
                    CollectionTools.immutableMapUnion(inConditions, Collections.singletonMap(best.var, best.x))
                    ,result );
        }
        return result;
    }

    // Non-recursive, uses topological order
    private List<SingleMarginal<Variable,Value>> findBestMarginals(){
        List<SingleMarginal<Variable,Value>> result = new ArrayList<>(pi.size());
        Map<Variable,Value> currConditions = new HashMap<>(conditions);
        if (!pi.isEmpty()) for (Variable parent : GraphTools.nodesInTopOrder(edgar.network)){
            Value value = pi.get(parent);
            if (null != value && !value.equals(conditions.get(value))){
                Map<Variable,Value> newConditions = edgar.cleanup(parent, currConditions);
                result.add( new SingleMarginal(parent, value, newConditions, edgar, this ) );
                currConditions.put(parent,value);
            }
        }
        return result;
    }

    private double contributionDistance( SingleMarginal<Variable,Value> term ){
        double d = 0;
        for (Map.Entry<Value,Double> z : ps.entrySet())
            d = z.getValue() - term.getP(z.getKey()) - d;
        return Math.abs(d);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("<html>Joint term ");
        sb.append(pi.toString());
        /*
        for (Map.Entry<Value,Double> z : ps.entrySet()){
            sb.append("P(").append(pi.toString()).append("|");
            if (!conditions.isEmpty())
                sb.append(conditions.toString()).append(", ");
            sb.append(edgar.contrast).append("=").append(z.getKey())
                    .append(") = ").append(z.getValue().toString()).append("<br />");
        }

        //sb.append("Difference = ").append(Double.toString(getScore())).append("<br />");*/

        double q = 0;
        for( double z : ps.values() ) q = Math.log(z)-q;
        q = Math.exp( Math.abs(q) );

        if (q == 1)
            sb.append("<br />(No contribution to the ratio)");
        else
            sb.append("<br />Contribution to the ratio: ").append(NF.format(q));

        sb.append("</html>");
        return sb.toString();
    }
}
