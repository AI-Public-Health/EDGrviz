package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
* Created by y on 5/28/14.
*/
class MarginalDifference<Variable,Value> extends ENode<Variable,Value> {

    public final Variable var;
    public final Value x;
    public final Map<Value,Double> ps;

    MarginalDifference(Variable var, Value x, Map<Variable, Value> conditions, EDGrLogic<Variable, Value> edgar, EDGrNode<Variable, Value> parent){
        super( edgar, conditions, parent );
        this.var = var;
        this.x = x;
        Map<Value,Double> tmp = new HashMap<>(2);
        for (Value z : edgar.network.values(edgar.contrast) )
            tmp.put(z, edgar.jointP(var, x, Collections.EMPTY_MAP, conditions, z));
        ps = Collections.unmodifiableMap(tmp);
    }

    @Override
    public String toString(){
        /*StringBuilder sb = new StringBuilder("<html><font");

        double d = getScore(), q = 0;
        for( double z : ps.values() ) q = Math.log(z)-q;
        q = Math.exp( Math.abs(q) );

        if (d<edgar.dThreshold) sb.append(GREYOUTMOD);
        sb.append(">");

        for (Map.Entry<Value,Double> z : ps.entrySet()){
            sb.append("P(").append(var.toString()).append("=")
                    .append(x.toString()).append("|");
            if (!conditions.isEmpty())
                sb.append(conditions.toString()).append(", ");
            sb.append(edgar.contrast).append("=").append(z.getKey())
                    .append(") = ").append(z.getValue().toString()).append("<br />");
        }

        sb.append("Ratio = ").append(q).append("<br />")
                .append("Difference = ").append(Double.toString(d));

        sb.append("</html>");
        return sb.toString();*/
        return "Difference: "+NF.format(getScore());
    }

    @Override
    public double getScore() {
        double score = 0;
        for (Double p : ps.values()) score = p-score;
        return Math.abs(score);
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if (!needToGenerate) return Collections.EMPTY_MAP;

        List<JointGroup<Variable,Value>> children = new ArrayList<>();
        for( Map<Variable,Value> pi : edgar.piAssignments(var) )
            if (!EDGrLogic.conflict(pi, conditions))
                children.add( new JointGroup<>(var, x, pi, conditions, edgar, this ) );

        if (children.size() < 2) {
            needToGenerate = false;
            return Collections.EMPTY_MAP;
        }

        Collections.sort(children, Collections.reverseOrder());

        List<EDGrNode<Variable,Value>>
                bigChildren = new ArrayList<>(),
                smallChildren = new ArrayList<>();

        List<Value> keys = new ArrayList<>( ps.keySet() );
        int nKeys = keys.size();
        double target = 0;
        for (int i=0; i < nKeys; i++)
            if (0 == (i & 1)) target += ps.get(keys.get(i));
            else target -= ps.get(keys.get(i));
        target /= 2;

        boolean negative = (target < 0);

        for (JointGroup<Variable,Value> child : children){
            if (negative ^ (target > 0)) {
                for (int i = 0; i < nKeys; i++)
                    if (0 == (i & 1)) target -= child.getP(keys.get(i));
                    else target += child.getP(keys.get(i));

                bigChildren.add(child);
            } else
                smallChildren.add(child);
        }

        List<EDGrNode<Variable,Value>> myChildren = new ArrayList<>(2);
        if(!bigChildren.isEmpty())
            myChildren.add( new LabelNonLeaf<Variable, Value>("Terms accounting for most of the difference", bigChildren, this) );
        if(!smallChildren.isEmpty())
            myChildren.add( new LabelNonLeaf<Variable, Value>("Remaining terms", smallChildren, this) );

        needToGenerate = false;
        //return addChildrenToTree( Collections.singletonList( new AggregateJoints<>(var, x, conditions, edgar, this) ) );
        return addChildrenToTree( myChildren );
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        parent.drawPath(ga);
    }

    double getP( Value z ){
        return ps.get(z);
    }
}
