package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.subsets;

public class SubsetGeneratorMthTest {

    @Test
    public void shouldGenerate_AllSubsets_For_4Elements_and_M_Equals1() {
        var elements = List.of('A','B','C','D');
        var iter_expected = subsets.of(elements).all().lexOrder().iterator();
        var iter_testResult = subsets.of(elements).all().lexOrderMth(1, 0).iterator();
        while(iter_expected.hasNext()) {
            Assert.assertEquals(iter_expected.next(), iter_testResult.next());
        }
    }

    @Test
    public void should_generate_all_subsets_for_10elements_and_different_m_for_range_3_6() {
        var elements = List.of('0','1','2','3','4','5','6','7','8','9');

        for(int from=1; from<elements.size()/2; from++) {
            for(int to=from; to<elements.size(); to++) {
                for(int m=1; m<6; m++) {
                    var allValues = subsets.of(elements).inRange(from, to).lexOrder().stream();
                    var expected = TestBase.everyMthValue(allValues, m);
                    var  result =  subsets.of(elements).inRange(from, to).lexOrderMth(m, 0).stream().toList();
                    Assert.assertEquals(expected, result);
                }
            }
        }
    }

    @Test
    public void should_generate_all_subsets_for_4elements_and_different_m() {
        var elements = List.of('A','B','C','D');
        for(int m=1; m<=15; m++) {
            var allValues = subsets.of(elements).all().lexOrder().stream();
            var expected = TestBase.everyMthValue(allValues, m);
            var  result =  subsets.of(elements).all().lexOrderMth(m, 0).stream().toList();
            Assert.assertEquals(expected, result);
        }
    }
}