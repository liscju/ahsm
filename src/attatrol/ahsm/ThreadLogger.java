package attatrol.ahsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Logger, that doesn't lock threads.
 * <p>
 * Reason: synchronized singleton logger has 2 disadvantages:<br>
 * 1. Threads have to wait for it, thus it can significantly slow them and break
 * their logic schedule;<br>
 * 2. It is not necessary to access the log in real time.
 * </p>
 * Idea: create a logger for each thread, then unite their records in one single
 * log on demand.
 * <p>
 * 
 * @author atta_troll
 *
 */

public class ThreadLogger{

	private static long START_TIME_POINT = System.currentTimeMillis();

	private static List<ThreadLogger> threadLoggers = new ArrayList<ThreadLogger>();

	private String threadName;
	private List<String> records = new LinkedList<String>();

	public static ThreadLogger getLogger(String threadName) {
		final ThreadLogger logger = new ThreadLogger(threadName);
		threadLoggers.add(logger);
		return logger;
	}

	/**
	 * Returns all logs sorted by time, cleans them
	 */
	public static String dumpLog() {
		List<String> log = new LinkedList<String>();
		for (ThreadLogger logger : threadLoggers) {
			log.addAll(logger.records);
			logger.clear();
		}
		Collections.sort(log);
		StringBuilder sb = new StringBuilder();
		for (String rec : log) {
			sb.append(rec).append('\n');
		}
		return sb.toString();
	}

	private ThreadLogger(String threadName) {
		this.threadName = threadName;
	}

	public String getThreadName() {
		return threadName;
	}

	public void logMessage(String message) {
		records.add(String.format("%10dms: %s  says:\"%s\"",
		        System.currentTimeMillis() - START_TIME_POINT, threadName, message));
	}
	public void clear() {
		records.clear();
	}
	
	public void setThreadName(String newThreadName) {
		threadName = newThreadName;
	}

}
