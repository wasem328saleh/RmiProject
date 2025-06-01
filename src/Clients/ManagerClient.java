package Clients;
import Common.CoordinatorInterface;
import Common.User;

import java.rmi.Naming;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ManagerClient {
    public static void main(String[] args) {
    try {
        // الاتصال بالسيرفر
        CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost:5000/coordinator");


        // تسجيل الدخول كمدير
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        String token = coordinator.login(username, password);
        User manager=coordinator.getUserByUserName(username);
        if (manager != null && "manager".equals(manager.getRole())) {
            System.out.println("Login successful as Manager.");
            System.out.println("Token: " + manager.getToken());

            // عرض قائمة خيارات للمدير
            int choice = -1;
            do {
                System.out.println("\nChoose an action:");
                System.out.println("1. Create new employee");
                System.out.println("2. Update employee permissions");
                System.out.println("0. Exit");

                System.out.print("Your choice: ");
                String input = scanner.nextLine();

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a number.");
                    continue; // يرجع لبداية الحلقة
                }
                if (choice == 1) {
                    // إنشاء موظف جديد
                    System.out.print("Enter new employee username: ");
                    String newUsername = scanner.nextLine();

                    System.out.print("Enter new employee password: ");
                    String newPassword = scanner.nextLine();

                    System.out.print("Enter employee department: ");
                    String department = scanner.nextLine();

                    System.out.print("Enter permissions (comma separated, e.g., upload,download): ");
                    String permsInput = scanner.nextLine();
                    Set<String> permissions = new HashSet<>(Arrays.asList(permsInput.split(",")));

                    User newEmployee = new User(newUsername, newPassword, "employee", department, permissions);

                    coordinator.addEmployee(manager, newEmployee);
                    System.out.println("New employee added successfully.");

                } else if (choice == 2) {
                    // تعديل صلاحيات موظف
                    System.out.print("Enter username to update permissions: ");
                    String targetUsername = scanner.nextLine();
                    // 🔍 تحقق من وجود المستخدم
                    User targetUser = coordinator.findUserByUsername(targetUsername);
                    if (targetUser == null) {
                        System.out.println("User not found.");
                        continue; // ارجع للقائمة
                    }

                    // ✅ المستخدم موجود، طباعة صلاحياته الحالية
                    System.out.println("Current permissions: " + targetUser.getPermissions());

                    Set<String> newPermissions = new HashSet<>();

                    System.out.print("Allow delete files? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) newPermissions.add("delete");

                    System.out.print("Allow edit files? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) newPermissions.add("edit");

                    System.out.print("Allow list files? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) newPermissions.add("list");

                    System.out.print("Allow upload files? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) newPermissions.add("upload");

                    System.out.print("Allow download files? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) newPermissions.add("download");

                    coordinator.updateUserPermissions(manager, targetUsername, newPermissions);
                    System.out.println("User permissions updated.");

                } else if (choice == 0) {
                    System.out.println("Exiting manager panel...");
                } else {
                    System.out.println("Invalid choice.");
                }

            } while (choice != 0);





        } else {
            System.out.println("Login failed or not a manager.");
        }
        scanner.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
