/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.pitt.isp.sverchkov.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author yus24
 */
public class MutableDAGImpl<Node> implements DAG<Node> {
    
    public final List<Node> nodes;
    private final List<Node> modifiableNodes;
    private List<BitSet> parentSets;
    
    public MutableDAGImpl() {
        modifiableNodes = new ArrayList<>();
        nodes = Collections.unmodifiableList( modifiableNodes );
        parentSets = new ArrayList<>();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public List<Node> parents(Node node) {
        int[] parents = parents( nodes.indexOf(node) );
        List<Node> result = new ArrayList<>( parents.length );
        for( int parent : parents )
            result.add( nodes.get(parent) );
        return result;
    }
    
    private int[] parents( int node ) {
        BitSet parents = parentSets.get(node);
        int[] result = new int[parents.cardinality()];
        
        for( int i = parents.nextSetBit(0), j = 0; i >= 0; i = parents.nextSetBit(i+1), j++ )
            result[j] = i;
        
        return result;
    }

    @Override
    public Iterator<Node> iterator() {
        return modifiableNodes.iterator();
    }
    
    private static BitSet dropBit( int bit, BitSet set ){
        BitSet right = set.get(0, bit);
        BitSet left = set.get(1, set.length());
        left.clear(0, bit);
        left.or( right );
        return left;
    }
    
    public void addNode( Node node ){
        modifiableNodes.add(node);
        parentSets.add( new BitSet() );
    }
    
    public void removeNode( Node node ){
        int n = modifiableNodes.indexOf(node);
        modifiableNodes.remove(n);
        parentSets.remove(n);
        for( BitSet bs : parentSets ) dropBit(n, bs);
    }
    
    public void addArc( Node parent, Node child ){
        int p, c;
        while( (p = nodes.indexOf( parent )) < 0 )
            addNode( parent );
        while( (c = nodes.indexOf( child )) < 0 )
            addNode( child );
        parentSets.get(c).set(p);
    }
    
    public void removeArc( Node parent, Node child ){
        int
                p = nodes.indexOf( parent ),
                c = nodes.indexOf( child );
        if( p > 0 && c > 0 ) parentSets.get(c).clear(p);
    }
}
