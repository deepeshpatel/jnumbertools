package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class KPermutationForSequence<T> extends AbstractKPermutation<T> {
    final int[] initialValue;
    final BigInteger nPk;

    final Iterable<BigInteger> sequence;

    public KPermutationForSequence(List<T> elements, int k, Iterable<BigInteger> sequence, Calculator calculator) {
        super(elements, k);
        this.sequence = sequence;
        this.initialValue = IntStream.range(0, k).toArray();
        nPk = calculator.nPr(elements.size(), initialValue.length);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr(sequence.iterator());
    }

    class Itr implements Iterator<List<T>> {

        private final Iterator<BigInteger> seqGenerator;

        public Itr(Iterator<BigInteger> seqGenerator) {
            this.seqGenerator = seqGenerator;
        }

        @Override
        public boolean hasNext() {
            return seqGenerator.hasNext();
        }

        @Override
        public List<T> next() {
            BigInteger rank = seqGenerator.next();
            int[] next = PermutadicAlgorithms.unRankWithoutBoundCheck(rank, elements.size(), k);
            return indicesToValues(next);
        }
    }
}