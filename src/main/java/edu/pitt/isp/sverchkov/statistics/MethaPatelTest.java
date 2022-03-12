/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.statistics;

import edu.pitt.isp.sverchkov.collections.ArrayTools;
import edu.pitt.isp.sverchkov.collections.Pair;
import edu.pitt.isp.sverchkov.math.MathTools;
import java.util.*;

/**
 * Implementation of Metha and Patel's network algorithm for Fisher's exact test for nxm contingency tables.
 * @author YUS24
 */
public class MethaPatelTest {
    
    private final int[] cols, rows;
    private final State root;
    private final double normalization; // The normalizing constant prod_r( R_r! )/T.
    private final Map<State,List<State>> network;
    private final Map<State,Pair<Double,Double>> bounds; // (shortest, longest) paths from a node

    /*public MethaPatelTest( int[][] counts ){
        int[] dim = ArrayTools.rectangularDimensions( counts );
        rows = new int[dim[0]];
        cols = new int[dim[1]];
        
        for( int r = 0; r < dim[0]; r++ )
            for( int c = 0; c < dim[1]; c++ ){
                rows[r] += counts[r][c];
                cols[c] += counts[r][c];
            }
        
        network = new HashMap<>();
        bounds = new HashMap<>();
        root = generateStates( cols.length, rows );
        computeBounds();
    }*/
    
    /**
     * Creates an instance of the testing class, automatically generating the
     * network and calculating path length bounds at creation time.
     * Each instance is bound to a particular set of marginals.
     * The same instance can be used to get the p-values for multiple tables
     * that have the same marginals.
     * @param rowTotals row marginal counts
     * @param colTotals column marginal counts
     */
    public MethaPatelTest( int[] rowTotals, int[] colTotals ){
        
        // Set cols and rows (copy to defend against modification)
        cols = new int[colTotals.length];
        rows = new int[rowTotals.length];
        System.arraycopy(colTotals, 0, cols, 0, colTotals.length);
        System.arraycopy(rowTotals, 0, rows, 0, rowTotals.length);
        
        // Collection initialization
        network = new HashMap<>();
        bounds = new HashMap<>();
        
        {// Compute normalizing constant
            double n = -MathTools.lnFactorial( MathTools.sum( rows ) ); // 1/T!
            for( int r : rows )
                n += MathTools.lnFactorial( r ); // prod_r( R_r! )
            normalization = n;
        }

        // Generate network and compute path bounds
        root = generateStates( cols.length, rows );
        computeBounds();
    }
    
    /**
     * Returns the two-sided Fisher's exact test p-value for the contingency
     * table defined by the 2-d array counts.
     * This is equivalent to calling
     * <tt> new MethaPatelTest( rows, cols ).getPValue( counts ) </tt>
     * with <tt>rows, cols</tt> being the row and column marginal counts.
     * @param counts the contingency table
     * @return the p-value
     */
    public static double pValue( int[][] counts ){
        int[]
                dim = ArrayTools.rectangularDimensions( counts ),
                rows = new int[dim[0]],
                cols = new int[dim[1]];
        
        for( int r = 0; r < dim[0]; r++ )
            for( int c = 0; c < dim[1]; c++ ){
                rows[r] += counts[r][c];
                cols[c] += counts[r][c];
            }
        
        return new MethaPatelTest( rows, cols ).getPValue( counts );
    }
    
    /**
     * Returns the two-sided Fisher's exact test p-value for the contingency
     * table defined by the 2-d array counts.
     * @param counts the contingency table
     * @return the p-value
     */
    public double getPValue( int[][] counts ){
        // Validate
        {
            final int[]
                    rSum = new int[rows.length],
                    cSum = new int[cols.length],
                    dim = ArrayTools.rectangularDimensions( counts );
            
            if( dim[0] != rows.length || dim[1] != cols.length )
                throw new IllegalArgumentException("Given contingency table has the wrong dimensions.");
            
            for( int r = 0; r < dim[0]; r++ )
                for( int c = 0; c < dim[1]; c++ ){
                    rSum[r] += counts[r][c];
                    cSum[c] += counts[r][c];
                }
            
            if( !Arrays.equals(rSum, rows) || !Arrays.equals(cSum, cols) )
                throw new IllegalArgumentException("Given contingency table has different marginal counts.");
            
        }
        
        // Get path length
        final double pathLength = pathLength( counts );
        
        // Breadth-first, prune anything exceeding the target length, sum the rest.
        final Queue<Pair<Double,State>> queue = new LinkedList<>();
        queue.add( new Pair<>( 0.0, root ) );
        double lnP = Double.NEGATIVE_INFINITY;
        
        for(Pair<Double,State> p; null != ( p = queue.poll() );){
            final double path = p.first;
            final State node = p.second;
            final Pair<Double,Double> bnds = bounds.get( node );
            // Upperbound test
            if( path + bnds.second <= pathLength ) // Include all paths from here
                lnP = MathTools.lnXplusY( lnP, path + sumOfAllPathsFrom( node ) );
            // Lowerbound test
            else
            if( path + bnds.first <= pathLength ) // Then we have to look at children
                
                if( network.get(node) == null ) lnP = MathTools.lnXplusY( lnP, path ); // We reached the end :)
                else for( State child : network.get( node ) )
                    queue.add( new Pair( edgeCost( node, child ) + path, child ) );
        }
        
        return Math.exp( lnP+normalization );
    }
    
    /**
     * Computes the cost of an edge from the source to the destination, ln( C_k! / prod_r( ( sourceR_r - destinationR_r )! ) )
     * @param source
     * @param destination
     * @return the edge cost.
     */
    private double edgeCost( State source, State destination ){
        double cost = MathTools.lnFactorial( cols[destination.k] );
        for( int i=0; i<source.r.length; i++ )
            cost -= MathTools.lnFactorial( source.r[i] - destination.r[i] );
        return cost;
    }
    
    /**
     * Uses Metha and Patel's formula for computing the sum of all paths from a state.
     * @param state
     * @return the sum of all path lengths.
     */
    private double sumOfAllPathsFrom( State state ){
        int sum = 0;
        for( int i = 0; i < state.k; i++ )
            sum += cols[i];
        double cost = MathTools.lnFactorial( sum );
        for( int ri : state.r )
            cost -= MathTools.lnFactorial( ri );
        return cost;
    }

    /**
     * Uses dynamic programming to compute the longest and shortest paths from each
     * state in the network.
     */
    private void computeBounds(){
        
        // Arrange nodes by layers
        final Map<Integer,List<State>> layers = new HashMap<>();
        for( State node : network.keySet() ){
            List<State> list = layers.get( node.k );
            if( list == null ){
                list = new ArrayList<>();
                layers.put( node.k, list );
            }
            list.add( node );
        }
        
        for( State node : layers.get(0) )
            bounds.put( node, new Pair<>( 0.0, 0.0 ) );
        
        for( int k = 1; k <= cols.length; k++ )
            for( State node : layers.get(k) ){
                double
                        lower = Double.POSITIVE_INFINITY,
                        upper = Double.NEGATIVE_INFINITY;
                for( State child : network.get( node ) ){
                    final double edge = edgeCost( node, child );
                    final Pair<Double,Double> bnds = bounds.get( child );
                    lower = Math.min( lower, edge + bnds.first );
                    upper = Math.max( upper, edge + bnds.second );
                }
                bounds.put( node, new Pair<>(lower,upper) );
            }
    }
    
    /**
     * Computes
     * <i>log( prod_c( C_c! / prod_r( x_rc! ) ) )</i>
     * the (log) path length in the algorithm for the specific contingency table
     * @param counts the contingency table
     * @return the path length
     */
    private double pathLength( int[][] counts ){
        // Do not rearrange this code (e.g. by changing the loop order or eliminating 'term.'
        // This exact order of operations is necessary to match the exact order of operations
        // in the network's path calculation so that this table will be properly included in
        // the equality comparisons.
        double l = 0;
        for( int c = cols.length-1; c >= 0; c-- ){         
            double term = MathTools.lnFactorial( cols[c] ); // prod_c( C_c! )
            for( int r = 0; r < rows.length; r++ )
                term -= MathTools.lnFactorial( counts[r][c] ); // prod_rc( 1 / x_rc! )
            l += term; // Again, DO NOT REARRANGE.
        }
        return l;
    }
    
    /**
     * Generates the state defined by k,r and its successors (if it and they don't already exist in the network).
     * @param k network level
     * @param r the partial row totals array
     * @return the state k:r after generating all its successors (if needed).
     */
    private State generateStates( int k, int[] r ){
        State node = new State( k, r );
        if( !network.containsKey( node ) ){
            if( k <= 0 ){
                // Sanity check
                for( int ri : r ) if( ri != 0 ) throw new RuntimeException("Metha-Patel network construction error: k=0, but R_i = "+ri+".");
                
                network.put(node,null);                
            }else{
                List<State> next = new ArrayList<>();
                network.put(node,next);
                // Give rows[k-1] pieces of candy to r girls:
                int[] rp = new int[r.length];
                System.arraycopy(r, 0, rp, 0, r.length);
                addNodes( rp, cols[k-1], r.length-1, k-1, next );
            }
        }
        return node;
    }

    /**
     * Helper for generating successor nodes
     * @param rp partial row totals at this stage
     * @param candy number of column counts to distribute at this stage.
     * The metaphor here is that this is like distributing cols[k] candy to |rows| girls.
     * @param i row number to which we are distributing
     * @param k network level (and column number)
     * @param next the array to which successor nodes are added.
     */
    private void addNodes( int[] rp, int candy, int i, int k, List<State> next ){
        if( i == 0 ){ // Last girl, give her the rest of the candy.
            if( rp[i] >= candy ){ // Don't overfeed her
                int[] rn = new int[rp.length];
                System.arraycopy( rp, 0, rn, 0, rp.length );
                rn[i] = rp[i] - candy; // Give candy!
                next.add( generateStates( k, rn ) );
            }

        } else {
            // Save capacity, compute how many candies we can give
            final int rpi = rp[i], max = Math.min( rp[i], candy );

            // Give her from 0 to max candies
            for( int c = 0; c <= max; c++, rp[i]-- ){
                addNodes( rp, candy-c, i - 1, k, next ); // Candy for the next girl
            }

            // Reload capacity
            rp[i] = rpi;
        }
    }
  
    /**
     * Class representing a state/node in the network
     */
    private static class State {
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final State other = (State) obj;
            if (this.k != other.k) {
                return false;
            }
            if (!Arrays.equals(this.r, other.r)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.k;
            hash = 97 * hash + Arrays.hashCode(this.r);
            return hash;
        }
        
        final int k;
        final int[] r;
        
        State( final int k, final int[] r ){
            this.k = k;
            this.r = r;
        }
        
        @Override
        public String toString(){
            return k+": "+Arrays.toString(r);
        }
    }
}
