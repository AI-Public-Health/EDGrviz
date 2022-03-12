package edu.pitt.isp.sverchkov.data;

import java.util.*;

/**
 *
 * @author YUS24
 */
public class CategoricalDataSubset<Attribute,Value> implements CategoricalData<Attribute,Value> {

    private final CategoricalData<Attribute,Value> me;
    private final Attribute attribute;
    private final Value value;

    public CategoricalDataSubset( CategoricalData<Attribute,Value> source, Attribute a, Value v ){
        me = source;
        attribute = a;
        value = v;
    }

    @Override
    public int count(Map<Attribute, Value> assignment) {
        assignment = new HashMap<>( assignment );
        assignment.put(attribute, value);
        return me.count(assignment);
    }

    @Override
    public Map<Value, Integer> counts(Attribute a, Map<Attribute, Value> assignment) {
        assignment = new HashMap<>( assignment );
        assignment.put(attribute, value);
        return me.counts(a, assignment);
    }

    @Override
    public List<Value> values(Attribute a) {
        if( attribute.equals(a) ) return Collections.singletonList( value );
        return me.values(a);
    }

    @Override
    public List<CategoricalData<Attribute, Value>> split(Attribute attribute) {

        List<Value> valueList = values( attribute );

        List<CategoricalData<Attribute, Value>> results = new ArrayList<>( valueList.size() );

        for ( Value value : valueList ) {
            results.add( new CategoricalDataSubset<>( this, attribute, value ) );
        }

        return results;
    }
}
