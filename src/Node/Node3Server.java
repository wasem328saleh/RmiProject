package Node;
import Common.CoordinatorInterface;
import Common.NodeInterface;
import Sync.FileSyncScheduler;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Node3Server {public static void main(String[] args) {
    try {
        LocateRegistry.createRegistry(6003);
        NodeInterface node = new NodeImpl("node3_storage");

        Naming.rebind("rmi://localhost:6003/node3", node);
        System.out.println("Node3 running on port 6003...");
//        CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost:5000/coordinator");
//          coordinator.registerNode("6003",node);
        new Thread(() -> {
            try {
                FileSyncServer.startServer(5002, "node3_storage");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        long secc=calculateInitialDelay(23,59);
        // بعد 40 ثانية من تشغيل العقدة، قم بتفعيل المزامنة
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                FileSyncScheduler.scheduleDailySync(
                        "node3_storage",
                        Arrays.asList(
                                new FileSyncScheduler.NodeInfo("localhost", 5003),
                                new FileSyncScheduler.NodeInfo("localhost", 5001)
                        )
                );
                System.out.println("File sync executed at: " + new java.util.Date());
            }
        }, 0, secc * 1000);
//        // ✅ تشغيل المزامنة اليومية مع باقي العقد
//        FileSyncScheduler.scheduleDailySync(
//                "node3_storage",
//                Arrays.asList(
//                        new FileSyncScheduler.NodeInfo("localhost", 5003), // Node2
//                        new FileSyncScheduler.NodeInfo("localhost", 5001)  // Node3
//                )
//        );



    } catch (Exception e) {
        e.printStackTrace();
    }
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
}
