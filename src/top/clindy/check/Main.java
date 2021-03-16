package top.clindy.check;

import brut.androlib.AndrolibException;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.directory.ExtFile;
import edu.osu.sec.vsa.utility.Logger;
import soot.Scene;
import soot.jimple.infoflow.android.axml.ApkHandler;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.data.FlowDroidMemoryManager;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import soot.jimple.infoflow.android.resources.ARSCFileParser;


import soot.jimple.infoflow.android.resources.ARSCFileParser;
//import soot.jimple.infoflow.android.data.AndroidMemoryManager;

public class Main {
    static String ProcessDir = "./example/app/eye.apk";
    static String AndroidJar = "./libs/android.jar";
    static String TargetMethod = "<java.net.URL: void <init>(java.lang.String)>";
    static List<Integer> RegIndex;
    static ApkHandler ah = null;

    static ARSCFileParser asrc = null;
    static ProcessManifest mfest = null;
    static ResPackage[] resps = null;		//brut.androlib.res.data
    //static AndroidMemoryManager amm = null;


    public static void main(String[] arg) throws IOException, AndrolibException {
        soot.G.reset();
        Options.v().set_src_prec(Options.src_prec_apk);			//是否记录代码所在行
        Options.v().set_process_dir(Collections.singletonList(ProcessDir));
        Options.v().set_force_android_jar(AndroidJar);
        Options.v().set_whole_program(true);		//以全局应用的模式运行
        Options.v().set_allow_phantom_refs(true);	//表示是否加载未被解析的类`
        Options.v().set_process_multiple_dex(true);
        Options.v().ignore_resolution_errors();

        Scene.v().loadNecessaryClasses();		//使用soot反编译dex文件，并将反编译后的文件加载到内存中

        init();
        Logger.print("Start!");

        Map<Integer,String> gsp = asrc.getGlobalStringPool();


        Logger.print("End!");
    }

    public static void init() throws IOException, AndrolibException {

        ah = new ApkHandler(ProcessDir);                           //加载apkhandler
        asrc = new ARSCFileParser();
        asrc.parse(ah.getInputStream("resources.arsc"));

        try {
            mfest = new ProcessManifest(ah.getInputStream("AndroidManifest.xml"));    //尝试加载AndroidManifest.xml;
        } catch (Exception e) {
            System.out.println("Cant't Solve AndroidManifest");
        }

        ExtFile apkFile = new ExtFile(new File(ProcessDir));            //打开文件
        AndrolibResources res = new AndrolibResources();
        ResTable resTab = res.getResTable(apkFile, true);
        resps = res.getResPackagesFromApk(apkFile, resTab, true);

        //amm = new AndroidMemoryManager(true,pdem,);

        ah.close();     //信息加载完后完后关闭它。


        System.out.println("FileName : " + ah.getFilename().toString());
        System.out.println("Path : " + ah.getPath().toString());
        System.out.println("AbsolutePath : " + ah.getAbsolutePath().toString());



    }
}
