package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;
import edu.pitt.isp.sverchkov.egvz.GraphAdapter;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
* Created by y on 5/28/14.
*/
class TopLevelEN<Variable,Value> extends ENode<Variable,Value> {

    public final Variable var;
    private final List<SingleMarginal<Variable,Value>> diffChildren, otherChildren;
    private final String diffLabel, otherLabel;

    TopLevelEN(Variable var, EDGrLogic<Variable, Value> edgar){
        super(edgar, Collections.EMPTY_MAP,null);
        this.var = var;

        Collection<Value> values = edgar.network.values(var);

        diffChildren = new ArrayList<>();
        otherChildren = new ArrayList<>();

        for (Value x : values){
            SingleMarginal<Variable,Value> child = new SingleMarginal<>(var, x, edgar, this);
            (child.getScore() > edgar.dThreshold ? diffChildren : otherChildren).add(child);
        }

        diffLabel = "Values of "+var.toString()+" showing high differences across "+edgar.contrast.toString();
        otherLabel = "Values of "+var.toString()+" showing low differences across "+edgar.contrast.toString();
    }

    @Override
    public double getScore() {
        return (diffChildren.isEmpty() ? Collections.max( otherChildren ) : Collections.max( diffChildren ) ).getScore();
    }

    @Override
    public Map<TreeNode, EDGrNode<Variable, Value>> generateChildren() {
        if (needToGenerate){
            Collections.sort(diffChildren, Collections.reverseOrder());
            Collections.sort(otherChildren, Collections.reverseOrder());

            List<AggregateMarginal<Variable,Value>> myChildren = new ArrayList<>(2);
            if (!diffChildren.isEmpty())
                myChildren.add( new AggregateMarginal<>(diffLabel, edgar, diffChildren, this) );
            if (!otherChildren.isEmpty())
                myChildren.add( new AggregateMarginal<>(otherLabel, edgar, otherChildren, this) );
            Map<TreeNode, EDGrNode<Variable, Value>> addList = addChildrenToTree(myChildren);
            needToGenerate = false;
            return addList;
        }
        return Collections.EMPTY_MAP;
    }

    @Override
    public void drawPath(GraphAdapter<Variable, Value> ga) {
        ga.clear();
        ga.addNode(edgar.contrast);
        ga.placeNearTop(edgar.contrast);
        ga.anchor(edgar.contrast);
        ga.addNode(var);
        ga.placeNearBottom(var);
        ga.anchor(var);
        for (Variable p : edgar.getOrderedParents(var)) {
            ga.addNode(p);
            //ga.addEdge(p, var);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<html><font");
        //if (getScore()<edgar.dThreshold) sb.append(GREYOUTMOD);
        sb.append(">").append(var.toString())
                //.append(" (max P difference of ")
                //.append(Double.toString(score))
                //.append(")")
                .append("</font></html>");
        return sb.toString();
    }
}
