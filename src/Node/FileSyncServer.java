package Node;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
public class FileSyncServer {
    public static void startServer(int port, String nodeStoragePath) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("📂 FileSyncServer is listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("✅ Connected to client");

            DataInputStream dis = new DataInputStream(socket.getInputStream());


            // *** التعديل هنا: قراءة عدد الملفات أولاً ***
            int numberOfFiles = dis.readInt();

            for (int i = 0; i < numberOfFiles; i++) {

                String relativePath = dis.readUTF();
                long fileSize = dis.readLong();

                Path filePath = Paths.get(nodeStoragePath, relativePath);
                Files.createDirectories(filePath.getParent());

                try (OutputStream os = Files.newOutputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    long remaining = fileSize;

                    while ((read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                        os.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
                System.out.println("📥 File received: " + relativePath);

            }
            dis.close();
            socket.close();
        }
    }
}