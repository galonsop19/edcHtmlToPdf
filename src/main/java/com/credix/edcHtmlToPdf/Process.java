package com.credix.edcHtmlToPdf;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
@Component
public class Process {

	Params paramsObj;
	Input inputObj;
	Parameters parameters;
	Functions functions = new Functions();
	Logger logger = new Logger();
	
	public void printProcess() {
		parameters = new Parameters();
		paramsObj = parameters.getParameters();
		inputObj = parameters.getInputParams();

		final String RUTA_EDC_PDF_OUT = paramsObj.getRutaEdcPdf() + "/" + inputObj.getAno() + "/" + inputObj.getMes() + "/" + inputObj.getCartera();
		File outputDir = new File(RUTA_EDC_PDF_OUT);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		int quantity = functions.getQuantityFilesOfDirectory(paramsObj.getRutaEdcOrig());
		logger.write("Cantidad de HTML a procesar: " + quantity, 0);
		System.out.println("Cantidad de HTML a procesar: " + quantity);

		int quantity_by_hilo = quantity / inputObj.getHilos();
		int quantity_last_hilo = quantity - quantity_by_hilo * (inputObj.getHilos() - 1);
		logger.write("Cantidad por hilo: " + quantity_by_hilo, 0);
		System.out.println("Cantidad por hilo: " + quantity_by_hilo);
		logger.write("Cantidad ultimo hilo: " + quantity_last_hilo, 0);
		System.out.println("Cantidad ultimo hilo: " + quantity_last_hilo);
		logger.write("Inicia proceso de generacion multihilo de Estados de Cuenta", 0);
		System.out.println("Inicia proceso de generacion multihilo de Estados de Cuenta");
		openPrintPdfThreads(inputObj.getHilos(), quantity_by_hilo, quantity, paramsObj.getRutaEdcOrig(), RUTA_EDC_PDF_OUT);
	}
	/**
	 * Crea arrays con la cantidad de archivos a              listo pa
	 * procesar por cada hilo de trabajo
	 * @param hilos
	 * @param quantity_by_hilo
	 * @param quantity
	 * @param rutaEdcOrig
	 */
	public void openPrintPdfThreads(int hilos, int quantity_by_hilo, int quantity, String ruta_edc_orig, 
			String ruta_edc_pdf) {
		ArrayList<String> files = functions.getFilesOfDirectory(ruta_edc_orig);
		int currentIndice = 0;
		ArrayList<String> log = new ArrayList<String>();
		for(int x = 1; x < hilos; x++) {
			ArrayList<String> htmlGroup = new ArrayList<String>();
			for(int y = 0; y < quantity_by_hilo ; y++) {
				htmlGroup.add(files.get(currentIndice));
				log.add(files.get(currentIndice));
				currentIndice++;
			}
			ProcessThread thread = new ProcessThread(htmlGroup, ruta_edc_orig, ruta_edc_pdf, x);
			thread.start();
		}
		//Todos los valores sobrante del array se envian en un ultimo proceso
		int faltantes = quantity - currentIndice;
		ArrayList<String> htmlGroup = new ArrayList<String>();
		for(int x = 0; x < faltantes; x++) {
			htmlGroup.add(files.get(currentIndice));
			log.add(files.get(currentIndice));
			currentIndice++;
		}
		ProcessThread thread = new ProcessThread(htmlGroup, ruta_edc_orig, ruta_edc_pdf, hilos);
		thread.start();
	}

}
