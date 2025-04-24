package com.credix.edcHtmlToPdf;

import java.io.IOException;
import java.util.ArrayList;

public class ProcessThread extends Thread {
	
	private ArrayList<String> files = new ArrayList<String>();
	private String rutaEdcOrig = "";
	private String rutaEdcPdf = "";
	private Logger logger = new Logger();
	private int threadNumber;
	
	public ProcessThread(ArrayList<String> files, String ruta_edc_orig, String ruta_edc_pdf, int threadNumber) {
		this.files = files;
		this.rutaEdcOrig = ruta_edc_orig;
		this.rutaEdcPdf = ruta_edc_pdf;
		this.threadNumber = threadNumber;
	}
	
	public void run() {
		for(int x = 0; x < files.size(); x++) {
			Pdf pdf = new Pdf();
			logger.write("Hilo #"+threadNumber+ " Creando archivo pdf de " + files.get(x), 0);
			try {
				pdf.createPdfFromHtml(rutaEdcOrig+"/"+files.get(x), rutaEdcPdf,
						files.get(x).replace("html", "pdf"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}