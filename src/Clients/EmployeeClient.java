package Clients;

import Common.CoordinatorInterface;
import Common.NodeInterface;
import Common.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.util.Scanner;
public class EmployeeClient {
    public static void main(String[] args) {
    try {
        // الاتصال بسيرفر RMI
        CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost:5000/coordinator");


        // معلومات الدخول للموظف
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();


        // تنفيذ عملية تسجيل الدخول
        String token = coordinator.login(username, password);

        if ( token != null) {
            System.out.println("Login successful!");
//            System.out.println("Welcome " + loggedInUser.getUsername());
//            System.out.println("Role: " + loggedInUser.getRole());
//            System.out.println("Token: " + loggedInUser.getToken());
//            System.out.println("Permissions: " + loggedInUser.getPermissions());
        } else {
            System.out.println("Login failed: Invalid credentials.");
        }


        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Upload File");
            System.out.println("2. Download File");
            System.out.println("3. Delete File");
            System.out.println("4. Edit File");
            System.out.println("5. List Files");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                // upload
                System.out.print("Department: ");
                String dept = scanner.nextLine();
                System.out.print("File path: ");
                String filePath = scanner.nextLine();
                byte[] data = Files.readAllBytes(Paths.get(filePath));
                String fileName = Paths.get(filePath).getFileName().toString();
                String response= coordinator.uploadFile(token, dept, fileName, data);
                if (!response.equals("Sucess")){
                    System.out.println(response);
                }else {
                    System.out.println("Uploaded");
                }




            } else if (choice.equals("2")) {

                System.out.print("Department: ");
                String dept = scanner.nextLine();

                System.out.print("File name to download: ");
                String fileName = scanner.nextLine();

// 1. طلب مكان الملف من الكورديناتور
                String nodeName = coordinator.findFileLocation(fileName);

                if (nodeName != null) {
                    // 2. الاتصال بالعقدة المعينة
                    NodeInterface node = (NodeInterface) Naming.lookup("rmi://localhost:" + nodeName); // تأكد من البورت واسم التسجيل

                    // 3. تحميل الملف من العقدة
                    byte[] fileData = node.downloadFile(dept, fileName);

                    // 4. حفظ الملف محلياً
                    Files.write(Paths.get("downloaded_" + fileName), fileData);
                    System.out.println("File downloaded successfully and saved as: downloaded_" + fileName);
                } else {
                    System.out.println("File not found in any node.");
                }



            } else if (choice.equals("3")) {
                // delete
                System.out.print("Department: ");
                String dept = scanner.nextLine();
                System.out.print("File name to delete: ");
                String fileName = scanner.nextLine();
                coordinator.deleteFile(token, dept, fileName);
                System.out.println("Deleted");

            } else if (choice.equals("4")) {
                // edit
                System.out.print("Department: ");
                String dept = scanner.nextLine();
                System.out.print("File name to edit: ");
                String fileName = scanner.nextLine();
                System.out.print("Path of new file content: ");
                String newPath = scanner.nextLine();
                byte[] newData = Files.readAllBytes(Paths.get(newPath));
                coordinator.editFile(token, dept, fileName, newData);
                System.out.println("Edited");

            } else if (choice.equals("5")) {
                // list
                System.out.print("Department: ");
                String dept = scanner.nextLine();
                System.out.println("Files:");
                coordinator.listFiles(token, dept)
                        .forEach(System.out::println);

            } else if (choice.equals("0")) {
                System.out.println("Goodbye!");
                break;

            } else {
                System.out.println("Invalid option.");
            }
        }

        scanner.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
