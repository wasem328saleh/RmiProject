package Coordinator;
import Common.CoordinatorInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CoordinatorServer {
    public static void main(String[] args) {
    try {

        // إنشاء كائن من الـ CoordinatorImpl
        CoordinatorInterface coordinator = new CoordinatorImpl();

        // إنشاء registry على البورت 5000 (أو أي بورت آخر)
        Registry registry = LocateRegistry.createRegistry(5000);

        // تسجيل الكائن في الـ registry
        registry.rebind("coordinator", coordinator);

        System.out.println("Coordinator server is running...");

    } catch (Exception e) {
        System.err.println("Server error: " + e.getMessage());
        e.printStackTrace();
    }
}
}
