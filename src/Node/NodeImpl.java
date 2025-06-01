package Node;
import Common.NodeInterface;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import Utils.FileChangeDetector;

public class NodeImpl extends UnicastRemoteObject implements NodeInterface {
    private final String nodeFolder;

    public NodeImpl(String nodeFolder) throws RemoteException {
        this.nodeFolder = nodeFolder;
        createDepartmentFolders();

    }

    public String getNodeFolder() {
        return nodeFolder;
    }

    private void createDepartmentFolders() {
        String[] departments = {"development", "graphic design", "QA"};
        for (String dept : departments) {
            File folder = new File(nodeFolder + "/" + dept);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
    }

    @Override
    public void uploadFile(String department, String fileName, byte[] data) throws RemoteException {
        String fileKey = department + "/" + fileName;

        try {

            FileLockManager.lockFile(fileKey, true,10, TimeUnit.SECONDS); // قفل كتابة

            Path deptPath = Paths.get(nodeFolder, department);
            if (!Files.exists(deptPath)) {
                Files.createDirectories(deptPath);
            }

            Path filePath = deptPath.resolve(fileName);
            Files.write(filePath, data);
            System.out.println("File uploaded to " + filePath);
        } catch (IOException e) {
            throw new RemoteException("Failed to upload file", e);
        }finally {
        FileLockManager.unlockFile(fileKey, true);
    }
    }


    @Override
    public byte[] downloadFile(String department, String fileName) throws RemoteException {
        String fileKey = department + "/" + fileName;
        try {
            FileLockManager.lockFile(fileKey, false,10, TimeUnit.SECONDS); // قفل كتابة

            Path filePath = Paths.get(nodeFolder,department , fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RemoteException("Failed to download file", e);
        }finally {
            FileLockManager.unlockFile(fileKey, false);
        }
    }


    @Override
    public void deleteFile(String department, String fileName) throws RemoteException {
        try {
            Path filePath = Paths.get(nodeFolder, department , fileName);
            Files.deleteIfExists(filePath);
            System.out.println("File deleted: " + filePath);
        } catch (IOException e) {
            throw new RemoteException("Failed to delete file", e);
        }
    }

    @Override
    public void editFile(String department, String fileName, byte[] newData) throws RemoteException {
        try {
            Path filePath = Paths.get(nodeFolder, department , fileName);
            Files.write(filePath, newData);
            System.out.println("File edited: " + filePath);
        } catch (IOException e) {
            throw new RemoteException("Failed to edit file", e);
        }
    }

    @Override
    public List<String> listFiles(String department) throws RemoteException {
        try {
            Path deptPath = Paths.get(nodeFolder, department);
            if (!Files.exists(deptPath)) return new ArrayList<>();

            return Files.list(deptPath)
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RemoteException("Failed to list files", e);
        }
    }
    //talab4
    String[] departments = FileChangeDetector.getDepartments();

    @Override
    public boolean hasFile(String fileName) throws RemoteException {
        for (String department : departments) { // departments: مجلدات الأقسام في هذه العقدة
            Path filePath = Paths.get(nodeFolder, department, fileName);
            if (Files.exists(filePath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFolderPath() throws RemoteException {
        return nodeFolder;
    }


}
