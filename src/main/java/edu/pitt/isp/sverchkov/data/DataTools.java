package edu.pitt.isp.sverchkov.data;

import java.io.*;
import java.util.*;

/**
 *
 * @author YUS24
 */
public class DataTools {

    public final static String NEWLINE = System.getProperty("line.separator");
    public final static String DELIMITER_REGEX = " *, *";
    public final static String DELIMITER = ", ";

    public static DataTableImpl<String,String> dataTableFromFile( File file ) throws FileNotFoundException{
        DataTableImpl<String,String> data;
        try ( Scanner in = new Scanner( file ) ){
            data = new DataTableImpl<>( internList( in.nextLine().trim().split(DELIMITER_REGEX) ) );
            while( in.hasNextLine() )
                data.addRow( internList( in.nextLine().trim().split(DELIMITER_REGEX) ) );
        }
        return data;
    }

    public static DataTableImpl<String,String> dataTableFromFile( File file, List<String> attributes ) throws FileNotFoundException{
        DataTableImpl<String,String> data;
        try ( Scanner in = new Scanner( file ) ){
            List<String> attrs = internList( in.nextLine().trim().split(DELIMITER_REGEX) );
            attrs.retainAll( attributes );
            data = new DataTableImpl<>( attrs );
            while( in.hasNextLine() )
                data.addRow( internList( in.nextLine().trim().split(DELIMITER_REGEX) ) );
        }
        return data;
    }

    public static DataTableImpl<String,String> dataTableFromFiles( File data, File nams ) throws FileNotFoundException{
        DataTableImpl<String,String> table;
        // Read nams
        try ( Scanner in = new Scanner( nams ) ){
            final int n = Integer.parseInt( in.nextLine() );
            List<String> cols = new ArrayList<>(n);
            for( int i = 0; i<n; i++ ){
                cols.add( in.nextLine().intern() );
                for( int j = Integer.parseInt( in.nextLine() ); j > 0; j-- )
                    in.nextLine();
            }
            table = new DataTableImpl<>( cols );
        }

        try ( Scanner in = new Scanner( data ) ){
            while( in.hasNextLine() )
                table.addRow( internList( in.nextLine().trim().split(DELIMITER_REGEX) ) );
        }

        return table;
    }

    public static <Attribute,Value> DataTableImpl<Attribute,Value> dataTableFromMaps( Collection<? extends Map<? extends Attribute, ? extends Value>> maps ){
        List<? extends Attribute> attributes;
        {
            Set<Attribute> attr = new HashSet<>();
            for( Map<? extends Attribute, ? extends Value> map : maps )
                attr.addAll(map.keySet());
            attributes = new ArrayList<>(attr);
        }
        DataTableImpl<Attribute, Value> result = new DataTableImpl<>( attributes );

        for( Map<? extends Attribute, ? extends Value> map : maps ){
            List<Value> row = new ArrayList<>(attributes.size());
            for( Attribute a : attributes )
                row.add( map.get(a) );
            result.addRow(row);
        }

        return result;
    }

    public static <Attribute,Value> void saveCSV( DataTable<Attribute,Value> data, File dest, boolean headers ) throws IOException{
        try( BufferedWriter out = new BufferedWriter( new FileWriter( dest ) ) ){

            if( headers ){
                String delim = "";
                for( Attribute a : data.variables() ){
                    out.append(delim).append( a.toString() );
                    delim = DELIMITER;
                }
                out.append(NEWLINE);
            }

            for( List<Value> row : data ){
                String delim = "";
                for( Value v : row ){
                    out.append(delim).append( null==v ? "null" : v.toString() );
                    delim = DELIMITER;
                }
                out.append(NEWLINE);
            }
        }
    }

    public static <Attribute,Value> DataTableImpl<Attribute,Value> filter( DataTable<Attribute,Value> table, Attribute a, Value v ){
        final int vars = table.variables().size();
        List<Attribute> newAttrs = new ArrayList<>( table.variables() );
        int removed = newAttrs.indexOf(a);
        newAttrs.remove(removed);
        DataTableImpl<Attribute,Value> result = new DataTableImpl<>( newAttrs );

        for( List<Value> row : table ){
            if( v.equals( row.get(removed) ) ){
                List<Value> newRow = new ArrayList<>( vars-1 );
                for( int i = 0; i < vars; i++ ) if( i != removed )
                    newRow.add( row.get(i) );
                result.addRow( newRow );
            }
        }

        return result;
    }

    public static <Attribute,Value> DataTable<Attribute,Value> hide( DataTable<Attribute,Value> table, Attribute a ){
        final int vars = table.variables().size();
        List<Attribute> newAttrs = new ArrayList<>( table.variables() );
        int removed = newAttrs.indexOf(a);
        newAttrs.remove(removed);
        DataTable<Attribute,Value> result = new DataTableImpl<>( newAttrs );

        for( List<Value> row : table ){
            List<Value> newRow = new ArrayList<>( vars-1 );
            for( int i = 0; i < vars; i++ ) if( i != removed )
                newRow.add( row.get(i) );
            result.addRow( newRow );
        }

        return result;
    }

    public static List<String> internList(List<String> inlist){
        if( inlist == null ) return null;
        List<String> result = new ArrayList<>(inlist.size());
        for( String s : inlist )
            result.add(s.intern());
        return result;
    }

    public static List<String> internList(String... inlist){
        if( inlist == null ) return null;
        List<String> result = new ArrayList<>(inlist.length);
        for( String s : inlist )
            result.add(s.intern());
        return result;
    }
}