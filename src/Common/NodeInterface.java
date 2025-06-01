package Common;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface NodeInterface extends Remote{// رفع ملف إلى مجلد القسم
    void uploadFile(String department, String fileName, byte[] data) throws RemoteException;

    // حذف ملف من مجلد القسم
    void deleteFile(String department, String fileName) throws RemoteException;

    // تعديل محتوى ملف
    void editFile(String department, String fileName, byte[] newData) throws RemoteException;

    // عرض جميع الملفات داخل مجلد القسم
    List<String> listFiles(String department) throws RemoteException;

    // تحميل ملف من مجلد القسم
    byte[] downloadFile(String department, String fileName) throws RemoteException;
    //talab4
    boolean hasFile(String fileName) throws RemoteException;

    String getFolderPath()throws RemoteException;



}
