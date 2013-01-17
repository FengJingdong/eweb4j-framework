package org.eweb4j.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eweb4j.config.bean.LogConfigBean;
import org.eweb4j.config.bean.LogsConfigBean;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.FileUtil;

public class LogImpl implements Log {
	private Class<?> clazz;
	private String module = "";
	private LogsConfigBean logs;

	public LogImpl(LogsConfigBean logs, String module, Class<?> clazz) {
		this.logs = logs;
		this.clazz = clazz;
		if (module != null && module.trim().length() > 0)
			this.module = " [" + module + "]";
	}

	public LogImpl() {
	}

	private String write(final int level, final String mess) {
		if (logs == null || logs.getLog() == null)
			return mess;

		StringBuilder result = new StringBuilder();
		for (LogConfigBean log : logs.getLog()) {
			/* 如果当前给定的日志级别小于配置的允许级别，不予记录 */
			if (LogLevel.level(log.getLevel()) == 0 || level < LogLevel.level(log.getLevel())) {
				return mess;
			}

			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(LogLevel.level(level).toUpperCase());
			sb.append("] ");
			sb.append(CommonUtil.getNowTime("HH:mm:ss"));
			sb.append(this.module.toLowerCase());
			sb.append(" ~ ");
			Exception e = new Exception(this.clazz.getName());
			StackTraceElement s = e.getStackTrace()[2];
			String m = s.getClassName() + "." + s.getMethodName();
			sb.append(s.toString().replace(m, ""));
			sb.append(" ");
			sb.append(mess);

			result.append(sb.toString());
			
			if ("1".equals(log.getConsole()) || "true".equalsIgnoreCase(log.getConsole())){
				if (level > 3)
					System.err.println(sb.toString());
				else
					System.out.println(sb.toString());
			}

			BufferedWriter bw = null;
			try {
				if (log.getFile() != null) {
					File file = new File(ConfigConstant.CONFIG_BASE_PATH + log.getFile());
					if (!file.exists()) {
						FileUtil.createFile(log.getFile());
					}

					if (file.length() / (1024 * 1024) >= Integer.parseInt(log
							.getSize())) {
						File tf = new File(file.getAbsolutePath() + "." + CommonUtil.getNowTime("_MMddHHmmss"));
						FileUtil.copy(file, tf);
						file.delete();
						file = null;
						file = new File(log.getFile());
					}

					bw = new BufferedWriter(new FileWriter(file, true));

					bw.newLine();
					bw.write(sb.toString());
				}
			} catch (Exception ex) {

			} finally {
				if (bw != null) {
					try {
						bw.flush();
						bw.close();
					} catch (IOException ex) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return result.toString();
	}

	public String debug(String debug) {
		return this.write(1, debug);
	}

	public String info(String info) {
		return this.write(2, info);
	}
	
	public String warn(String warn) {
		return this.write(3,  warn);
	}
	
	public String warn(String warn, Exception e) {
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter, true);
		e.printStackTrace(writer);
		StringBuffer sb = strWriter.getBuffer();
		
		String err = warn + " cause by " + sb.toString();
		
		return this.write(3,  err);
	}

	public String error(String error) {
		return this.write(4,  error);
	}
	
	public static void main(String[] args){
		try {
			int i = 10 / 0;
		}catch(Exception e){
			StringWriter strWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(strWriter, true);
			e.printStackTrace(writer);
			StringBuffer sb = strWriter.getBuffer();
			
			String err = " cause by " + sb.toString();
			System.out.println(err);
		}
	}
	
	public String error(String error, Exception e) {
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter, true);
		e.printStackTrace(writer);
		StringBuffer sb = strWriter.getBuffer();
		
		String err = error + " cause by " + sb.toString();
		
		return this.write(4,  err);
	}
	
	public String fatal(String fatal) {
		return this.write(5,  fatal);
	}
	
	public String fatal(String fatal, Exception e) {
		StringWriter strWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(strWriter, true);
		e.printStackTrace(writer);
		StringBuffer sb = strWriter.getBuffer();
		
		String err = fatal + " cause by " + sb.toString();
		
		return this.write(5,  err);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public LogsConfigBean getLogs() {
		return logs;
	}

	public void setLogs(LogsConfigBean logs) {
		this.logs = logs;
	}

}
