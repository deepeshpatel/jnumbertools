import io.github.deepeshpatel.jnumbertools.entrypoint.Combinations;
import io.github.deepeshpatel.jnumbertools.entrypoint.Permutations;

import java.util.List;

public class Scrap3 {

    public static void main(String[] args) throws  Exception {

        Permutations p = new Permutations();
        p.unique(1,2,3).lexOrder();
        p.unique(1,2,3).lexOrderMth(3);
        p.nPr(5,3).lexOrder();
        p.nPr(5,3).lexOrderMth(2);
        p.nPr(5,3).combinationOrder();
        p.nPr(5,3).combinationOrderMth(2);
        p.multiset(List.of("A","B"), new int[]{2,3}).lexOrder();
        p.multiset(List.of("A","B"), new int[]{2,3}).lexOrderMth(2);
        p.repetitive(2,3).lexOrder();
        p.repetitive(2,3).lexOrderMth(2);

        Combinations c = new Combinations();
        c.unique(3,2).lexOrder();
        c.unique(3,2).lexOrderMth(2);
        c.multiset(List.of("A","B"), new int[]{2,3}, 1).lexOrder();
        c.repetitive(3,2).lexOrder();

    }
}
