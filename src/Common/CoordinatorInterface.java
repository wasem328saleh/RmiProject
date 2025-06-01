package Common;
import Common.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
public interface CoordinatorInterface extends Remote{
    // تسجيل الدخول
    String login(String username, String password) throws RemoteException;

    // إضافة موظف - فقط إذا كان المدير هو اللي عم يطلب
    void addEmployee(User manager, User newEmployee) throws RemoteException;
    void updateUserPermissions(User admin, String username, Set<String> newPermissions) throws RemoteException;
    User findUserByUsername(String username) throws RemoteException;
    String uploadFile(String token, String department, String fileName, byte[] fileData) throws RemoteException;

    void deleteFile(String token, String department, String fileName) throws RemoteException;

    void editFile(String token, String department, String fileName, byte[] newData) throws RemoteException;

    List<String> listFiles(String token, String department) throws RemoteException;

    byte[] downloadFile(String token, String department, String fileName) throws RemoteException;
    String findFileLocation(String fileName) throws RemoteException;
    int getNodePort(String nodeName) throws RemoteException;
    public void registerNode(String nodeId, NodeInterface nodeStub)throws RemoteException;
    User getUserByUserName(String username)throws RemoteException;




}
