package fix.entity;

/**
 * 存放要引入的项目的路径
 */
public class ImportPath {
    //要寻找的参数名称
    public static String parametersName = "amount";
    //要加载的examples工程根路径
    public static String examplesRootPath = "D:\\Patch";
    //具体到某个项目的名称
//    public static String projectName = "account";
    public static String projectName = "account";
    //这个项目的主类名称
    public static String mainClassName = "Main";
    //临时文件的目录，不用太在意，反正用完就删
    public static String tempFile = "D:\\Patch\\temp.java";
    //验证程序的路径
    public static String verifyPath = "D:\\Patch";
}
