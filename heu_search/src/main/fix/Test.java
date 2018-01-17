package fix;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class Test {
    public static void main(String[] args){
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File com=fsv.getHomeDirectory();    //这便是读取桌面路径的方法了
        System.out.println(com.getPath());
    }
}
