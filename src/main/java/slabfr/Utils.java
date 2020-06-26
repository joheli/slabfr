package slabfr;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

public class Utils {

	public static File getLatestFile(List<File> fl) {
		return getFile(fl, true);
	}

	public static File getLatestFile(File[] fl) {
		List<File> f = Arrays.asList(fl);
		return getLatestFile(f);
	}

	public static File getOldestFile(List<File> fl) {
		return getFile(fl, false);
	}

	public static File getOldestFile(File[] fl) {
		List<File> f = Arrays.asList(fl);
		return getOldestFile(f);
	}

	private static File getFile(List<File> fl, boolean latest) {
		File returnFile = null;
		long lastModified;
		int modx;
		Iterator<File> it = fl.iterator();

		if (latest) {
			lastModified = 0;
			modx = 1;
		} else {
			lastModified = System.currentTimeMillis();
			modx = -1;
		}

		while (it.hasNext()) {
			File f = it.next();
			if ((f.lastModified() * modx) > (lastModified * modx)) {
				returnFile = f;
				lastModified = f.lastModified();
			}
		}

		return returnFile;
	}

	public static int exec(File execFile, String[] args)
			throws ExecuteException, IOException, InterruptedException {
		Map<String, File> fileMap = new HashMap<String, File>();
		CommandLine cL = new CommandLine(execFile);
		int i = 1;
		for (String a : args) {
			if (new File(a).exists()) {
				File f = new File(a);
				String fileKey = "file" + i;
				fileMap.put(fileKey, f);
				cL.addArgument("${" + fileKey + "}");
				i++;
			} else {
				cL.addArgument(a);
			}
		}
		cL.setSubstitutionMap(fileMap);
		return processExec(cL);
	}

	public static int exec(String line) throws ExecuteException, IOException,
			InterruptedException {
		CommandLine cL = CommandLine.parse(line);
		return processExec(cL);
	}

	private static int processExec(CommandLine cL) throws ExecuteException,
			IOException, InterruptedException {
		DefaultExecuteResultHandler rH = new DefaultExecuteResultHandler();
		ExecuteWatchdog wD = new ExecuteWatchdog(60 * 1000);

		DefaultExecutor ex = new DefaultExecutor();
		ex.setWatchdog(wD);
		ex.execute(cL, rH);

		rH.waitFor();
		int exitValue = rH.getExitValue();
		return exitValue;
	}

	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException n) {
			return false;
		}
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException n) {
			return false;
		}
	}

	public static boolean isNumeric(String s) {
		return (isInt(s) || isDouble(s));
	}

}
