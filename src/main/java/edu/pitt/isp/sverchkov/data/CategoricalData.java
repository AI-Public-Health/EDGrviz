package edu.pitt.isp.sverchkov.data;

import java.util.List;
import java.util.Map;

/**
 *
 * @author YUS24
 */
public interface CategoricalData<Attribute, Value> {

    int count(Map<Attribute, Value> assignment);

    Map<Value, Integer> counts(Attribute attribute, Map<Attribute, Value> assignment);

    List<Value> values(Attribute attribute);

    List<CategoricalData<Attribute,Value>> split(Attribute attribute);
}
