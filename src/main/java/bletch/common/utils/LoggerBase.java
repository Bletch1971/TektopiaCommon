package bletch.common.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public abstract class LoggerBase {

	protected Logger modLogger;
	protected File debugFile;
	
	protected void Initialise(String modName, String debugLogFile) {
		modLogger = LogManager.getLogger(modName);
		debugFile = new File(debugLogFile);
	}
	
	public void debug(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.debug(message);
		writeLine("[DEBUG] " + message, true);
	}
	
	public void debug(String message, Boolean checkConfig) {
		debug(message);
	}
	
	public void error(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.error(message);
		writeLine("[ERROR] " + message, true);
	}
	
	public void fatal(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.fatal(message);
		writeLine("[FATAL] " + message, true);
	}
	
	public void info(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.info(message);
		writeLine("[INFO] " + message, true);
	}
	
	public void info(String message, Boolean checkConfig) {
		info(message);
	}
	
	public void output(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.info(message);
		writeLine(message, true);
	}
	
	public void trace(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.trace(message);
		writeLine("[TRACE] " + message, true);
	}
	
	public void warn(String message) {
		if (message == null)
			return;
		
		if (modLogger != null)
			modLogger.warn(message);
		writeLine("[WARN] " + message, true);
	}
	
	public void resetDebug() {
		writeLines(Collections.singletonList("Debug Log:"), false);
	}
	
	public void writeLine(String line, Boolean append) {
		writeLines(Collections.singletonList(line), append);
	}	
	
	public void writeLines(Collection<String> lines, Boolean append) {
		if (debugFile == null)
			return;
		
		try {
			FileUtils.writeLines(debugFile, lines, append);
		} 
		catch (IOException e) {
		}
	}

}
