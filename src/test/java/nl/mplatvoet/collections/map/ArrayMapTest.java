package nl.mplatvoet.collections.map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.testing.Helpers.mapEntry;


public class ArrayMapTest {


    public static Test suite() {
        return new ArrayMapTest().allTests();
    }

    public Test allTests() {
        TestSuite suite =
                new TestSuite("nl.mplatvoet.collections.map.ArrayMap");
        suite.addTest(arrayMapDefaultTests());
        return suite;
    }

    private Test arrayMapDefaultTests() {
        return MapTestSuiteBuilder
                .using(new DefaultTestMapGenerator())
                .named("ArrayMapTests")
                .withFeatures(
                        CollectionSize.ANY,
                        MapFeature.SUPPORTS_REMOVE,
                        MapFeature.ALLOWS_NULL_VALUE_QUERIES,
                        MapFeature.ALLOWS_NULL_VALUES,
                        MapFeature.RESTRICTS_KEYS,
                        MapFeature.SUPPORTS_PUT,
                        MapFeature.SUPPORTS_REMOVE,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE

                )
                .createTestSuite();
    }


    private static class DefaultTestMapGenerator implements TestMapGenerator<Integer, Integer> {
        @Override
        public SampleElements<Map.Entry<Integer, Integer>> samples() {
            return new SampleElements<>(
                    mapEntry(1, 1),
                    mapEntry(2, 3),
                    mapEntry(3, 2),
                    mapEntry(90, 4),
                    mapEntry(100, 5));
        }

        @Override
        public Map<Integer, Integer> create(Object... elements) {
            ArrayMap<Integer> map = new ArrayMap<>();
            for (Object e : elements) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                map.put((Integer)entry.getKey(), (Integer)entry.getValue());
            }
            return map;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map.Entry<Integer, Integer>[] createArray(int length) {
            return new Map.Entry[length];
        }

        @Override
        public Iterable<Map.Entry<Integer, Integer>> order(
                List<Map.Entry<Integer, Integer>> insertionOrder) {
            return insertionOrder;
        }

        @Override
        public Integer[] createKeyArray(int length) {
            return new Integer[length];
        }

        @Override
        public Integer[] createValueArray(int length) {
            return new Integer[length];
        }
    }
}