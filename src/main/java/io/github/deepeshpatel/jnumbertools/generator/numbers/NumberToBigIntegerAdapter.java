package io.github.deepeshpatel.jnumbertools.generator.numbers;
import java.util.Iterator;
import java.math.BigInteger;

public class NumberToBigIntegerAdapter implements Iterable<BigInteger> {
    private final Iterable<? extends Number> numberIterable;

    public NumberToBigIntegerAdapter(Iterable<? extends Number> numberIterable) {
        if (numberIterable == null) {
            throw new IllegalArgumentException("Input iterable cannot be null");
        }
        this.numberIterable = numberIterable;
    }

    @Override
    public Iterator<BigInteger> iterator() {
        return new BigIntegerIterator(numberIterable.iterator());
    }

    private record BigIntegerIterator(Iterator<? extends Number> numberIterator) implements Iterator<BigInteger> {

        @Override
            public boolean hasNext() {
                return numberIterator.hasNext();
            }

            @Override
            public BigInteger next() {
                return BigInteger.valueOf(numberIterator.next().longValue());
            }

            @Override
            public void remove() {
                numberIterator.remove();
            }
        }
}