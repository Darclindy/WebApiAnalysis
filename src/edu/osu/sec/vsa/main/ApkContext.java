package edu.osu.sec.vsa.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import brut.androlib.AndrolibException;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.data.ResID;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.data.value.ResStringValue;
import brut.directory.ExtFile;
import soot.jimple.infoflow.android.axml.ApkHandler;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.android.resources.ARSCFileParser;

public class ApkContext {
	String path = null;
	ApkHandler apkh = null;			//soot.jimple.infoflow.android.axml
	ProcessManifest mfest = null;	//soot.jimple.infoflow.android.manifest
	ARSCFileParser afp = null;		//soot.jimple.infoflow.android.resources ARSC是一个资源索引表，它可以帮助系统根据资源 ID 快速找到资源。
	ResPackage[] resps = null;		//brut.androlib.res.data

	private ApkContext(String path) {
		this.path = path;
	}

	public String getAbsolutePath() throws ZipException, IOException, AndrolibException {
		if (apkh == null) {
			init();
		}

		return apkh.getAbsolutePath();
	}

	public String getPackageName() {
		try {
			if (apkh == null) {
				init();
			}

			if (mfest != null)
				return mfest.getPackageName();

		} catch (Exception e) {
		}

		return null;
	}

	public Set<String> getPermission(){
		return mfest.getPermissions();
	}



	/*
	 * public String getIdentifier(String name, String type, String packageName)
	 * { try { SootClass cls = Scene.v().getSootClass(String.format("%s.R$%s",
	 * (packageName == null || packageName.trim().length() == 0) ?
	 * this.getPackageName() : packageName, type)); SootField sfield =
	 * cls.getFieldByName(name); Tag tag =
	 * sfield.getTag("IntegerConstantValueTag"); int id =
	 * ByteBuffer.wrap(tag.getValue()).getInt(); return id + ""; } catch
	 * (Exception e) { return "-1"; } }
	 */
	/*
	 * public String getIdentifier(String name, String type, String packageName)
	 * { for (ResPackage resPackage : afp.getPackages()) { for (ResType resType
	 * : resPackage.getDeclaredTypes()) { if (resType.toString().equals(type)) {
	 * for (AbstractResource res : resType.getAllResources()) {
	 * Logger.print(res.getResourceName() + " " + res.getResourceID() + " " +
	 * res); if (res.getResourceName().equals(name)) { return
	 * res.getResourceID() + ""; } } } } } return "-1"; }
	 */
	public String getIdentifier(String name, String type, String packageName) {
		for (ResPackage resp : resps) {
			try {
				return resp.getType(type).getResSpec(name).getId().id + "";

			} catch (AndrolibException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "-1";
	}

	public void init() throws ZipException, IOException, AndrolibException {
		apkh = new ApkHandler(path);

		afp = new ARSCFileParser();
		afp.parse(apkh.getInputStream("resources.arsc"));
		try {
			mfest = new ProcessManifest(apkh.getInputStream("AndroidManifest.xml"));
		} catch (Exception e) {
		}
		ExtFile apkFile = new ExtFile(new File(path));

		AndrolibResources res = new AndrolibResources();
		ResTable resTab = res.getResTable(apkFile, true);
		resps = res.getResPackagesFromApk(apkFile, resTab, true);
		apkh.close();
	}

	public String findResource(int id) {
		String str = String.format("[XML String:%s]", id);
		try {
			if (apkh == null) {
				init();
			}
			// str = afp.findResource(id).toString();
			for (ResPackage resp : resps) {
				if (resp.getResSpec(new ResID(id)) != null) {
					str = ((ResStringValue) resp.getResSpec(new ResID(id)).getDefaultResource().getValue()).encodeAsResXmlValue();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}




	static ApkContext apkcontext = null;

	public static ApkContext getInstance(String path) {//输入路径，得到apk实例
		apkcontext = new ApkContext(path);
		return apkcontext;
	}

	public static ApkContext getInstance() {
		return apkcontext;
	}



}
