package com.credix.edcHtmlToPdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Logger {

	private static final String PROJECT_DIR = System.getProperty("user.dir");
	private static final String LOG_FOLDER = PROJECT_DIR + File.separator + "logs";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String LOG_FILE = "Log_" + DATE_FORMAT.format(new Date()) + ".txt";
	private static final String LOG_ERROR_FILE = "LogError_" + DATE_FORMAT.format(new Date()) + ".txt";
	private final ExecutorService logger = Executors.newSingleThreadExecutor();

	public Logger() {
		File logDir = new File(LOG_FOLDER);
		if (!logDir.exists()) {
			if (logDir.mkdirs()) {
				System.out.println("Carpeta de logs creada en: " + LOG_FOLDER);
			} else {
				System.err.println("No se pudo crear la carpeta de logs en: " + LOG_FOLDER);
			}
		}
	}

	/*type 0 normal, 1 error*/
	public void write(String text, int type) {
		final String textLog = text;
		final int typeLog = type;
		ExecutorService logger = Executors.newSingleThreadExecutor();
		logger.execute(() -> {
		            try {
		            	String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						String logFile = (typeLog  == 0) ? LOG_FILE : LOG_ERROR_FILE;
						File logFilePath = new File(LOG_FOLDER, logFile);
						try (FileWriter fw = new FileWriter(logFilePath, true)) {
							fw.write(dateTime + " " + textLog);
							fw.write(System.lineSeparator());
						}
		            } catch (IOException e) {
		                e.printStackTrace();
		            }

		    });
	}
}