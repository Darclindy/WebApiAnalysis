package top.clindy.permission;

import brut.androlib.AndrolibException;
//import com.sun.org.apache.xpath.internal.operations.String;
import edu.osu.sec.vsa.main.ApkContext;
import edu.osu.sec.vsa.main.Config;
import edu.osu.sec.vsa.utility.ErrorHandler;
import edu.osu.sec.vsa.utility.FileUtility;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Permission {
    static JSONObject permission;
    static JSONObject permissionUsed;
    static JSONObject targetMethds;	//目标方法

    public static void main(String[] args) throws IOException, AndrolibException {
        initDirs();
        Config.ANDROID_JAR_DIR = args[0];           //arg0为android.jar包的路径
        targetMethds = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1]))));              //arg1为json文件路径，读取信息,并转换为json格式
        permission = new JSONObject(new String(Files.readAllBytes(Paths.get(Config.PERMISSIONDIR))));   //解析权限文件
        JSONArray apks = targetMethds.getJSONArray("apk");

        for(Object apk:apks) {
            ApkContext apkcontext = ApkContext.getInstance((String) apk);//创建apkContext对象，path成员初始化
            try{
                apkcontext.init();
            }catch(Exception e){
                FileUtility.wf(Config.PERMISSION + ApkContext.getInstance().getPackageName(),"SomethingWrong", false);
                continue;
            }

            permissionUsed = new JSONObject();

            for (String a : apkcontext.getPermission()) {
                if (permission.keySet().contains(a)) {
                    //System.out.println(permission.get(a));  //如果有就翻译成中文
                    permissionUsed.put(permission.get(a).toString(), "yes");
                } else {
                    System.out.println(a);   //没有就将英文输出来
                }
            }

            for (String per : permission.keySet()) {
                if(!permissionUsed.keySet().contains(per)){
                    permissionUsed.put(permission.get(per).toString(), "no");   //剩下的选项标记为no
                }
            }

            FileUtility.wf(Config.PERMISSION+ApkContext.getInstance().getPackageName(),apkcontext.getPackageName()+"\n",true);
            FileUtility.wf(Config.PERMISSION + ApkContext.getInstance().getPackageName(), permissionUsed.toString(), false);

        }


    }

    public static void initDirs() {
        File tmp = new File(Config.PERMISSION); //output file
        if (!tmp.exists())
            tmp.mkdir();
        tmp = new File(Config.LOGDIR);          //log file
        if (!tmp.exists())
            tmp.mkdir();
    }

    public static List<String> getAllFile(String directoryPath, boolean isAddDirectory) throws IOException {
        List<String> list = new ArrayList<String>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if(isAddDirectory){
                    list.add("example/" + file.getName());
                }
                list.addAll(getAllFile(file.getAbsolutePath(),isAddDirectory));
            } else {
                list.add(file.getName()); //这是垃圾代码……
            }
        }
        return list;
    }

    /*
    public static List<String> getAllFile(String directoryPath, boolean isAddDirectory) {
        List<String> list = new ArrayList<String>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if(isAddDirectory){
                    list.add(file.getName());
                }
                list.addAll(getAllFile(file.getAbsolutePath(),isAddDirectory));
            } else {
                list.add(file.getName());
            }
        }
        return list;
    }
    */

}
