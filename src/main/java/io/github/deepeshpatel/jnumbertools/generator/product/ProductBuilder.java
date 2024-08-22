package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.Combinations;
import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProductBuilder<T> {

    private final List<List<List<T>>> allLists = new LinkedList<>();
    private final Calculator calculator;

    public ProductBuilder(int n, List<T> data, Calculator calculator) {
        this.calculator = calculator;
        List<List<T>> first = new Combinations(calculator).unique(n,data).lexOrder().stream().toList();
        allLists.add(first);
    }

    public ProductBuilder<T> andDistinct(int n, List<T> data) {
        var lists = new Combinations(calculator).unique(n, data).lexOrder().stream().toList();
        allLists.add(lists);
        return this;
    }

    public ProductBuilder<T> andMultiSelect(int n, List<T> data) {
        var lists = new Combinations(calculator).repetitive(n, data).lexOrder().stream().toList();
        allLists.add(lists);
        return this;
    }

    public ProductBuilder<T> andInRange(int a, int b, List<T> data) {
        var lists = JNumberTools.subsets()
                .of(data).inRange(a,b)
                .stream().toList();
        allLists.add(lists);
        return this;
    }

    public List<List<T>> build() {
        //List<List<List<T>>> allLists = new LinkedList<>(permanent);
        List<List<T>> first = allLists.remove(0);
        while(allLists.size() > 0) {
            first = productImpl(first, allLists.remove(0));
        }
        return first;
    }

    private  List<List<T>> productImpl(List<List<T>> l1, List<List<T>> l2) {

        List<List<T>> product = new ArrayList<>();
        for(List<T> valuesInL1: l1) {
            for(List<T> valuesInL2: l2) {
                List<T> entry = new ArrayList<>();
                entry.addAll(valuesInL1);
                entry.addAll(valuesInL2);
                product.add(entry);
            }
        }
        return product;
    }
}
