package fix.io;

public class AddLock {
    public static void addLock(int startLine, int endLine, String lockName,String filePath){
        InsertCode.insert(startLine, "synchronized (" + lockName +"){ ", filePath);
        InsertCode.insert(endLine, "}", filePath);
    }
}
