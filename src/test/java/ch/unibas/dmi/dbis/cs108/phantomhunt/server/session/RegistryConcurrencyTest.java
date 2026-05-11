package ch.unibas.dmi.dbis.cs108.phantomhunt.server.session;

import ch.unibas.dmi.dbis.cs108.phantomhunt.util.FakeClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistryConcurrencyTest {
    private Registry registry;

    @BeforeEach
    void setUp() {
        registry = Registry.getInstance();
        registry.resetForTests();
    }

    @Test
    void claimName_concurrentRequests_areHandledSafely() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // blocks all thread at starting line
        CountDownLatch startLatch = new CountDownLatch(1);
        // counts down, when a thread is finished
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // a thread-secure set to collect the taken names
        Set<String> claimedNames = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // prepare 100 threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // all wait for start here

                    FakeClientHandler handler = new FakeClientHandler("Unknown");
                    // all want to be named "alice" at the same time
                    String assignedName = registry.claimName("Alice", handler);

                    claimedNames.add(assignedName);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown(); // thread says goodbye
                }
            });
        }
        // lets all 100 threads start on registry
        startLatch.countDown();

        // wait until all threads have done their work
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "Timeout since Deadlock");
        executor.shutdown();

        // verify decision, when registry is thread-safe, 100 names must exist
        assertEquals(100, claimedNames.size(), "Each Thread must have received a name");

        // control if registry internally also has 100 names
        String[] namesArray = registry.names().split(" ");
        assertEquals(100, namesArray.length, "Each Thread must have received an entry name in the registry");
    }
}
