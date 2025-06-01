package Sync;
import Utils.FileChangeDetector;

import java.util.*;
import java.util.concurrent.*;

public class FileSyncScheduler {
    public static void scheduleDailySync(String currentNodeStorage, List<NodeInfo> targetNodes) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // ✨ التزامن الأول بيشتغل فوراً بعد 5 ثواني
        scheduler.schedule(() -> {
            try {
                System.out.println("🕛 Starting first file sync from: " + currentNodeStorage);

                Map<String, String> changedFiles = FileChangeDetector.detectChanges(currentNodeStorage);

                for (NodeInfo node : targetNodes) {
                    FileSyncClient.sendFilesToNode(currentNodeStorage, node.host, node.port, changedFiles);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 40, TimeUnit.SECONDS);  // أول تزامن بعد 40 ثواني


        long delay = calculateInitialDelay(23, 59); // وقت المزامنة

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("🕛 Starting daily file sync from: " + currentNodeStorage);

                Map<String, String> changedFiles = FileChangeDetector.detectChanges(currentNodeStorage);

                for (NodeInfo node : targetNodes) {
                    FileSyncClient.sendFilesToNode(currentNodeStorage, node.host, node.port, changedFiles);


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, delay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public static long calculateInitialDelay(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar nextRun = (Calendar) now.clone();
        nextRun.set(Calendar.HOUR_OF_DAY, hour);
        nextRun.set(Calendar.MINUTE, minute);
        nextRun.set(Calendar.SECOND, 0);

        if (now.after(nextRun)) nextRun.add(Calendar.DATE, 1);
        return (nextRun.getTimeInMillis() - now.getTimeInMillis()) / 1000;
    }

    // ✅ كلاس داخلي بسيط لتمثيل العقدة الهدف
    public static class NodeInfo {
        public String host;
        public int port;

        public NodeInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
