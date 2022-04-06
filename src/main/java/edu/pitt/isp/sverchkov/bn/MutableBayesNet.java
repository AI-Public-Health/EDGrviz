package edu.pitt.isp.sverchkov.bn;

import java.util.List;
import java.util.Map;

/*author*
 *
 * @author YUS24
 */



    public interface MutableBayesNet<N,V> extends BayesNet<N,V> {
        void setCPT( N node, Map<N,V> parentAssignment, Map<V,Double> conditionalProbabilities );
        void addNode( N node, List<V> values );
        void addArc( N parent, N child );
        void removeArc(N parent, N child);
    }

