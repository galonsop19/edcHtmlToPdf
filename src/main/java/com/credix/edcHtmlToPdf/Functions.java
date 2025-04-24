package com.credix.edcHtmlToPdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class Functions {

	public void deleteContentDirectory(String directory) {
		Logger logger = new Logger();
		File dir = new File(directory);
		String[] archivos = dir.list();
		logger.write("Intentando limpiar directorio: " + directory, 0);
		logger.write("Cantidad de archivos encontrados: " + archivos.length, 0);
		if (archivos.length > 0) {
			File archivo;
			for (int x = 0; x < archivos.length; x++) {
				String filePath = directory + "/" + archivos[x];
				archivo = new File(filePath);
				archivo.delete();
			}
			archivos = dir.list();
			if (archivos.length < 1) {
				logger.write("Elementos eliminados", 0);
			} else {
				logger.write("Ocurrio un problema al eliminar los archivos", 0);
				System.exit(0);
			}
		} else {
			logger.write("No hay archivos que eliminar", 0);
		}
	}

	public int getQuantityFilesOfDirectory(String directory) {
		File dir = new File(directory);
		String[] archivos = dir.list();
		int quantity = 0;
		for (String archivo : archivos) {
			File f = new File(directory + "/" + archivo);
			if (f.isFile() && archivo.endsWith(".html") && !archivo.equals("desktop.ini")) {
				quantity++;
			}
		}
		return quantity;
	}


	public ArrayList<String> getFilesOfDirectory(String directory) {
		File dir = new File(directory);
		String[] files = dir.list();
		ArrayList<String> returnFiles = new ArrayList<String>();
		for (int x = 0; x < files.length; x++) {
			File file = new File(directory + "/" + files[x]);
			if (file.isFile()) {
				returnFiles.add(files[x]);
			}
		}
		return returnFiles;
	}

	public void copyFilesOfDirectory(String origDirectory, String destDirectory) {
		File dir = new File(origDirectory);
		String[] files = dir.list();
		for (int x = 0; x < files.length; x++) {
			File file = new File(origDirectory + "/" + files[x]);
			if (file.isFile()) {
				Path FROM = Paths.get(origDirectory + "/" + files[x]);
				Path TO = Paths.get(destDirectory + "/" + files[x]);
				CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, };
				try {
					Files.copy(FROM, TO, options);
				} catch (IOException e) {
					System.out.println("Ocurrio un problema: " + e.getMessage());
					System.exit(0);
				}
			}
		}
	}

	public String readFile(String file) throws IOException {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			return content.toString();
		}
	}

	public void createDirectory(String destDirectory, String name) {
		File dir = new File(destDirectory + "/" + name);
		dir.mkdir();
	}
}
