package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Product<T> {

    List<List<List<T>>> allLists = new LinkedList<>();

    private Product(List<List<T>> list) {
        allLists.add(list);
    }

    public static <T> Product<T> of(int n, List<T> data) {
        var first = new JNumberTools().combinations().unique(n,data).lexOrder().stream().toList();
        return new Product<>(first);
    }

    public Product<T> andDistinct(int n, List<T> data) {
        var lists = new JNumberTools().combinations().unique(n, data).lexOrder().stream().toList();
        allLists.add(lists);
        return this;
    }

    public Product<T> andMultiSelect(int n, List<T> data) {
        var lists = new JNumberTools().combinations().repetitive(n, data).lexOrder().stream().toList();
        allLists.add(lists);
        return this;
    }


    public Product<T> andInRange(int a, int b, List<T> data) {
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
