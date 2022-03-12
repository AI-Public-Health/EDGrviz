/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.isp.sverchkov.statistics;

import edu.pitt.isp.sverchkov.collections.ArrayTools;
import edu.pitt.isp.sverchkov.math.MathTools;

/**
 * Class for performing Fisher's exact test
 * @author YUS24
 */
public class FishersExactTest {
    
    public static double pValue( int[][] counts ){ // Defaults to two-tailed
        
        final int rows, cols;
        {
            int[] dim = ArrayTools.rectangularDimensions(counts);
            rows = dim[0];
            cols = dim[1];
        }
        
        if( rows < 2 || cols < 2 )
            throw new IllegalArgumentException("A contingency table cannot be "+rows+"x"+cols+".");

        /* The direct 2x2 test I used actually turned out to be less accurate (according to SAS) than the Metha-Patel algorithm
        if( rows == 2 && cols == 2 ) // 2x2 case computed directly
            return test( counts[0][0], counts[0][1], counts[1][0], counts[1][1], true );
        */
        
        if( rows < cols ){ // Metha-Patel favors having more rows
            counts = ArrayTools.transpose( counts );
        }
        
        return MethaPatelTest.pValue( counts );
    }
}
