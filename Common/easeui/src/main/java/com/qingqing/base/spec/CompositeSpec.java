package com.qingqing.base.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by huangming on 2016/8/26.
 */
public class CompositeSpec<T> implements Spec<T> {
    
    List<Spec<T>> specs = new ArrayList<>();
    
    public void addSpec(Spec<T> spec) {
        specs.add(spec);
    }
    
    public List<Spec<T>> getSpecs() {
        return Collections.unmodifiableList(specs);
    }
    
    @Override
    public boolean isSatisfiedBy(T product) {
        Iterator<Spec<T>> iterator = specs.iterator();
        while (iterator.hasNext()) {
            Spec<T> spec = iterator.next();
            if (!spec.isSatisfiedBy(product)) {
                return false;
            }
        }
        return true;
    }
    
}
