package edu.pitt.isp.sverchkov.data;

import java.util.*;

/**
 *
 * @author YUS24
 */
public class DataTableImpl<N,V> implements DataTable<N,V>, CategoricalData<N,V> {

    private final List<N> variables;
    private final List<List<V>> rows;

    public DataTableImpl( List<? extends N> vars ){
        variables = Collections.unmodifiableList( new ArrayList<>( vars ) );
        rows = new ArrayList<>();
    }

    @Override
    public List<N> variables() {
        return variables;
    }

    @Override
    public int columnCount() {
        return variables.size();
    }

    @Override
    public int rowCount() {
        return rows.size();
    }

    @Override
    public void addRow(List<? extends V> row) {
        final int
                m = row.size(),
                w = columnCount();

        if( m != w )
            throw new IllegalArgumentException( "Tried to insert a row of length "+m+" into a table of width "+w+"." );

        rows.add( Collections.unmodifiableList( new ArrayList<>( row ) ) );
    }

    @Override
    public Iterator<List<V>> iterator() {
        return Collections.unmodifiableList( rows ).listIterator();
    }

    @Override
    public int count(Map<N, V> assignment) {

        boolean[] matches = new boolean[rows.size()];
        for( int r=0; r < matches.length; r++ )
            matches[r] = true;

        for( Map.Entry<N,V> entry : assignment.entrySet() )
            for( int r = 0; r < matches.length; r++ )
                if( matches[r] && !rows.get(r).get(variables.indexOf(entry.getKey())).equals(entry.getValue()) )
                    matches[r] = false;

        int count = 0;

        for( int r=0; r<matches.length; r++ )
            if( matches[r] ) ++count;

        return count;
    }

    @Override
    public Map<V, Integer> counts(N attribute, Map<N, V> assignment) {
        int[] matches = new int[rows.size()];
        int attr = variables.indexOf( attribute );
        List<V> values = values( attribute );
        for( int r = 0; r < matches.length; r++ )
            matches[r] = values.indexOf( rows.get(r).get(attr) );

        for( Map.Entry<N,V> entry : assignment.entrySet() )
            for( int r = 0; r < matches.length; r++ )
                if( matches[r] >= 0 && !rows.get(r).get(variables.indexOf(entry.getKey())).equals(entry.getValue()) )
                    matches[r] = -1;

        Map<V,Integer> result = new HashMap<>();
        for( V value : values ) result.put( value, 0 );
        for( int r = 0; r < matches.length; r++ ) if(matches[r] >= 0) {
            V value = values.get(matches[r]);
            result.put( value, result.get(value) + 1 );
        }

        return result;
    }

    @Override
    public List<V> values(N attribute) {
        List<V> result = new ArrayList<>();
        int i = variables.indexOf( attribute );
        for( List<V> row : rows )
            if( !result.contains( row.get(i) ) ) result.add( row.get(i) );
        return result;
    }

    @Override
    public List<CategoricalData<N, V>> split(N attribute) {
        List<V> values = values( attribute );
        List<CategoricalData<N,V>> result = new ArrayList<>( values.size() );
        for( V value : values )
            result.add( DataTools.filter(this, attribute, value) );
        return result;
    }

    @Override
    public String toString(){
        final String newLine = System.getProperty("line.separator");
        StringBuilder str = new StringBuilder('\n');
        for( N attribute : variables )
            str.append(attribute.toString()).append(", ");
        str.append(newLine);
        for( List<V> row : this ){
            for( V value : row )
                str.append(value.toString()).append(", ");
            str.append(newLine);
        }
        return str.toString();
    }
}