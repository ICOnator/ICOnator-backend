package io.iconator.commons.db.services;

import io.iconator.commons.db.services.config.TestConfig;
import io.iconator.commons.model.db.KeyPairs;
import io.iconator.commons.test.utils.ThreadTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class, KeyPairsRepositoryService.class})
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional(propagation = Propagation.SUPPORTS)
public class KeyPairsRepositoryServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(KeyPairsRepositoryServiceTest.class);

    @Autowired
    private KeyPairsRepositoryService keyPairsRepositoryService;

    @Test
    public void testGetFreshKey() throws InterruptedException {

        List<Optional<KeyPairs>> optionalKeyPairsList =
                Collections.synchronizedList(new ArrayList<Optional<KeyPairs>>());

        ThreadTestUtils.runMultiThread(
                () -> optionalKeyPairsList.add(keyPairsRepositoryService.getFreshKey()),
                10
        );

        assertEquals(10, optionalKeyPairsList.size());
        assertEquals(10, optionalKeyPairsList.stream().filter((k) -> k.isPresent()).count());

        List<KeyPairs> keyPairsList = optionalKeyPairsList.stream()
                .map((k) -> k.get())
                .collect(Collectors.toList());

        assertTrue(allBTCAreUnique(10, keyPairsList));
        assertTrue(allETHAreUnique(10, keyPairsList));
        assertTrue(allMarkedAsUnavailable(keyPairsList));

    }

    @Test
    public void testAddKeyPairsIfNotPresent_New() {
        KeyPairs k1 = new KeyPairs("testPublicBtc", "testPublicEth", true);
        Optional<KeyPairs> resultFind = keyPairsRepositoryService.findByPublicBtc("testPublicBtc");
        assertFalse(resultFind.isPresent());
        boolean result1 = keyPairsRepositoryService.addKeyPairsIfNotPresent(k1);
        assertTrue(result1);
    }

    @Test()
    public void testAddKeyPairsIfNotPresent_AlreadyInserted() {
        KeyPairs k1 = new KeyPairs("mgfRrsDH56YfTgw3pWJg7j4yEaUgxJxxim", "0x0eB5C5de600D088AB0260d068E9765022FD5173b", true);
        Optional<KeyPairs> resultFind = keyPairsRepositoryService.findByPublicBtc("mgfRrsDH56YfTgw3pWJg7j4yEaUgxJxxim");
        assertTrue(resultFind.isPresent());
        boolean result1 = keyPairsRepositoryService.addKeyPairsIfNotPresent(k1);
        assertFalse(result1);
    }

    private static <T> Predicate<T> distinctKeyPair(Function<? super T, ?> keyPairsExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyPairsExtractor.apply(t));
    }

    private static boolean allBTCAreUnique(int uniqueEntries, List<KeyPairs> keyPairsList) {
        return keyPairsList.stream().filter(distinctKeyPair(KeyPairs::getPublicBtc)).count() == uniqueEntries;
    }

    private static boolean allETHAreUnique(int uniqueEntries, List<KeyPairs> keyPairsList) {
        return keyPairsList.stream().filter(distinctKeyPair(KeyPairs::getPublicEth)).count() == uniqueEntries;
    }

    private static boolean allMarkedAsUnavailable(List<KeyPairs> keyPairsList) {
        return keyPairsList.stream().allMatch((k) -> !k.getAvailable());
    }

}
