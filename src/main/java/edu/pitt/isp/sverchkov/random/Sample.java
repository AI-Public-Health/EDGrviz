package edu.pitt.isp.sverchkov.random;

import edu.pitt.isp.sverchkov.math.MathTools;
import java.util.*;

/**
 *
 * @author YUS24
 */
public class Sample {
    
    /**
     * Random sampling of a combination of k objects out of a list.
     * Based on Robert Floyd's algorithm.
     * @param <T> The type of the objects.
     * @param collection The list from which to sample.
     * @param k The number of objects to sample.
     * @param random The RNG object.
     * @return A new collection containing k objects from the list.
     */
    public static <T> Collection<T> combination( List<T> collection, int k, Random random ){
        
        final int n = collection.size();
        
        if( n <= k ) return collection;
        
        Set<T> result = new HashSet();
        for( int j = n - k; j < n; j++ ){
            
            final T item = collection.get( random.nextInt( j ) );
            
            if( result.contains( item ) )
                result.add( collection.get(j) );
            else
                result.add( item );
        }            
        return result;
    }
    
    public static <T> Collection<T> combination( List<T> collection, int k ){
        return combination( collection, k, new Random() );
    }

    public static double[] randomUniform( double[] resultArray, final int k, Random random ){
        if( k < 2 ) return resultArray;
        if( null == random ) random = new Random();
        if( resultArray == null || resultArray.length < k )
            resultArray = new double[k];
        for( int i=0; i < k-1; i++ )
            resultArray[i] = random.nextDouble()/k;
        resultArray[k-1] = 1 - MathTools.sum( resultArray, 0, k-1 );
        
        return resultArray;
    }
    
    public static double[] randomUniform( double[] resultArray, final int k ){
        return randomUniform( resultArray, k, null );
    }
    
    public static double[] randomUniform( double[] resultArray, Random random ){
        if( null == resultArray ) return null;
        return randomUniform( resultArray, resultArray.length, random );
    }
    
    public static double[] randomUniform( double[] resultArray ){
        return randomUniform( resultArray, null );
    }
    
    public static double[] randomUniform( final int k, Random random ){
        return randomUniform( null, k, random );
    }
    
    public static double[] randomUniform( final int k ){
        return randomUniform( null, k, null );
    }
}
