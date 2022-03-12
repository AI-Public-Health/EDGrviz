package edu.pitt.isp.sverchkov.edgr;

import edu.pitt.isp.sverchkov.bn.BayesNet;
import edu.pitt.isp.sverchkov.combinatorics.Assignments;
import edu.pitt.isp.sverchkov.graph.Ancestry;
import edu.pitt.isp.sverchkov.graph.DAG;
import edu.pitt.isp.sverchkov.graph.GraphTools;
import edu.pitt.isp.sverchkov.graph.MutableValueDAGImpl;
import edu.pitt.isp.sverchkov.graph.SimpleDAGImpl;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public class EDGrLogic<Variable,Value> {

    public final BayesNet<Variable,Value> network;
    public final Variable contrast;
    private final Ancestry ancestors;
    public final List<Variable> topOrder;
    public final Map<Variable,Value> z0map, z1map;
    public final double dThreshold;
    public final double lnqThreshold;

    public EDGrLogic( BayesNet<Variable,Value> net, Variable z ){
        this( net, z, 0.1 );
    }

    public EDGrLogic( BayesNet<Variable,Value> net, Variable z, double threshold ){
        network = net;
        contrast = z;
        MutableValueDAGImpl<Variable,Value> tmp = new MutableValueDAGImpl<>( net );
        tmp.removeNode( z );
        ancestors = new Ancestry( tmp );
        topOrder = Collections.unmodifiableList( GraphTools.nodesInTopOrder(net) );

        // Z = 0,1 maps:
        {
            Iterator<Value> iter = network.values(contrast).iterator();
            z0map = Collections.singletonMap(contrast, iter.next());
            z1map = Collections.singletonMap(contrast, iter.next());
        }

        dThreshold = threshold;
        lnqThreshold = -Math.log1p(-threshold);
        System.out.println("Using thresholds "+dThreshold+" and "+lnqThreshold);
    }

    public boolean doWeRun(Variable node) {
        for( Value x : network.values(node) )
            if( marginalTest( node, x ) )
                return true;
        return false;
    }

    public boolean marginalTest(Variable node, Value x){
        return marginalTest( node, x, Collections.EMPTY_MAP );
    }

    public boolean marginalTest(Variable node, Value x, Map<Variable,Value> conditions) {
        return dThreshold < marginalDelta( node, x, conditions );
    }

    public double marginalDelta(Variable node, Value x, Map<Variable,Value> conditions) {
        Map<Variable,Value> xMap = Collections.singletonMap(node, x);
        return Math.abs(
                network.probability(xMap, union(conditions, z1map)) -
                        network.probability(xMap, union(conditions, z0map)) );
    }

    public boolean jointTest( Map<Variable,Value> pa, Map<Variable,Value> conditions ) {
        return dThreshold < jointLnQ(pa, conditions);
    }

    public double jointLnQ(Map<Variable, Value> pa, Map<Variable, Value> conditions) {
        return Math.abs(
                Math.log( network.probability(pa, union(conditions, z1map)) ) -
                        Math.log( network.probability(pa, union(conditions, z0map)) ) );
    }

    public double jointP(Variable node, Value x, Map<Variable,Value> pa, Map<Variable,Value> conditions, Value zVal ){
        Map<Variable,Value>
                o = EDGrLogic.union( Collections.singletonMap(node, x), pa ),
                c = EDGrLogic.union( conditions, Collections.singletonMap(contrast, zVal) );
        return network.probability( o, c );
    }

    public double jointP(Map<Variable,Value> pa, Map<Variable,Value> conditions, Value zVal ){
        Map<Variable,Value>
                c = EDGrLogic.union( conditions, Collections.singletonMap(contrast, zVal) );
        return network.probability( pa, c );
    }

    public boolean separable( Variable child, Variable parent ){
        final List<Variable> parents = new ArrayList<>( network.parents(child) );
        parents.remove(parent);
        return ancestors.haveCommonAncestors(Collections.singleton(parent), parents);
    }

    public List<List<Variable>> partitionParents( Variable node ){
        return ancestors.partitionByCommonAncestry( network.parents(node) );
    }

    public List<Variable> children( Variable node ){
        List<Variable> result = new ArrayList<>();
        for( Iterator<Variable> iter = topOrder.listIterator( topOrder.indexOf(node)+1 ); iter.hasNext(); )
            if (network.parents(iter.next()).contains(node))
                result.add(node);
        return result;
    }

    /**
     * "Pi" assignments: the possible assignments of non-z parents of node
     * @param node
     * @return an Assignments object
     */
    public Assignments<Variable,Value> piAssignments( Variable node ){
        Assignments<Variable,Value> result;

        Collection<Variable> parents = network.parents(node);

        if (parents.contains(contrast)) {
            Map<Variable,Collection<Value>> valueMap = new HashMap<>( parents.size()-1 );
            for (Variable parent : parents) if (!contrast.equals(parent))
                valueMap.put(parent, network.values(parent));
            result = new Assignments<>(valueMap);
        }else
            result = network.parentAssignments(node);

        return result;
    }

    static <K,V> Map<K,V> union( Map<K,V>... maps ){
        int size = 0;
        for (Map<K,V> map : maps) size += map.size();
        Map<K,V> result = new HashMap<>(size);
        for (Map<K,V> map : maps) result.putAll(map);
        return result;
    }

    public List<Variable> getOrderedParents(Variable node) {
        Collection<Variable> parents = network.parents(node);
        List<Variable> orderedParents = new ArrayList<>(parents.size());
        for( Variable v : topOrder )
            if (parents.contains(v) && !contrast.equals(v)) orderedParents.add(v);
        return orderedParents;
    }

    public static <Variable,Value> boolean conflict(Map<Variable, Value> pa, Map<Variable, Value> conditions) {
        Set<Variable> conflicts = new HashSet<>( conditions.keySet() );
        conflicts.retainAll( pa.keySet() );
        for (Variable v : conflicts)
            if (!pa.get(v).equals(conditions.get(v))) return true;
        return false;
    }

    public Map<Variable, Value> cleanup(Variable node, Map<Variable, Value> conditions) {
        final int n = conditions.size();
        Map<Variable, Value> newConditions = new HashMap<>( n );

        Queue<Variable>
                qu = new LinkedList<>(),
                qd = new LinkedList<>();
        qu.offer(node);
        qd.offer(node);

        Set<Variable>
                vu = new HashSet<>(),
                vd = new HashSet<>();

        while (!qu.isEmpty() || !qd.isEmpty() && newConditions.size() < n ) {
            {// Uppers
                Variable up=qu.poll();
                if (null!=up && vu.add(up))
                    for (Variable p : network.parents(up))
                        if( conditions.containsKey(p) )
                            newConditions.put( p, conditions.get(p) );
                        else{
                            qu.offer(p);
                            qd.offer(p);
                        }
            }
            {// Downers
                Variable down=qd.poll();
                if (null!=down && vd.add(down))
                    for (Variable c : children(down))
                        if( conditions.containsKey(c) ){
                            newConditions.put( c, conditions.get(c) );
                            qu.offer(c);
                        }else{
                            qd.offer(c);
                        }
            }
        }
        //System.out.println(conditions.toString() + " => " + newConditions.toString());
        return newConditions;
    }
}