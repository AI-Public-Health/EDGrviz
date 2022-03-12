/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.pitt.isp.sverchkov.egvz.node;

import edu.pitt.isp.sverchkov.edgr.EDGrLogic;

import java.util.*;

/**
 *
 * @author yus24
 * @param <Variable>
 * @param <Value>
 */
public abstract class ENode<Variable,Value> extends EDGrNode<Variable,Value> {
    public final EDGrLogic<Variable,Value> edgar;
    public final Map<Variable,Value> conditions;
    protected boolean needToGenerate;

    protected ENode(EDGrLogic<Variable, Value> edgar, Map<Variable, Value> conditions, EDGrNode<Variable, Value> parent) {
        super(parent);
        this.edgar = edgar;
        this.conditions = Collections.unmodifiableMap(conditions);
        this.needToGenerate = true;
    }

}
