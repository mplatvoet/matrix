package nl.mplatvoet.collections.map;

import com.google.common.collect.testing.*;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.TestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import static com.google.common.collect.testing.Helpers.mapEntry;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ArrayMapTest.GuavaTests.class,
        ArrayMapTest.AdditionalTests.class,
})
public class ArrayMapTest {

    public static class AdditionalTests {
        private ArrayMap<String> map = new ArrayMap<>();

        @Test(expected = IllegalArgumentException.class)
        public void put_negativeKeyShouldThrow() throws Exception {
            map.put(-1, "");
        }
    }

    public static class GuavaTests {
        public static TestSuite suite() {
            return SortedMapTestSuiteBuilder
                    .using(new DefaultTestMapGenerator())
                    .named("ArrayMapGuavaTest")
                    .withFeatures(
                            CollectionSize.ANY,
                            MapFeature.SUPPORTS_REMOVE,
                            MapFeature.ALLOWS_NULL_VALUE_QUERIES,
                            MapFeature.ALLOWS_NULL_VALUES,
                            MapFeature.RESTRICTS_KEYS,
                            MapFeature.SUPPORTS_PUT,
                            MapFeature.SUPPORTS_REMOVE,
                            MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                            CollectionFeature.SERIALIZABLE
                            //CollectionFeature.SERIALIZABLE_INCLUDING_VIEWS
                    ).createTestSuite();
        }
    }

    private static class DefaultTestMapGenerator implements TestSortedMapGenerator<Integer, Integer> {
        @Override
        public SampleElements<Map.Entry<Integer, Integer>> samples() {
            return new SampleElements<>(
                    mapEntry(10, 1),
                    mapEntry(11, 3),
                    mapEntry(12, 2),
                    mapEntry(13, 4),
                    mapEntry(20, 5));
        }

        @Override
        public SortedMap<Integer, Integer> create(Object... elements) {
            ArrayMap<Integer> map = new ArrayMap<>();
            for (Object e : elements) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                map.put((Integer) entry.getKey(), (Integer) entry.getValue());
            }
            return map;
        }

        @Override
        public Map.Entry<Integer, Integer> belowSamplesLesser() {
            return mapEntry(1, 40);
        }

        @Override
        public Map.Entry<Integer, Integer> belowSamplesGreater() {
            return mapEntry(5, 20);
        }

        @Override
        public Map.Entry<Integer, Integer> aboveSamplesLesser() {
            return mapEntry(30, 10);
        }

        @Override
        public Map.Entry<Integer, Integer> aboveSamplesGreater() {
            return mapEntry(40, 7);
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