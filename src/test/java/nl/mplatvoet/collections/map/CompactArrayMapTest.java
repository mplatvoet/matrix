package nl.mplatvoet.collections.map;

import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.SortedMapTestSuiteBuilder;
import com.google.common.collect.testing.TestSortedMapGenerator;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CompactArrayMapTest.GuavaTests.class,
        CompactArrayMapTest.AdditionalTests.class,
})
public class CompactArrayMapTest {

    public static class AdditionalTests {

        private <V> SortedMap<Integer, V> createNewSortedMap() {
            return new CompactArrayMap<>();
        }

        @Test(expected = IllegalArgumentException.class)
        public void put_negativeKeyShouldThrow() throws Exception {
            createNewSortedMap().put(-1, "");
        }

        @Test
        public void clear_tailMap() {
            final SortedMap<Integer, Integer> map = createNewSortedMap();
            map.put(1, 5);
            map.put(2, 5);
            map.put(4, 5);
            map.tailMap(2).clear();
            assertThat("should only clear the tailMap portion", map.size(), is(1));
        }

        @Test
        public void clear_headMap() {
            final SortedMap<Integer, Integer> map = createNewSortedMap();
            map.put(1, 5);
            map.put(2, 5);
            map.put(4, 5);
            map.headMap(2).clear();
            assertThat("should only clear the headMap portion", map.size(), is(2));
        }

        @Test
        public void clear_subMap() {
            final SortedMap<Integer, Integer> map = createNewSortedMap();
            map.put(1, 5);
            map.put(2, 5);
            map.put(4, 5);
            map.subMap(2, 3).clear();
            assertThat("should only clear the subMap portion", map.size(), is(2));
        }
    }

    public static class GuavaTests {
        public static TestSuite suite() {
            return SortedMapTestSuiteBuilder
                    .using(new DefaultTestMapGenerator())
                    .named("CompactArrayMapGuavaTest")
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
            CompactArrayMap<Integer> map = new CompactArrayMap<>();
            for (Object e : elements) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                if (entry == null) {
                    map.put(null, null);
                } else {
                    map.put((Integer) entry.getKey(), (Integer) entry.getValue());
                }
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