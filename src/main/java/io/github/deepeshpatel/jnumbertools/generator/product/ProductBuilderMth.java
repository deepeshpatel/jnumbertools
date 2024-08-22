package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.Combinations;
import io.github.deepeshpatel.jnumbertools.entrypoint.Subsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductBuilderMth<T> {

    enum PRODUCT_TYPE  {DISTINCT, MULTISELECT, RANGE}

    //private final List<AbstractGenerator> generators = new LinkedList<>();
    private final Calculator calculator;
    private final long m;

    private final List<Entry> entries = new ArrayList<>();


    public ProductBuilderMth(long m, int n, List<T> data, Calculator calculator) {
        this.m = m;
        this.calculator = calculator;
        entries.add(new Entry(PRODUCT_TYPE.DISTINCT, data, n,-1));
    }

    public ProductBuilderMth<T> andDistinct(int n, List<T> data) {
        entries.add(new Entry(PRODUCT_TYPE.DISTINCT, data, n));
        return this;
    }

    public ProductBuilderMth<T> andMultiSelect(int n, List<T> data) {
        entries.add(new Entry(PRODUCT_TYPE.MULTISELECT, data, n));
        return this;
    }

    public ProductBuilderMth<T> andInRange(int a, int b, List<T> data) {
        entries.add(new Entry(PRODUCT_TYPE.RANGE, data, a, b));
        return this;
    }

    public List<T> build() {
        List<T> output = new ArrayList<>();
        Collections.reverse(entries);
        long remaining = m;
        //String str = "";
        for(Entry e: entries) {
            long index = remaining % e.radix;
            remaining = remaining / e.radix;
            List<T> values = e.getValue(index);
            Collections.reverse(values);
            output.addAll(values);

            //str = index + "," + str;

        }
        //System.out.println(str);
        Collections.reverse(output);
        return output;
    }

    private class Entry {
        final int a;
        final int b;
        final PRODUCT_TYPE type;
        final List<T> data;
        final long radix;

        public Entry(PRODUCT_TYPE type,  List<T> data, int a, int b) {
            this.a = a;
            this.b = b;
            this.type = type;
            this.data = data;
            radix = initCount();
        }

        public Entry(PRODUCT_TYPE type,  List<T> data, int a) {
            this(type, data, a, a);
        }

        private long initCount() {
            return switch(type) {
                case DISTINCT -> calculator.nCr(data.size(), a).longValue();
                case MULTISELECT -> calculator.nCr(data.size()+a-1, a).longValue();
                default -> countForRange();
            };
        }

        private long countForRange() {
            long c = 0;
            for(int i=a; i<=b; i++ ) {
                c = c  + calculator.nCr(data.size(), i).longValue();
            }
            return c;
        }

        public List<T> getValue(long index) {
            return switch (type) {
                case DISTINCT -> getValueForDistinct(index);

                case MULTISELECT -> new Combinations(calculator)
                        .repetitive(a, data)
                        .lexOrder().stream().toList().get((int)(index));

                default -> new Subsets()
                        .of(data).inRange(a,b)
                        .stream().toList().get((int)(index));
            };
        }

        private List<T> getValueForDistinct(long index) {
            if(index ==0) {
                var  iterator =  new Combinations(calculator).unique(a, data).lexOrder().iterator();
                return iterator.next();
            } else{
                var  iterator =  new Combinations(calculator).unique(a, data).lexOrderMth(index).iterator();
                iterator.next(); //zeroth index
                return iterator.next();
            }
        }
    }

}
