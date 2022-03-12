package edu.pitt.isp.sverchkov.cn;

import edu.pitt.isp.sverchkov.data.ADTree;
import edu.pitt.isp.sverchkov.data.CategoricalData;
import edu.pitt.isp.sverchkov.data.DataTable;
import edu.pitt.isp.sverchkov.graph.AbstractValueDAG;
import edu.pitt.isp.sverchkov.graph.DAG;
import edu.pitt.isp.sverchkov.graph.SimpleDAGImpl;
import java.util.*;

public class CountNetImpl<N,V> extends AbstractValueDAG<N,V> implements CountNet<N,V> {

    private DAG<N> dag;
    private CategoricalData<N,V> counts;

    public CountNetImpl( CategoricalData<N,V> counts, DAG<N> structure ){
        this.counts = counts;
        dag = structure;
    }

    public CountNetImpl( DataTable<N,V> data, DAG<N> structure ){
        counts = new ADTree( data );
        dag = new SimpleDAGImpl( structure );
    }

    @Override
    public Collection<V> values(N node) {
        return counts.values(node);
    }

    @Override
    public Map<V, Integer> counts(N node, Map<N, V> conditions) {
        return counts.counts(node, conditions);
    }

    @Override
    public int size() {
        return dag.size();
    }

    @Override
    public Collection<N> parents(N node) {
        return dag.parents(node);
    }

    @Override
    public Iterator<N> iterator() {
        return dag.iterator();
    }





}
