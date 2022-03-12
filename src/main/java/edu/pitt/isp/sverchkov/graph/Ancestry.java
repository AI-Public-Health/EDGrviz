/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.pitt.isp.sverchkov.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author yus24
 * @param <Node>
 */
public class Ancestry<Node> {
    private final List<Node> nodes;
    
    /* bits stores the ancestry map of nodes
     * node 0 has no ancestors (by construction)
     * node 1 has 1 possible ancestor (node 0, bit 0 indicates if it is an ancestor)
     * node n has n possible ancestors (nodes 0 through (n-1), bits n*(n-1)/2 up to (excluding) n*(n+1)/2
     */
    private final BitSet bits;
    
    public Ancestry( DAG<Node> graph ){
        nodes = GraphTools.nodesInTopOrder( graph );
        final int n = nodes.size();
        bits = new BitSet( (n * (n - 1)) >> 1 );
        
        for( int child = 0, bit = 0; child < n; child++ ){
            Node c = nodes.get(child);
            Collection<Node> parents = graph.parents(c);

            for( int parent = 0; parent < child; parent++, bit++ ){
                if( parents.contains( nodes.get(parent) ) )
                    bits.set(bit);
            }
        }
        
        for( int child = 0; child < n; child++ ){
            BitSet parents = ancestors( child );
            for( int parent = 0; parent < child; parent++ ) if( parents.get(parent) ){
                addAncestors( child, ancestors( parent ) );
            }
        }
    }
    
    private BitSet ancestors( int node ){
        return bits.get( (node * (node-1)) >> 1, (node * (node + 1)) >> 1 );
    }
    
    private void addAncestors( int node, BitSet toAdd ){
        final int start = ( node * (node-1) ) >> 1;
        for( int i = toAdd.nextSetBit(node); i >= 0 && i < toAdd.length(); i++ )
            if( toAdd.get(i) ) bits.set(start+i);
    }
    
    private boolean haveCommonAncestors( int[][] groups ){
        final int n = groups.length;
        BitSet theSet = new BitSet();
        for( int i=0; i<n; i++ ){
            BitSet toCompare = new BitSet();
            for( int group : groups[i] )
                toCompare.or( ancestors(group) );
            if( theSet.intersects( toCompare ) ) return true;
            theSet.or( toCompare );
        }     
        return false;   
    }
    
    public boolean haveCommonAncestors( Collection<Node>... nodeSets ){
        return haveCommonAncestors( Arrays.asList(nodeSets) );
    }
    
    public boolean haveCommonAncestors( Collection<Collection<Node>> nodeSets ){
        final int[][] groups = new int[nodeSets.size()][];
        
        int i = 0;
        for( Collection<Node> group : nodeSets ){
            groups[i] = new int[group.size()];
            int j = 0;
            for( Node n : group ){
                groups[i][j++] = nodes.indexOf(n);
            }
            ++i;
        }
        
        return haveCommonAncestors( groups );
    }
    
    private int[][] partitionByCommonAncestry( int[] nodes ){
        
        final int n = nodes.length;
        
        // Setup
        int[] markers = new int[n];
        List<List<Integer>> result = new ArrayList<>();
        for( int i = 0; i<n; i++ ){
            List<Integer> l = new ArrayList<>();
            l.add( i );
            result.add( l );
            markers[i] = i;
        }
        
        // Run connected component algo
        for( int i=0; i<n; i++ )
            for( int j=i+1; j<n; j++ )
                if(
                        markers[i] != markers[j] && (
                        ancestors( nodes[i] ).intersects( ancestors( nodes[j] ) ) ||
                        isAncestor( nodes[i], nodes[j] ) ) )
                {
                    // Put bigger-indexed into smaller-indexed
                    final List<Integer> jList = result.get(markers[j]);
                    result.get(markers[i]).addAll(jList);
                    for( int k : jList ) markers[k] = markers[i];
                    jList.clear();                    
                }
        
        // Clean
        for( Iterator<List<Integer>> iter = result.iterator(); iter.hasNext(); )
            if( iter.next().isEmpty() ) iter.remove();
        
        // Dump into array
        int[][] r = new int[result.size()][];
        for( int i=0; i<r.length; i++ ){
            final int m = result.get(i).size();
            r[i] = new int[m];
            for( int j = 0; j<m; j++ )
                r[i][j] = nodes[result.get(i).get(j)];        
        }
        return r;
    }
    
    public List<List<Node>> partitionByCommonAncestry( Collection<Node> nodeset ){
        int[] nodeindeces = new int[nodeset.size()];
        for( int i=0, j=0; j<nodeindeces.length && i<nodes.size(); i++ )
            if( nodeset.contains(nodes.get(i)) ) nodeindeces[j++] = i;
        
        int[][] partitions = partitionByCommonAncestry( nodeindeces );
        
        List<List<Node>> result = new ArrayList<>( partitions.length );
        for( int[] partition : partitions){
            List<Node> set = new ArrayList<>(partition.length);
            for( int index : partition )
                set.add( nodes.get(index) );
            result.add(set);
        }
        return result;
    }
    
    private int bitsOffset( int node ){
        return ( node * ( node - 1 ) ) >> 1;
    }
    
    /*private int bitRangeEnd( int node ){
        return ( node * ( node - 1 ) ) >> 1;
    }*/

    private boolean isAncestor(int ancestor, int descendant) {
        return ancestor < descendant && bits.get( bitsOffset( descendant ) + ancestor );
    }
}
