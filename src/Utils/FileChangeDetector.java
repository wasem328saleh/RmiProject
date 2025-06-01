package Utils;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
public class FileChangeDetector {
    private static final String[] departments = {"development", "graphic design", "QA"};
    public static String[] getDepartments() {
        return departments;
    }

    public static Map<String, String> detectChanges(String nodePath) throws Exception {
        Map<String, String> currentHashes = new HashMap<>();
        for (String dept : departments) {
            Path deptPath = Paths.get(nodePath, dept);
            if (!Files.exists(deptPath)) continue;

            Files.walk(deptPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String hash = calculateHash(file);
                            String relativePath = Paths.get(nodePath).relativize(file).toString().replace("\\", "/");

                            currentHashes.put(relativePath.replace("\\", "/"), hash);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        File manifestFile = new File(nodePath + "/manifest.txt");
        Map<String, String> oldHashes = new HashMap<>();
        if (manifestFile.exists()) {
            List<String> lines = Files.readAllLines(manifestFile.toPath());
            for (String line : lines) {
                String[] parts = line.split(" = ");
                if (parts.length == 2) {
                    oldHashes.put(parts[0], parts[1]);
                }
            }
        }

        // الملفات الجديدة أو المعدّلة
        Map<String, String> changedFiles = new HashMap<>();
        currentHashes.forEach((path, hash) -> {
            if (!hash.equals(oldHashes.get(path))) {
                changedFiles.put(path, hash);
            }
        });

        // حدّث manifest.txt
        try (PrintWriter writer = new PrintWriter(manifestFile)) {
            currentHashes.forEach((path, hash) -> writer.println(path + " = " + hash));
        }

        return changedFiles;
    }

    private static String calculateHash(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(file);
        byte[] hash = digest.digest(bytes);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
