package com.qingqing.base.spec;

/**
 * Created by huangming on 2016/8/26.
 */
public interface Spec<T> {

    boolean isSatisfiedBy(T product);

}
