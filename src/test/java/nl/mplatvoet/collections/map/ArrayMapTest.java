package nl.mplatvoet.collections.map;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.SortedMapTestSuiteBuilder;
import com.google.common.collect.testing.TestMapGenerator;
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
                            CollectionFeature.SERIALIZABLE,
                            CollectionFeature.KNOWN_ORDER


                    ).createTestSuite();
        }
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
        public SortedMap<Integer, Integer> create(Object... elements) {
            ArrayMap<Integer> map = new ArrayMap<>();
            for (Object e : elements) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                map.put((Integer) entry.getKey(), (Integer) entry.getValue());
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