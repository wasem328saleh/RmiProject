package Coordinator;
import Common.CoordinatorInterface;
import Common.NodeInterface;
import Common.User;
import Node.NodeImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;



public class CoordinatorImpl extends UnicastRemoteObject implements CoordinatorInterface {

    // قائمة المستخدمين (مثلاً موجودين مسبقاً)
    private final Map<String, User> users = new HashMap<>();
    private final List<NodeInterface> nodes = new ArrayList<>();
    private final Map<String, Integer> nodePorts = new HashMap<>();
    private static final Map<String, String> tokenToUsername = new HashMap<>();

    Map<String, Integer> nodeLoadMap = new HashMap<>();
    Map<String, NodeInterface> registeredNodes = new HashMap<>();
    private Set<String> failedNodes = new HashSet<>();
    // Constructor
    public CoordinatorImpl() throws RemoteException {
        super();

        // مستخدمين افتراضيين
        users.put("admin", new User("admin", "admin123", "manager"));
        users.put("employee1", new User("employee1", "emp123", "employee"));
        Set<String> allPermissions = new HashSet<>(Arrays.asList("upload", "download", "delete", "edit", "list"));
        users.put("ahmad", new User("ahmad", "ahmad123", "employee", "development",
                Set.of("list", "upload", "download", "edit", "delete")));
        users.put("sara", new User("sara", "sara123", "employee", "QA",
                Set.of("list", "upload", "download", "edit", "delete")));
        users.put("ahma", new User("ahma", "ahma123", "employee", "QA",
                Set.of("list", "upload", "download", "edit", "delete")));

        // ✅ تعبئة خريطة المنافذ
        nodePorts.put("node1", 6001);
        nodePorts.put("node2", 6002);
        nodePorts.put("node3", 6003);



        // ربط العقد
        try {
            NodeInterface n1=(NodeInterface) java.rmi.Naming.lookup("rmi://localhost:6001/node1");
            nodes.add(n1);
            registeredNodes.put("6001", n1);
            nodeLoadMap.put("6001", 0);
            NodeInterface n2=(NodeInterface) java.rmi.Naming.lookup("rmi://localhost:6002/node2");
            nodes.add(n2);
            registeredNodes.put("6002", n2);
            nodeLoadMap.put("6002", 0);
            NodeInterface n3=(NodeInterface) java.rmi.Naming.lookup("rmi://localhost:6003/node3");
            nodes.add(n3);
            registeredNodes.put("6003", n3);
            nodeLoadMap.put("6003", 0);
            System.out.println("Nodes connected successfully.");
        } catch (Exception e) {
            System.err.println("Error connecting to nodes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void registerNode(String nodeId, NodeInterface nodeStub) {
        nodes.add(nodeStub);
        NodeImpl node= (NodeImpl) nodeStub;
        String s=node.getNodeFolder();
        System.out.println("node.getNodeFolder() : "+s);
        registeredNodes.put(nodeId, nodeStub);
        nodeLoadMap.put(nodeId, 0);
    }
    // ✅ دالة للحصول على رقم المنفذ من اسم العقدة
    @Override
    public int getNodePort(String nodeName) throws RemoteException {
        return nodePorts.getOrDefault(nodeName, -1);
    }
//public User getUserByToken(String token){
//
//}
    // login
//    @Override
//    public User login(String username, String password) throws RemoteException {
//        User user = users.get(username);
//        if (user != null && user.getPassword().equals(password)) {
//            String token = UUID.randomUUID().toString();
//            user.setToken(token);
//            System.out.println("User " + username + " logged in with token: " + token);
//            return user;
//        }
//        return null;
//    }
      @Override
      public  String login(String username, String password) {
          User user = users.get(username);
          if (user != null && user.getPassword().equals(password)) {
              String token = UUID.randomUUID().toString(); // توليد توكن عشوائي
              tokenToUsername.put(token, username); // تخزين التوكن
              return token; // إرجاع التوكن للمستخدم
    }
    return null;
}


    // إضافة موظف
    @Override
    public void addEmployee(User manager, User newEmployee) throws RemoteException {
        if (manager != null && manager.getRole().equals("manager")) {
            users.put(newEmployee.getUsername(), newEmployee);
            System.out.println("Added new employee: " + newEmployee.getUsername());
        } else {
            System.out.println("Unauthorized attempt to add employee by " + (manager != null ? manager.getUsername() : "null"));
            throw new RemoteException("Only manager can add new employees.");
        }
    }


    @Override
    public void updateUserPermissions(User admin, String username, Set<String> newPermissions) throws RemoteException {
        if (admin != null && admin.getRole().equals("manager")) {
            User user = users.get(username);
            if (user != null) {
                user.setPermissions(newPermissions);
                System.out.println("Permissions updated for user: " + username);
            } else {
                System.out.println("User not found: " + username);
                throw new RemoteException("User not found: " + username);
            }
        } else {
            System.out.println("Unauthorized attempt to update permissions by " + (admin != null ? admin.getUsername() : "null"));
            throw new RemoteException("Only manager can update user permissions.");
        }
    }

    @Override
    public User findUserByUsername(String username) throws RemoteException {
        for (User user : users.values()) {

            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null; // المستخدم غير موجود
    }

    @Override
    public String uploadFile(String token, String department, String fileName, byte[] fileData) throws RemoteException {

        if (isAuthorized(token, department, "upload")) {

            // وزّع القسم حسب hash مثلاً
            int nodeIndex = getNodeIndex(department, fileName);

            nodes.get(nodeIndex).uploadFile(department, fileName, fileData);
            System.out.println("File uploaded: " + fileName + " to " + department + " via Node " + (nodeIndex + 1));
            return "Sucess";
        }
        return "Unauthorized to upload to this department.";
//        else {
//            throw new RemoteException("Unauthorized to upload to this department.");
//        }
    }

    @Override
    public void deleteFile(String token, String department, String fileName) throws RemoteException {
        if (isAuthorized(token, department, "delete")) {
            int nodeIndex = getNodeIndex(department, fileName);

            nodes.get(nodeIndex).deleteFile(department, fileName);
            System.out.println("File deleted: " + fileName + " from " + department);
        } else {
            throw new RemoteException("Unauthorized to delete from this department.");
        }
    }

    @Override
    public void editFile(String token, String department, String fileName, byte[] newData) throws RemoteException {
        if (isAuthorized(token, department, "edit")) {
            int nodeIndex = getNodeIndex(department, fileName);
            nodes.get(nodeIndex).editFile(department, fileName, newData);
            System.out.println("File edited: " + fileName + " in " + department);
        } else {
            throw new RemoteException("Unauthorized to edit in this department.");
        }
    }


    @Override
    public List<String> listFiles(String token, String department) throws RemoteException {
        if (!isAuthorized(token, department, "list")) {
            throw new RemoteException("Unauthorized to list files from this department.");
        }

        Set<String> allFiles = new HashSet<>();

        for (NodeInterface node : nodes) {
            try {
                List<String> nodeFiles = node.listFiles(department);
                allFiles.addAll(nodeFiles); // يضيف بدون تكرار
            } catch (RemoteException e) {
                System.err.println("Node failed to respond during list: " + e.getMessage());
            }
        }

        return new ArrayList<>(allFiles);
    }


    @Override
    public byte[] downloadFile(String token, String department, String fileName) throws RemoteException {
        if (!isAuthorized(token, department, "download")) {
            throw new RemoteException("Unauthorized to download from this department.");
        }


        for (NodeInterface node : nodes) {
            try {
                byte[] data = node.downloadFile(department, fileName);
                if (data != null) {
                    System.out.println("File " + fileName + " found in one of the nodes.");
                    return data;
                }
            } catch (RemoteException e) {
                // العقدة ممكن ما يكون فيها الملف، عادي نتجاهل هاد النوع من الأخطاء
            }
        }

        throw new RemoteException("File not found in any node.");
    }
    //talab4

    @Override
    public String findFileLocation(String fileName) throws RemoteException {
        int maxAttempts = registeredNodes.size(); // عدد العقد الكلي
        Set<String> triedNodes = new HashSet<>();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            NodeInterface selectedNode = getLeastLoadedNode(triedNodes);

            if (selectedNode == null) {
                System.out.println("❌ No available nodes to handle the request.");
                break;
            }

//            NodeInterface selectedNode = getLeastLoadedNode();
        if (selectedNode != null){
            String nodeId = "";
            String nodeName="";
            if (selectedNode.getFolderPath().equals("node1_storage")) {
                nodeId = "6001";
                nodeName="node1";
            } else if (selectedNode.getFolderPath().equals("node2_storage")) {
                nodeId = "6002";
                nodeName="node2";
            } else if (selectedNode.getFolderPath().equals("node3_storage")) {
                nodeId = "6003";
                nodeName="node3";
            }
            triedNodes.add(nodeId);
            incrementLoad(nodeId);
            try {

                try {
                    if (selectedNode.hasFile(fileName)) {
                        return nodeId+"/"+nodeName;
//                    return "node" + (i + 1); // مثال: node1 أو node2
                    }
                } catch (RemoteException e) {
                    System.err.println("Failed to contact " + nodeName + " during file search.");
                    // نزيلها من القائمة في حال فشلها
                    markNodeAsFailed(nodeId);
                }
            } finally {
                decrementLoad(nodeId);
            }
        }else {
        System.out.println("No nodes available for search");
        return null;
    }}
//        if (selectedNode != null){
//
//            String nodeId = "";
//
//            incrementLoad(nodeId);
//
            System.out.println("❌ All attempts to search file failed.");

return null;

    }




//    private boolean isAuthorized(User user, String department, String action) {
//        return user != null &&
//                user.getDepartment().equals(department) &&
//                user.getPermissions().contains(action);
//    }

    public boolean isAuthorized(String token, String department, String requiredPermission) {
        String username = tokenToUsername.get(token);
        if (username == null) return false;

        User user = users.get(username);
        return user != null &&
                department.equals(user.getDepartment()) &&
                user.getPermissions().contains(requiredPermission);
    }


    public User getUserByToken(String token) throws RemoteException {
        String username = tokenToUsername.get(token);
        if (username != null) {
            return users.get(username);
        }
        return null;
    }



    private int getNodeIndex(String department, String fileName) {
        return Math.abs((department + "_" + fileName).hashCode()) % nodes.size();
    }


        public synchronized NodeInterface getLeastLoadedNode(Set<String> excludedNodes) {
            return registeredNodes.entrySet().stream()
                    .filter(entry -> !excludedNodes.contains(entry.getKey()))
                    .filter(entry -> !failedNodes.contains(entry.getKey()))
                    .min(Comparator.comparingInt(entry -> nodeLoadMap.get(entry.getKey())))
                    .map(Map.Entry::getValue)
                    .orElse(null);
        }


        public synchronized void incrementLoad(String nodeId) {
        nodeLoadMap.put(nodeId, nodeLoadMap.get(nodeId) + 1);
    }

    public synchronized void decrementLoad(String nodeId) {
        nodeLoadMap.put(nodeId, nodeLoadMap.get(nodeId) - 1);
    }
        public synchronized void markNodeAsFailed(String nodeId) {
            failedNodes.add(nodeId);
        }

    }
