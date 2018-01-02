package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.config.SystemProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {

    private static final int THRESHOLD = 500;

	private volatile List<Integer> primes = new ArrayListWithSyncAdd<>(20_000);

    /**
     * {@inheritDoc}
     *
     * Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
    	if (limit < THRESHOLD) {
			SieveSequential sieveSequential = new SieveSequential();
			return sieveSequential.countPrimes(limit);
		}
		int cores = Integer.valueOf(SystemProperty.numWorkers.getPropertyValue());
		ExecutorService executorService = Executors.newFixedThreadPool(cores);
		SieveActorActor actor = new SieveActorActor(0, cores, primes, executorService);
        actor.process(limit);

		executorService.shutdown();

        return primes.size();
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

    	private int id;
    	private int amount;
		private List<Integer> primes;
		private ExecutorService executorService;
		private SieveActorActor nextActor;

		SieveActorActor(int id, int amount, List<Integer> primes, ExecutorService executorService) {
			this.id = id;
			this.amount = amount;
			this.primes = primes;
			this.executorService = executorService;
		}

		/**
         * Process a single message sent to this actor.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
			Integer num = (Integer) msg;

			Future<?> future = executorService.submit(() -> {
				for (int i = 2; i < num; i++) {
					if (i % amount == id)
						if (isPrime(i))
							primes.add(i);
				}
			});

			int nextId = id + 1;
			if (nextId < amount) {
				nextActor = new SieveActorActor(nextId, amount, primes, executorService);
				nextActor.process(num);
			}
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private boolean isPrime(int n) {
			if (n < 2) return false;
			if (n == 2) return true;
			if (n % 2 == 0) return false;
			for (int i = 3; i * i <= n; i += 2)
				if (n % i == 0) return false;
			return true;
		}
    }

	private static class ArrayListWithSyncAdd<E> extends ArrayList<E> {

		public ArrayListWithSyncAdd(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public synchronized boolean add(E e) {
			return super.add(e);
		}
	}
}