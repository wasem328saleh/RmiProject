package Node;
import Common.NodeInterface;
import Sync.FileSyncScheduler;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Node1Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(6001);
            NodeInterface node = new NodeImpl("node1_storage");

            Naming.rebind("rmi://localhost:6001/node1", node);
            System.out.println("Node1 running on port 6001...");

            new Thread(() -> {
                try {
                    FileSyncServer.startServer(5003, "node1_storage");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            // بعد 40 ثانية من تشغيل العقدة، قم بتفعيل المزامنة
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    FileSyncScheduler.scheduleDailySync(
                            "node3_storage",
                            Arrays.asList(
                                    new FileSyncScheduler.NodeInfo("localhost", 5002),
                                    new FileSyncScheduler.NodeInfo("localhost", 5001)
                            )
                    );
                    System.out.println("File sync executed at: " + new java.util.Date());
                }
            }, 0, 40 * 1000);
//            // ✅ تشغيل المزامنة اليومية مع باقي العقد
//            FileSyncScheduler.scheduleDailySync(
//                    "node1_storage",
//                    Arrays.asList(
//                            new FileSyncScheduler.NodeInfo("localhost", 5001), // Node2
//                            new FileSyncScheduler.NodeInfo("localhost", 5002)  // Node3
//                    )
//            );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
