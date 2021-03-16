package edu.osu.sec.vsa.utility;

import java.lang.Thread.UncaughtExceptionHandler;

import static edu.osu.sec.vsa.main.Main.wf;

public class ErrorHandler implements UncaughtExceptionHandler {
	String tag;

	public ErrorHandler(String tag) {
		this.tag = tag;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("File: " + tag);
		sb.append('\n');
		sb.append("Msge: " + e.getMessage());
		sb.append('\n');
		for (StackTraceElement st : e.getStackTrace()) {
			sb.append("     " + st.toString());
			sb.append('\n');
		}
		wf(sb.toString());
		Logger.printE(sb.toString());

	}

}
