package com.credix.edcHtmlToPdf;

import java.io.*;
import java.util.Properties;

import com.google.gson.Gson;

public class Parameters {
	static final String CONSTANTS_FILE = "/home/produccion/conf/constants.properties";
	static final String JSON_PARAMS = "edc.json.params";
	static final String JSON_INPUT = "edc.json.input";
	Params paramsObj;
	Input inputObj;
	Gson gson = new Gson();
	Properties constanst;
	public Params getParameters() {
		try {
			constanst = this.readConstants();
			String abPath = new File(constanst.getProperty(JSON_PARAMS)).getAbsolutePath();
			String abPath2 = new File(constanst.getProperty(JSON_INPUT)).getAbsolutePath();

			System.out.print(abPath);
			System.out.print(abPath2);

			BufferedReader br = new BufferedReader(
					new FileReader(constanst.getProperty(JSON_PARAMS)));
			paramsObj = gson.fromJson(br, Params.class);

		} catch(IOException e) {

		}
		return paramsObj;
	}

	public Input getInputParams() {
		try {
			constanst = this.readConstants();
			BufferedReader br = new BufferedReader(
					new FileReader(constanst.getProperty(JSON_INPUT)));
			inputObj = gson.fromJson(br, Input.class);

		} catch(IOException e) {

		}
		return inputObj;
	}

	/**
	 * Devuelve todas las constantes almacenadas en un archivo externos con cambios OTF
	 * @return
	 */
	public Properties readConstants() {
		Properties props = new Properties(  );
		try {
			File file = new File( CONSTANTS_FILE );
			props.load( new FileInputStream( file ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
		return props;
	}
}
