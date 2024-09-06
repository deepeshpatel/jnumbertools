package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

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
    public void should_generate_all_subsets_for_5elements_and_different_m_for_range_3_6() {
        var elements = List.of('0','1','2','3','4','5');

        for(int from=1; from<elements.size(); from++) {
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

    @Test
    public void test_start_parameter_greater_than_0() {
        var builder = subsets.of('A','B','C','D');

        var listOfAll = builder.all()
                .lexOrderMth(5, 3).stream().collect(Collectors.toList());
        Assert.assertEquals("[[C], [B, C], [A, C, D]]", listOfAll.toString());

        var listOfRange = builder.inRange(2,3)
                .lexOrderMth(5,3).stream().collect(Collectors.toList());
        Assert.assertEquals("[[B, C], [A, C, D]]", listOfRange.toString());
    }
}