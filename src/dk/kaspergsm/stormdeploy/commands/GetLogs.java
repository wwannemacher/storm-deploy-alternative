package dk.kaspergsm.stormdeploy.commands;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called to get all logs and merge into one file
 * 
 * @author Kasper Grud Skat Madsen
 */
public class GetLogs {
	private static List<LogMonitor> _logReaders = Collections.synchronizedList(new ArrayList<LogMonitor>());
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd k:m:s");
	private static Logger log = LoggerFactory.getLogger(GetLogs.class);
	private static volatile boolean _initialized = false;
	private static ComputeServiceContext _computeContext;
	private static String _clustername;
	private static Timer _t;
		
	@SuppressWarnings("resource")
	public static void run(String clustername, ComputeServiceContext computeContext) {
		_t = new Timer();
		_clustername = clustername;
		_computeContext = computeContext;

		/**
		 * Schedule periodic updating of logs being monitored
		 *  Updated every 2 minutes
		 */
		_t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				for (NodeMetadata n : (Set<NodeMetadata>) _computeContext.getComputeService().listNodes()) {			
					if (n.getStatus() != Status.TERMINATED && n.getGroup() != null && n.getGroup().toLowerCase().equals(_clustername.toLowerCase())) {					
						String host = n.getPublicAddresses().iterator().next();
						for (String logFile : getLogfiles(host)) {
							if (!isMonitoring(host, logFile)) {
								log.info("Monitoring " + host + ":" + logFile);
								monitorLog(host, logFile);
							}
						}
					}
				}
				_initialized = true;
			}
		}, 0, 1000*60*2);
		
		
		/**
		 * Open file to contained merged logs
		 */
		FileWriter writer;
		try {
			String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new GregorianCalendar().getTime());
			log.info("Writing to " + "log_" + ts + ".log");
			writer = new FileWriter("log_" + ts + ".log", false);
		} catch (IOException ex) {
			log.error("Logfile could not be opened for writing", ex);
			return;
		}
		
		
		/**
		 * Wait until all logs are being monitored
		 */
		log.info("Connecting to instances");
		while (!_initialized) {
			try {
				Thread.sleep(100);
			} catch (Exception ex) {}
		}
		
		
		/**
		 * Merge logs
		 */
		log.info("Connections initialized");
		while (true) {
			try {

				// Sort
				// TODO: This is not optimal. Only first element needs to be sorted.
				Collections.sort(_logReaders);
				
				// Get next log (order is based on timestamps)
				LogMonitor l = _logReaders.get(0);
				if (l == null || l.peekNextTimestamp() == null) {
					Thread.sleep(100);
					continue;
				}

				// Write content of log
				for (String line : l.getLines())
					writer.write(line + "\n");
				writer.flush();
			} catch (Exception ex) {
				log.error("Exception", ex);
			}
		}
	}

	/**
	 * Call to get logfiles on node
	 */
	public static List<String> getLogfiles(String host) {
		CommandLine command = new CommandLine("ssh");
		command.addArgument("-o");
		command.addArgument("UserKnownHostsFile=/dev/null");
		command.addArgument("-o");
		command.addArgument("StrictHostKeyChecking=no");
		command.addArgument("-n");
		command.addArgument("ubuntu@" + host);
		command.addArgument("ls");
		command.addArgument("/home/ubuntu/storm/logs/*");

		LogMonitor outStream = new LogMonitor(null,null);
		try {
			DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(new PumpStreamHandler(outStream));
			executor.execute(command);
		} catch (ExecuteException ex) {
		} catch (IOException ex) {}

		return outStream.getLinesRaw();
	}

	/**
	 * Read logfiles
	 */
	public static void monitorLog(String host, String logfile) {
		CommandLine command = new CommandLine("ssh");
		command.addArgument("-o");
		command.addArgument("UserKnownHostsFile=/dev/null");
		command.addArgument("-o");
		command.addArgument("StrictHostKeyChecking=no");
		command.addArgument("-n");
		command.addArgument("ubuntu@" + host);
		command.addArgument("tail");
		command.addArgument("-n");
		command.addArgument("+0");
		command.addArgument("-f");
		command.addArgument(logfile);
		command.addArgument("&");

		LogMonitor outStream = new LogMonitor(host,logfile);
		try {
			DefaultExecutor executor = new DefaultExecutor();
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			executor.setStreamHandler(new PumpStreamHandler(outStream));
			executor.execute(command, resultHandler);
			_logReaders.add(outStream);
		} catch (ExecuteException ex) {
			log.error("Problem", ex);
		} catch (IOException ex) {
			log.error("Problem", ex);
		}
	}

	static class LogMonitor extends LogOutputStream implements Comparable<LogMonitor> {
		private LinkedBlockingQueue<String> out = new LinkedBlockingQueue<String>();
		private String host, logfile;
		
		public LogMonitor(String host, String logfile) {
			this.host = host;
			this.logfile = logfile;
		}
		
		public String getHost() {
			return this.host;
		}
		
		public String getLogFile() {
			return this.logfile;
		}
		
		@Override
		protected void processLine(String content, int l) {
			if (content.contains("Warning: Permanently added"))
				return;
			out.add(content);
		}

		public String peekNextTimestamp() {
			if (out.size() == 0)
				return null;
			return stripTimestamp(out.peek());
		}

		private String stripTimestamp(String line) {
			try {
				int indexOfSpace = line.indexOf(" ");
				return line.substring(0, line.indexOf(" ", indexOfSpace + 1)).trim();
			} catch (Exception ex) {
				return null;
			}
		}

		public List<String> getLinesRaw() {
			ArrayList<String> ret = new ArrayList<String>();
			while (!out.isEmpty()) {
				String o = out.poll();
				if (o.startsWith("ls: cannot access"))
					continue;
				ret.add(o);
			}
			return ret;
		}

		public List<String> getLines() {
			ArrayList<String> ret = new ArrayList<String>();
			while (!out.isEmpty()) {
				// Add line
				ret.add(out.poll());

				// If no more lines, break
				if (out.peek() == null)
					break;

				// If next line has ts, break
				try {
					String stripTs = stripTimestamp(out.peek());
					if (stripTs == null)
						continue;
					
					format.parse(stripTs);
				} catch (ParseException e) {
					continue;
				}

				break;
			}

			return ret;
		}

		@Override
		public int compareTo(LogMonitor o) {
			String ts1 = peekNextTimestamp();
			String ts2 = o.peekNextTimestamp();

			if (ts1 == null && ts2 == null)
				return 0;

			if (ts1 == null && ts2 != null)
				return 1;

			if (ts1 != null && ts2 == null)
				return -1;

			try {
				return format.parse(ts1).compareTo(format.parse(ts2));
			} catch (ParseException e) {
				log.error("Error parsing timestamp of log!");
				return 0;
			}
		}
	}

	public static boolean isMonitoring(String host, String log) {
		Iterator<LogMonitor> i = _logReaders.iterator();
		while (i.hasNext()) {
			LogMonitor lm = i.next();
			if (lm.getHost().equals(host) && lm.getLogFile().equals(log))
				return true;
		}
		return false;
	}
}
