package edu.osu.sec.vsa.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipException;

import com.sun.xml.internal.ws.client.sei.ValueSetter;
import org.json.JSONArray;
import org.json.JSONObject;

import soot.Scene;
import soot.Value;
import soot.jimple.parser.node.TStrictfp;
import soot.options.Options;
import brut.androlib.AndrolibException;
import edu.osu.sec.vsa.base.GlobalStatistics;
import edu.osu.sec.vsa.graph.CallGraph;
import edu.osu.sec.vsa.graph.DGraph;
import edu.osu.sec.vsa.graph.IDGNode;
import edu.osu.sec.vsa.graph.ValuePoint;
import edu.osu.sec.vsa.utility.ErrorHandler;
import edu.osu.sec.vsa.utility.FileUtility;
import edu.osu.sec.vsa.utility.Logger;

public class Main {

	static JSONObject targetMethds;	//目标方法

	public static void startWatcher(int sec) {
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(sec * 1000);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				wf("TimeOut!!!",false);
				System.exit(0);
			}
		};
		t.setDaemon(true);
		t.start();
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


	public static void main(String[] args) throws ZipException, IOException, AndrolibException {
		initDirs();		//新建output，log文件。
		List<String> allOutPutFile = getAllFile("./valuesetResult",false);
		Config.ANDROID_JAR_DIR = args[0];//arg0为android.jar包的路径
		
		targetMethds = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1]))));//arg1为json文件路径，读取信息,并转换为json格式
		JSONArray apks = targetMethds.getJSONArray("apk");
		for(Object apk : apks) {
			Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(args[0]));//设置异常处理
			ApkContext apkcontext = ApkContext.getInstance((String) apk);//创建apkContext对象，path成员初始化

			if(allOutPutFile.contains(ApkContext.apkcontext.getPackageName())){	//如果有重复输出，则跳过该文件
				continue;
			}

			Logger.TAG = apkcontext.getPackageName();

			soot.G.reset();            //soot.G.reset()是一个标准的soot操作，用于清空soot之前所有操作遗留下的缓存值
			Options.v().set_src_prec(Options.src_prec_apk);            //是否记录代码所在行
			Options.v().set_process_dir(Collections.singletonList(apkcontext.getAbsolutePath()));//待反编译文件所在的文件夹，此处是apk文件地址
			//Options.v().set_android_jars(Config.ANDROID_JAR_DIR);//表示在该路径下寻找android.jar文件
			Options.v().set_force_android_jar(Config.ANDROID_JAR_DIR);//表示绝对路径在该路径下寻找android.jar文件
			Options.v().set_process_multiple_dex(true);
			Options.v().set_whole_program(true);        //以全局应用的模式运行
			Options.v().set_allow_phantom_refs(true);    //表示是否加载未被解析的类
			Options.v().set_output_format(Options.output_format_none);//设置输出的格式，此处设置为不输出，因此不会输出反编译后的jimple文件
			Options.v().ignore_resolution_errors();
			try {
				Scene.v().loadNecessaryClasses();        // //使用soot反编译dex文件，并将反编译后的文件加载到内存中
			} catch (Exception e){
				Logger.printW("LoadNecessaryClasses failed");
				wf("LoadNecessaryClasses failed",true);
				continue;
			}

			startWatcher(Config.TIMEOUT);
			CallGraph.init();                        //生成函数调用图
			//startWatcher(20);

			DGraph dg = new DGraph();    //生成数据依赖图

			List<ValuePoint> allvps = new ArrayList<ValuePoint>();
			List<ValuePoint> vps;
			String tsig;            //存储目标函数的三地址码
			List<Integer> regIndex;//存储目标函数的参数位置
			JSONObject tmp;

			for (Object jobj : targetMethds.getJSONArray("methods")) {
				tmp = (JSONObject) jobj;
				tsig = tmp.getString("method");    //遍历输入json文件的method的三字节码
				regIndex = new ArrayList<Integer>();

				for (Object tob : tmp.getJSONArray("parmIndexs")) {    //获得参数索引
					regIndex.add((Integer) tob);
				}

				vps = ValuePoint.find(dg, tsig, regIndex); //目标方法作为第一个ValuePoint,进入DGragh,
				allvps.addAll(vps);
			}


			wf("====>" + ApkContext.apkcontext.getPackageName() + " <====\n", false);
			Vector<String> list = new Vector<String>();        //储存获得的http信息
			try{
				dg.solve(allvps);
			} catch (Exception e){
				wf("Error dg",true);
			}
			finally {
				printHttp(dg, list);
			}
		}

	}


	public static void wf(String content,boolean append) {
		FileUtility.wf(Config.RESULTDIR + ApkContext.getInstance().getPackageName(), content, append);
	}

	public static void initDirs() {
		File tmp = new File(Config.RESULTDIR);//output file
		if (!tmp.exists())
			tmp.mkdir();
		tmp = new File(Config.LOGDIR);//log file
		if (!tmp.exists())
			tmp.mkdir();
	}


	public static void printHttp(DGraph dg,Vector<String> list){
		//输出http信息
		for(Object vp : dg.getNodes()){
			if(vp instanceof ValuePoint){
				for(HashMap<Integer, HashSet<String>> var : ((ValuePoint) vp).getResult()){
					for(int i : var.keySet()){
						for(String s : var.get(i)){
							if(s.contains("http") && !list.contains(s)){
								list.add(s);
								System.out.println(s);
								wf(s,true);
							}
						}
					}
				}
			}
		}
	}


}



