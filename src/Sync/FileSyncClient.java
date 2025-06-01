package Sync;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Map;

public class FileSyncClient {
    public static void sendFilesToNode(String nodeStoragePath, String targetIP, int targetPort, Map<String, String> changedFiles) {
        try (Socket socket = new Socket(targetIP, targetPort);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            dos.writeInt(changedFiles.size()); // كم ملف رح نرسل

            for (String relativePath : changedFiles.keySet()) {
                File file = Paths.get(nodeStoragePath, relativePath).toFile();


                dos.writeUTF(relativePath); // المسار النسبي للملف

                byte[] fileBytes = Files.readAllBytes(file.toPath());
                dos.writeLong(fileBytes.length); // حجم الملف
                dos.write(fileBytes);            // محتوى الملف
            }

            System.out.println("✔ Sent " + changedFiles.size() + " files to " + targetIP + ":" + targetPort);

        } catch (Exception e) {
            System.err.println("✘ Error sending files to " + targetIP + ":" + targetPort);
            e.printStackTrace();
        }
    }
}
