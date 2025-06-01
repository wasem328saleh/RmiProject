package Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileLockManager {
    private static final Map<String, ReentrantReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    public static boolean lockFile(String filename, boolean writeLock, long timeout, TimeUnit unit) {
        ReentrantReadWriteLock lock = fileLocks.computeIfAbsent(filename, k -> new ReentrantReadWriteLock(true));

        try {
            if (writeLock) {
                return lock.writeLock().tryLock(timeout, unit);
            } else {
                return lock.readLock().tryLock(timeout, unit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void unlockFile(String filename, boolean writeLock) {
        ReentrantReadWriteLock lock = fileLocks.get(filename);
        if(lock != null) {
            if(writeLock) {
                lock.writeLock().unlock();
            } else {
                lock.readLock().unlock();
            }
        }
    }
}
