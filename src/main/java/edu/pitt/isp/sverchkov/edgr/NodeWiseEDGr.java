package edu.pitt.isp.sverchkov.edgr;

import edu.pitt.isp.sverchkov.combinatorics.Assignments;
import edu.pitt.isp.sverchkov.graph.MutableDAGImpl;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author yus24
 * @param <Variable>
 * @param <Value>
 */
public class NodeWiseEDGr<Variable,Value> implements Runnable {

    private final File outF;
    private final EDGrLogic<Variable,Value> edgar;
    private BufferedWriter out;
    private final Variable startingNode;

    public NodeWiseEDGr( EDGrLogic el, Variable n, File file ){
        edgar = el;
        outF = file;
        startingNode = n;
    }

    @Override
    public void run() {
        // Make the file, set up the writer
        try( FileWriter fw = new FileWriter( outF ) ){

            //System.out.println("Starting to write "+outF);
            out = new BufferedWriter( fw );
            //System.out.println("Created "+outF);

            try{
                Assignments<Variable,Value> pas = edgar.piAssignments(startingNode);
                List<Variable> orderedParents = edgar.getOrderedParents(startingNode);

                for( Value x : edgar.network.values(startingNode) ){
                    //System.out.println("Starting process for "+startingNode+" = "+x);
                    run( startingNode, x, pas, orderedParents, Collections.EMPTY_MAP );
                } // x loop

            } finally {
                out.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.out.println(outF.getAbsolutePath()+" written.");
    }

    private void run( Variable node, Value x, Assignments<Variable,Value> pas, List<Variable> orderedParents, Map<Variable,Value> conditions ) throws IOException{
        if( edgar.marginalTest( node, x, conditions ) ){
            reportMarginal( node, x, conditions );

            if (!orderedParents.isEmpty())
                for (Map<Variable,Value> pa : pas)
                    if (!edgar.conflict( pa, conditions )){
                        reportJoint( node, x, pa, conditions );
                        reportConditional( node, x, pa, conditions );
                        reportJoint( pa, conditions );

                        Map<Variable,Value> newConditions = new HashMap<>( conditions );
                        for (Variable parent : orderedParents) {
                            Value y = pa.get(parent);

                            run( parent, y, edgar.piAssignments(parent), edgar.getOrderedParents( parent ), edgar.cleanup( parent, newConditions ) );

                            if( !newConditions.containsKey(parent) )
                                newConditions.put(parent, y);
                        }
                    }
        }
    }

    private void reportMarginal(Variable node, Value x, Map<Variable,Value> conditions) throws IOException {
        Map<Variable,Value>
                c0 = EDGrLogic.union( conditions, edgar.z0map ),
                c1 = EDGrLogic.union( conditions, edgar.z1map );
        double
                p0 = edgar.network.probability( Collections.singletonMap(node,x), c0),
                p1 = edgar.network.probability( Collections.singletonMap(node,x), c1);

        out
                .append("P(").append(node.toString())
                .append('=').append(x.toString())
                .append('|').append(conditions.toString())
                .append(',').append(edgar.z1map.toString())
                .append(") = ").append(Double.toString(p1));
        out.newLine();
        out
                .append("P(").append(node.toString())
                .append('=').append(x.toString())
                .append('|').append(conditions.toString())
                .append(',').append(edgar.z0map.toString())
                .append(") = ").append(Double.toString(p0));
        out.newLine();
        out.append("Quotient = ").append(Double.toString(p1/p0));
        out.newLine();
        out.append("Difference = ").append(Double.toString(p1-p0));
        out.newLine();
        out.newLine();
    }

    private void reportConditional(Variable node, Value x, Map<Variable, Value> pa, Map<Variable,Value> conditions) throws IOException {
        Map<Variable,Value>
                c0 = EDGrLogic.union( pa, conditions, edgar.z0map ),
                c1 = EDGrLogic.union( pa, conditions, edgar.z1map );
        double
                p0 = edgar.network.probability( Collections.singletonMap(node,x), c0),
                p1 = edgar.network.probability( Collections.singletonMap(node,x), c1);

        out
                .append("P(").append(node.toString())
                .append('=').append(x.toString())
                .append('|').append(pa.toString())
                .append(',').append(conditions.toString())
                .append(',').append(edgar.z1map.toString())
                .append(") = ").append(Double.toString(p1));
        out.newLine();
        out
                .append("P(").append(node.toString())
                .append('=').append(x.toString())
                .append('|').append(pa.toString())
                .append(',').append(conditions.toString())
                .append(',').append(edgar.z0map.toString())
                .append(") = ").append(Double.toString(p0));
        out.newLine();
        out.append("Quotient = ").append(Double.toString(p1/p0));
        out.newLine();
        out.newLine();
    }

    private void reportJoint(Map<Variable, Value> pa, Map<Variable,Value> conditions) throws IOException {
        try {
            Map<Variable,Value>
                    c0 = EDGrLogic.union( conditions, edgar.z0map ),
                    c1 = EDGrLogic.union( conditions, edgar.z1map );
            double
                    p0 = edgar.network.probability( pa, c0),
                    p1 = edgar.network.probability( pa, c1);

            out
                    .append("P(").append(pa.toString())
                    .append('|').append(conditions.toString())
                    .append(',').append(edgar.z1map.toString())
                    .append(") = ").append(Double.toString(p1));
            out.newLine();
            out
                    .append("P(").append(pa.toString())
                    .append('|').append(conditions.toString())
                    .append(',').append(edgar.z0map.toString())
                    .append(") = ").append(Double.toString(p0));
            out.newLine();
            out.append("Quotient = ").append(Double.toString(p1/p0));
            out.newLine();
            out.newLine();
        }catch (RuntimeException e){
            System.err.println(pa.toString());
            System.err.println(conditions.toString());
            throw e;
        }
    }

    private void reportJoint(Variable node, Value x, Map<Variable, Value> pa, Map<Variable,Value> conditions) throws IOException {
        try {
            Map<Variable,Value>
                    o = EDGrLogic.union( Collections.singletonMap(node, x), pa ),
                    c0 = EDGrLogic.union( conditions, edgar.z0map ),
                    c1 = EDGrLogic.union( conditions, edgar.z1map );
            double
                    p0 = edgar.network.probability( o, c0),
                    p1 = edgar.network.probability( o, c1);

            out
                    .append("P(").append(node.toString())
                    .append('=').append(x.toString())
                    .append(',').append(pa.toString())
                    .append('|').append(conditions.toString())
                    .append(',').append(edgar.z1map.toString())
                    .append(") = ").append(Double.toString(p1));
            out.newLine();
            out
                    .append("P(").append(node.toString())
                    .append('=').append(x.toString())
                    .append(',').append(pa.toString())
                    .append('|').append(conditions.toString())
                    .append(',').append(edgar.z0map.toString())
                    .append(") = ").append(Double.toString(p0));
            out.newLine();
            out.append("Difference = ").append(Double.toString(p1-p0));
            out.newLine();
            out.append("Quotient = ").append(Double.toString(p1/p0));
            out.newLine();
            out.newLine();
        }catch (RuntimeException e){
            System.err.println(pa.toString());
            System.err.println(conditions.toString());
            throw e;
        }
    }
}