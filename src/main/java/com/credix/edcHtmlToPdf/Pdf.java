

package com.credix.edcHtmlToPdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pdf {

	public String html, name, directory,  pageText;
	private StringBuilder modifiedHtml = new StringBuilder();
	public int i = 1, numberPages;
	public int getNumberPages() {
		return numberPages;
	}
	public void setNumberPages(int numberPages) {
		this.numberPages = numberPages;
	}
	public String getPageText() {
		return pageText;
	}
	public void setPageText(String pageText) {
		this.pageText = pageText;
	}
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHtml() {
		return html;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}
	public void setHtml(String html) {
		this.html = html;
	}

	public void createPdfFromHtml(String sourceFile, String destDirectory, String name) throws IOException {
		String filename = destDirectory + "/-" + name;
		try {
			setName(name);
			setDirectory(destDirectory);
			Functions functions = new Functions();
			String str = functions.readFile(sourceFile);
			PdfWriter pdfWriter = new PdfWriter(new File(filename));
			propertiesPdf(pdfWriter, str);
			setHtml(str);
			setName(name);
			call(destDirectory, name);
			if (fileExists(destDirectory + "/-" + name)){
				deleteFile(destDirectory + "/-" + name);
			}

			if (fileExists(destDirectory + "/" + name)){
				if (fileExists(destDirectory + "/Update-" + name  )) {
					deleteFile(destDirectory + "/Update-" + name);
				}
			}
			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " Impreso pdf " + name);

		} catch (Exception e) {
			Logger logger = new Logger();
			logger.write("Ocurrió un error en " + name + ": " + e.getMessage(), 1);
			System.out.println("Ocurrió un error: " + e);
		}

	}
	public void call(String destDirectory, String name) throws IOException {
		int i = 1;
		String pageText = "";
		int numberOfPages = getNumberOfPages(destDirectory + "/-" + name);
		while (i <= numberOfPages) {

			if (fileExists(destDirectory + "/Update-" + name)) {
				pageText = readerPage(destDirectory + "/Update-" + name);
				numberOfPages = getNumberOfPages(destDirectory + "/Update-" + name);
				deleteFile(destDirectory + "/Update-" + name);

			} else {
				pageText = readerPage(destDirectory + "/-" + name);

			}

			if (readerLastline(pageText).contains("de cuenta.")&& i== numberOfPages) {
				updatePdf();
				return;
			}
			setPageText(pageText);
			setNumberPages(numberOfPages);
			searchAndCapture(getHtml(), readerLastline(pageText));
			i=getI();
			i++;
			setI(i);
		}

		closePdfDocument(openPdfDocument(destDirectory + "/Update-" + name));

	}
	public void searchAndCapture(String html, String lastLine) throws IOException {
		boolean found=false;


		if (getHtml().contains("COMPOSICI&Oacute;N DEL TOTAL ADEUDADO")&& getHtml().contains("ABREVIATURAS"))
		{
			updatePdf();
			setI(numberPages);
			return;
		}
		lastLine = lastLine.trim().replaceAll("\\s+", " ");

		if (lastLine.trim().equals("C: Capital D: Descarga de cuotas SP: Saldo pendiente T: Tasa mensual nominal")){
			updatePdf();
			return;
		}
		if (lastLine.trim().equals("No se registran transacciones en el periodo")){
			updatePdf();
			return;
		}
		if ((lastLine.trim().contains("Monto del crédito") && lastLine.trim().contains("Plazo en meses"))
				|| (lastLine.trim().contains("Moneda") && lastLine.trim().contains("Tasa nominal anual"))
				|| (lastLine.trim().contains("Origen") && lastLine.trim().contains("Tasa interés total anualizada"))
				|| (lastLine.trim().contains("Fecha de inicio") && lastLine.trim().contains("Tasa interés moratoria mensual"))
				|| (lastLine.trim().contains("Monto de la cuota del plan") && lastLine.trim().contains("Fecha de finalización"))
				|| (lastLine.trim().contains("Monto de la cuota del crédito") && lastLine.trim().contains("Fecha de finalización"))
				||(lastLine.trim().contains("Monto del plan") && lastLine.trim().contains("Plazo en meses"))
				||(lastLine.trim().contains("Total cuotas colones") && lastLine.trim().contains("Nominal anual"))
				||(lastLine.trim().contains("Total cuotas dólares") && lastLine.trim().contains("Total anualizada"))

				||(lastLine.trim().contains("Fecha y hora límite de pago") && lastLine.trim().contains("Moratoria mensual"))

		){
			updatePdf();
			return;
		}
		if (lastLine.trim().contains("*Programa Cuotas Cero Interés es un beneficio que se ofrece al cliente siempre que se mantenga al día con los pagos. En el momento que incumpla los pagos de las cuotas se le")
				|| lastLine.trim().contains("cobrará intereses de financiamiento calculados con la tasa nominal de la tarjeta de crédito e interes moratorios de la tarjeta, si aplica. En caso que el cliente normalice el pago de sus")
				|| lastLine.trim().contains("cuotas haciendo un pago de contado, al mes siguiente volverá a gozar del beneficio cero interés")
				|| lastLine.trim().contains("*El pago mínimo exigible es de ¢5.000 y de $10 cuando se tenga deuda en esa moneda")
				|| lastLine.trim().contains("**Corresponde a la tasa de interés del plazo máximo utilizado")){
			if (caseTag("p", lastLine)){
				updatePdf();
				return;
			}

		}
		if(getI()==getNumberPages()-1){
			int num=findText("Aspectos destacados", getPageText().split("\\n"));
			if (num>0){
				String newContent="";
				if (num<=12){
					num+=2;
				}
				else if (num>12 && num<=32){
					num-=2;
				}
				else if (num>=32){
					num-=4;
				}

				for (int i = 0; i < num; i++) {
					newContent+= "<br><b/>";
				}
					String tagContent = getTagContentMatch(extratorMatcher("div"),"Aspectos destacados" );
					String module = extratorModule(tagContent, "section","");
					modifyHtml(getTagIndexStart(module,"section", ""), newContent);
					updatePdf();
					return;
				}

			}


		if (caseTag("div", lastLine)) {
			updatePdf();
			return;

		}

		if (caseTag("tr", lastLine)) {
			updatePdf();
			return;

		}



		updatePdf();



	}
	public Boolean isProduct(String line){
		return line.equals("Cuotas cero interés*")
				|| line.trim().equals("Ampliar plazo cero interés") ||
				line.trim().equals("Ampliar plazo cuoticas") ||
				line.trim().equals("Cuoticas") ||
				line.trim().equals("Crédito personal") ||
				line.trim().equals("Crédito moto") ||
				line.trim().equals("Plan liquidez - Refinanciamiento") ||
				line.trim().equals("Plan apoyo - Refinanciamiento") ||
				line.trim().equals("Plan solidario - Refinanciamiento");
	}
	public boolean sectionMatchOnpage(String [] pageText){
		for (int i = pageText.length - 1; i >= 0; i--) {
			if (isProduct(pageText[i])){
				return true;
			}
		}
		return false;
	}

	public String headerMatchOnpage(String [] pageText){
		for (int i = pageText.length - 1; i >= 0; i--) {
			if (pageText[i].contains("dólares principal")){
				return "dólares";
			}
			if (pageText[i].contains("colones principal")){
				return "principal";
			}
		}
		return null;
	}
	public int findText(String text, String[] pageText){
		int count=0;
		for (int i = pageText.length - 1; i >= 0; i--) {
			count+=1;
			if (pageText[i].equals(text)) {
				return count;
			}
		}
		return 0;

	}
	public String sectionMatchTag(String [] pageText){
		for (int i = pageText.length - 1; i >= 0; i--) {
			if (isProduct(pageText[i])){
				return pageText[i];
			}
		}
		return null;
	}

	public String getTagContentMatch(Matcher matcher, String lastLine) {
		String tagContent;
		while (matcher.find()) {
			tagContent = matcher.group(1);
			if (tagContent.contains(lastLine)){
				return tagContent;
			}
		}
		return null;
	}

	private boolean isEquals(String tagContent, String lastLine) {
		int count = 0;
		String lineAux="";
		lastLine = lastLine.replace("¢", " ");
		lastLine = lastLine.replace("$", " ");
		lastLine = lastLine.replaceAll("\\b\\d{1,2}/\\d{1,2}(?!/\\d{2,4})\\b", "");
		lastLine = lastLine.replaceAll("\\s+", " ").trim();

		String[] words = lastLine.split(" ");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
			words[i] = words[i].replaceAll("\\p{Z}", " ").trim();
		}
		lastLine="";
		for (String word: words){
			lastLine+=word.trim()+" ";
		}
		for (int j = 0; j < words.length; j++) {
			if (tagContent.toLowerCase().contains(words[j].toLowerCase()) && (j == 0 || count > 0)) {
				count++;
				lineAux += words[j] + " ";
			} else {
				if (count == words.length) {
					break;
				}
			}
		}
		return (count == words.length || count == words.length - 1) && lastLine.trim().equals(lineAux.trim());

	}
	private boolean searchContentColums(String tagContent, String lastLine) {
		String lineAux=lastLine;
		lastLine= lastLine.replaceAll(":", ": ");
		lastLine= lastLine.replaceAll(",", ", ");
		if (i== numberPages){
			return false;
		}
		int count = 0;
		int countLine=0;
		String newContent="";
		boolean band=false;
		String[] words = lastLine.split("\\s+");

		for (int j = 0; j < words.length; j++) {
			if (tagContent.toLowerCase().contains(words[j].toLowerCase()) && (j == 0 || count > 0)) {
				count++;
			} else {
				if (count > (words.length / 2) - 2) {
					break;
				} else {
					return false;
				}

			}
			if (count == words.length - 1) {
				break;
			}

		}


		String[] lineas = pageText.split("\\n");
		for (String linea:
				lineas) {
			if (linea.contains("Aspectos destacados")){
				band=true;

			}
			if (band){
				countLine++;
			}

			linea=linea.replaceAll(Character.toString('•'), "");
			if (linea.trim().contains(lineAux.trim())|| linea.trim().contains(lastLine.trim())){
				if (countLine>22){
					countLine=countLine/2;
					countLine+=13;

				}else {
					if (!(countLine==20)){
						countLine+=2;
					}

				}

				for (int k = 0; k < countLine; k++) {
					newContent+="<br>";
				}
				tagContent="<strong>Aspectos destacados</strong>";
				if (lastLine.contains("comisiones")){
					newContent+="<br><br>";
				}
				modifyHtml(getTagIndexStart(extratorModule(tagContent,"section",""), "section", ""),newContent);
				return true;
			}
		}

		return true;
	}
	private boolean searchContentTable(String tagContent, String lastLine, boolean desglose, int spacious, int duplicate) throws IOException {
		int count = 0;
		String lineAux="";
		lastLine = lastLine.replace("¢", " ");
		lastLine = lastLine.replace("$", " ");
		lastLine = lastLine.replaceAll("\\b\\d{1,2}/\\d{1,2}(?!/\\d{2,4})\\b", "");
		lastLine = lastLine.replaceAll("\\s+", " ").trim();

		String[] words = lastLine.split(" ");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
			words[i] = words[i].replaceAll("\\p{Z}", " ").trim();
		}
		lastLine="";
		for (String word: words){
			lastLine+=word.trim()+" ";
		}

		for (int j = 0; j < words.length; j++) {


			if (words[j].equals("") && count>0){
				count++;
			}
			if (tagContent.toLowerCase().contains(words[j].toLowerCase()) && (j == 0 || count > 0)) {
				count++;
				lineAux += words[j] + " ";
				if (count==(words.length)&& lastLine.toLowerCase().trim().equals(lineAux.toLowerCase().trim())){
					if (duplicate!=1){
						return false;
					}
					break;

				}
			} else {
				if (count==(words.length)&& lastLine.toLowerCase().trim().equals(lineAux.toLowerCase().trim())){

					break;

				}
				if (words.length>5) {
					if ((count >= (words.length / 2) && lastLine.contains(lineAux))) {
						break;
					}

				}


				if (words.length <= 4){
					if (count==(words.length)&& lastLine.toLowerCase().trim().equals(lineAux.toLowerCase().trim())){
						if (tagContent.contains(lastLine)){
							break;
						}


					}
					List<String> words_sin_numeros = new ArrayList<>();

					for (String word : words) {
						if (!word.matches("\\d+")) { // Verificar si la palabra no es un número
							words_sin_numeros.add(word);
						}
					}
					String[] partes = lastLine.split("\\b\\d+\\b");

					// Construir la cadena sin números uniendo las partes
					String lastLine_sin_numeros = String.join("", partes);
					if(count==words_sin_numeros.size()&& lastLine_sin_numeros.equals(lineAux)){
						if (tagContent.contains(lastLine_sin_numeros)){
							break;
						}

					}


				}
				return false;


			}
		}
		String newContent="";
		int countLine=0;
		boolean band=false;

		if (desglose){

			for (int j = 0; j < spacious; j++) {
				newContent+="<br>";
			}


			if ((count== words.length ||count-1== words.length ) && lastLine.toLowerCase().trim().contains(lineAux.toLowerCase().trim())){
				modifyHtml(getTagIndexEnd(tagContent, "tr", lastLine),"</tbody></table>"+newContent+"<table class='edc'><tbody class='edc'>");
				return true;
			}
			return false;
		}
		if (words.length >=5){
			if (count >= (words.length / 2)  && lastLine.toLowerCase().trim().contains(lineAux.toLowerCase().trim())){

				if(tagContent.contains("colum-container clearfix")){
					String[] lines = pageText.split("\\n");
					for (String linea:
							lines) {
						if (linea.equals("Aspectos destacados")){
							band=true;
						}
						if (band){
							countLine++;
						}
						if (linea.equals(lastLine)){
							for (int k = 0; k <= countLine; k++) {
								newContent+="<br>";
							}
						}
					}
					modifyHtml(getTagIndexStart(extratorModule(tagContent,"section",""), "section", lastLine),newContent);
				}


				if (isLastTr(tagContent,"tr","table")){
					if (caseDetallePago(tagContent)) {
						return true;
					}
					updatePdf();
					return true;
				}
			}
			if (caseDetallePago(tagContent)) {
				return true;
			}

			if ((count== words.length) && lastLine.toLowerCase().trim().contains(lineAux.toLowerCase().trim())){
				if( lastLine.trim().contains("Tasa de interés Nominal anual Total anualizada Anual máxima (TAM) Moratoria mensual")){
					int index = getTagIndexStart(tagContent, "tr",lastLine);
					modifyHtml(index,"</tbody></table><br><br><br><br><table class='edc'><tbody class='edc'>");
					return true;
				}
				return searchHeader(tagContent, lastLine);
			}

			if (words.length>=10 && count>=8){
				return searchHeader(tagContent, lastLine);
			}
		}

		if (words.length<=4){
			if (words.length==2){
				if (!(count== words.length && lastLine.trim().equals(lineAux.trim()) && tagContent.contains(lastLine.trim()))){
					return false;
				}
			}
			if (words.length<=4){
				if (lastLine.contains("Interés corriente")){
					if (!(tagContent.contains("Interés corriente"))){
						return false;
					}
				}
			}
			if (count== words.length &&lastLine.trim().equals(lineAux.trim())){
				if (lastLine.trim().equals("Interés moratorio")) {
					String[] lines;
					if (tagContent.contains("Interés moratorio*")) {
						return false;
					}
				}

				if (isLastTr(tagContent,"tr","table")){
					updatePdf();
					return true;
				}
				return searchHeader(tagContent, lastLine);
			}
			if (count== words.length && tagContent.contains(lastLine.trim())){
				if (lastLine.trim().equals("Interés moratorio")){
					String[] lines;
					if(tagContent.contains("Interés moratorio*")){
						return false;
					}
					setI(getI() + 1);
					String fileName = fileExists(getDirectory() + "/" + getName()) ?
							getDirectory() + "/" + getName() :
							getDirectory() + "/Update-" + getName();
					lines = readerPage(fileName).split("\\n");
					if (lines[0].trim().equals("Reversión de intereses")){
						if (tagContent.contains("text-align: left; width: 50%;")){
							setI(getI() - 1);
							return searchHeader(tagContent, lastLine);
						}
						else {
							setI(getI() - 1);
							return false;

						}
					}
					setI(getI() - 1);
					if(isEquals(tagContent, lines[0])) {
						setI(getI() - 1);
					}
				}
				if (isLastTr(tagContent,"tr","table")){
					updatePdf();
					return true;
				}
				return searchHeader(tagContent, lastLine);
			}
			List<String> words_sin_numeros = new ArrayList<>();

			// Eliminar números del arreglo y almacenar palabras en la lista
			for (String word : words) {
				// Verificar si la palabra no es un número
				String wordd=word.replaceAll("[\\d.,]+", "");
				if (!wordd.equals("")) {
					words_sin_numeros.add(word);
				}
			}

// Eliminar números, puntos y comas de la última línea
			String lastLine_sin_numeros = lastLine.replaceAll("[\\d.,]+", "");

			// Construir la cadena sin números uniendo las partes


			if (tagContent.contains(lastLine_sin_numeros.trim()) && lastLine.contains(lastLine_sin_numeros.trim())){
				if (isLastTr(tagContent,"tr","table")){
					updatePdf();
					return true;
				}
				return searchHeader(tagContent, lastLine);
			}


			else {
				if( lastLine.trim().equals(lineAux.trim())&& tagContent.contains(lineAux)){
					if (isLastTr(tagContent,"tr","table")){
						updatePdf();
						return true;
					}
					if (caseDetallePago(tagContent)) {
						return true;
					}
					return searchHeader(tagContent, lastLine);

				}
			}
		}

		return false;
	}

	private int duplicate(String lastLine) {
		lastLine = lastLine.replace("¢", " ");
		lastLine = lastLine.replace("$", " ");
		lastLine = lastLine.replaceAll("\\b\\d{1,2}/\\d{1,2}(?!/\\d{2,4})\\b", "");
		lastLine = lastLine.replaceAll("\\s+", " ").trim();

		String[] words = lastLine.split(" ");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
			words[i] = words[i].replaceAll("\\p{Z}", " ").trim();
		}
		lastLine="";
		for (String word: words){
			lastLine+=word.trim()+" ";
		}
		String[] lines = pageText.split("\\n");
		int count=0;
		for ( String line:
				lines) {
			line = line.replace("¢", " ");
			line = line.replace("$", " ");
			line = line.replaceAll("\\b\\d{1,2}/\\d{1,2}(?!/\\d{2,4})\\b", "");
			line = line.replaceAll("\\s+", " ").trim();

			words = line.split(" ");
			for (int i = 0; i < words.length; i++) {
				words[i] = words[i].replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
				words[i] = words[i].replaceAll("\\p{Z}", " ").trim();
			}
			line="";
			for (String word: words){
				line+=word.trim()+" ";
			}
			if (line.trim().equals(lastLine.trim())){
				count++;
			}
		}
		return count;
	}

	private boolean searchHeader(String tagContent, String lastLine) {
		int count=0;
		String newContent="";
		String newContentAux="";
		String br="";
		int index=0;
		Matcher matcher= getReversedMatcher(extratorModule(tagContent,"table",""), tagContent, "tr");
		if (matcher==null){
			return false;
		}
		if (tagContent.contains("<th")){
			if (newContent!=""){
				modifyHtml(index,br);
			}else{
				modifyHtml(getTagIndexStart(tagContent,"tr", lastLine), "<br>");
			}

		}
		String contenido= matcher.group();
		if (contenido.contains("<th")){
			newContent+= contenido;
			br+="<br>";
			index= getTagIndexStart(tagContent,"tr", lastLine);


		}
		while (matcher.find()){

			String content= matcher.group(1);

			if (content.contains("<th")){
				if (tagContent.contains("Interés corriente")
						|| tagContent.contains("Interés moratorio")
						|| tagContent.contains("Reversión de intereses")
						|| tagContent.contains("Interés de otras líneas de financiamiento")
				){
					modifyHtml(getTagIndexEnd(tagContent,"tr", lastLine),content);
					return true;

				}

				count++;
				br+="<br>";
				if (count>1){
					if (content.equals(newContentAux)){
						break;
					}
					String cont=content+newContentAux;
					newContentAux=cont;
					break;
				}
				newContentAux= content;


			}
		}
		if (newContent==""){
			newContent=newContentAux;
		}
		if (newContent!=""&& newContentAux!=""){
			if (!(newContent.equals(newContentAux))){
				if (newContent.contains("Desglose")){
					newContent=newContent+newContentAux;
				}
				else {
					newContent = newContentAux + newContent;
				}
			}
		}
		if (newContent.contains("Desglose")){
			newContent="</tbody></table><br><br><table class='edc'><tbody class='edc'>"+newContent;
		}
		modifyHtml(getTagIndexEnd(tagContent,"tr", lastLine),newContent);

		return true;

	}
	public int getTagContentIndexWithSection(String lastLine, String module, String tag){
		int count=0;
		boolean band=false;
		lastLine = lastLine.replace("¢", " ");
		String lineAux="";
		String[] words = lastLine.split("[\\s\\p{Z}]+");
		for (String word : words) {
			lastLine += word.trim() + " ";
		}
		Pattern tagPattern = Pattern.compile("(<"+tag+"[^>]*>[\\s\\S]*?</"+tag+">)");
		Matcher matcher= tagPattern.matcher(module);
		while (matcher.find()) {
			String tagContent = matcher.group(1);
			lastLine = "";
			if (band){
				return matcher.start();
			}


			for (int j = 0; j < words.length; j++) {

				if (words[j].equals("") && count > 0) {
					count++;
				}
				if (tagContent.toLowerCase().contains(words[j].toLowerCase()) && (j == 0 || count > 0)) {
					count++;
					lineAux += words[j] + " ";
					if (count == (words.length) ) {
						band=true;


					}
				}
			}

		}
		return 0;


	}
	public boolean caseTag(String tag, String lastLine) throws IOException {
		int index=0;

		String tagContent;
		int spacious=0;
		Matcher matcher = extratorMatcher(tag);
		switch (tag) {

			case "p":

				while (matcher.find()) {
					tagContent = matcher.group(1);
					if (extratorModuleText(tagContent, "section", " <strong>Detalle de pago")) {
						if (lastLine.trim().contains("*El pago mínimo exigible es de ¢5.000 y de $10 cuando se tenga deuda en esa moneda")){
							if (caseDetallePago(tagContent)) {
								return true;
							}
						}

					}

					if (caseLeyenda(lastLine, tagContent)) {
						return true;
					}


				}

				return false;

			case "div":

				if (lastLine.trim().equals("de cuenta.")) {
					return true;
				}
				if (lastLine.trim().contains("Fecha y hora límite de pago")&& lastLine.trim().contains("Moratoria mensual")){
					return true;
				}
				while (matcher.find()) {
					tagContent = matcher.group(1);
					if (lastLine.trim().equals("Colones")) {
						String[] liness = pageText.split("\\n");
						String line = liness[liness.length - 2];
						if (tagContent.contains("<strong>" + line)) {
							index = getTagIndexStart(extratorModule(tagContent, "section","") ,"section", "");
							modifyHtml(index, "<br><br><br><br>");
							return true;

						}
					}

					if (tagContent.contains(lastLine.trim())) {
						if (tagContent.contains("</strong>\n" +
								"                                        </div>") || tagContent.contains("</strong></div>") ||
								tagContent.contains("<strong>" + lastLine.trim() + "</strong>") ||
								tagContent.contains("<strong>" + lastLine.trim())) {
							if (!caseStrong(lastLine, tagContent, "section")) {

							}
							return true;
						}
					}

				}
				return false;

			//case "p":
			case "tr":

				String[] lines;
				int countTag=0;
				if (getI() > 1) {
					setI(getI() + 1);
					String fileName = fileExists(getDirectory() + "/" + getName()) ?
							getDirectory() + "/" + getName() :
							getDirectory() + "/Update-" + getName();
					lines = readerPage(fileName).split("\\n");
					setI(getI() - 1);

					if (lines[0].trim().contains("Saldo anterior al") || lines[0].trim().contains("Total pagos recibidos") ||
							lines[0].trim().contains("Total transacciones del") ||
							lines[0].trim().contains("Total de intereses") ||
							lines[0].trim().contains("Total de otros cargos") ||
							lines[0].trim().contains("Total de servicios de elección voluntaria") ||
							lines[0].trim().contains("Total de cargos por gestión de cobro")

					) {
						return true;
					}
				}


				String newContent="";
				String identificador="";
				boolean band=true;
				int countWhile=0;
				Boolean desglose=false;
				int duplicate=duplicate(lastLine);;

				if (lastLine.trim().contains("Saldo anterior al") || lastLine.trim().contains("Total pagos recibidos") ||
						lastLine.trim().contains("Total transacciones del") ||
						lastLine.trim().contains("Total de intereses") ||
						lastLine.trim().contains("Total de otros cargos") ||
						lastLine.trim().contains("Total de servicios de elección voluntaria") ||
						lastLine.trim().contains("Total de cargos por gestión de cobro")

				) {
					return true;
				}
				if (lastLine.trim().contains("Saldos al corte por rubro")){
					return true;
				}
				String[] liness ;
				String line	;
				if (lastLine.trim().equals("Desglose de la cuota")){
					setI(getI() + 1);;
					String fileName = fileExists(getDirectory() + "/" + getName()) ?
							getDirectory() + "/" + getName() :
							getDirectory() + "/Update-" + getName();
					liness = readerPage(fileName).split("\\n");
					setI(getI() - 1);
					lastLine=liness[0];
					lastLine = lastLine.replace("¢", " ");
					lastLine = lastLine.replace("$", " ");
					lastLine = lastLine.replaceAll("\\b\\d{1,2}/\\d{1,2}(?!/\\d{2,4})\\b", "");
					lastLine = lastLine.replaceAll("\\s+", " ").trim();

					String[] words = lastLine.split(" ");
					for (int i = 0; i < words.length; i++) {
						words[i] = words[i].replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
						words[i] = words[i].replaceAll("\\p{Z}", " ").trim();
					}
					lastLine="";
					for (String word: words){
						lastLine+=word.trim()+" ";
					}
					desglose=true;


				}

				liness = pageText.split("\\n");
				line	= liness[liness.length - 2];
				while (matcher.find()) {

					tagContent = matcher.group(1);
					if (duplicate > 1) {
						if (isEquals(tagContent, lastLine)) {
							duplicate--;
						}
//					getTagContentTextWithSection

					}
					;
					if (getI() != 1) {


						if (lastLine.contains("Interés moratorio") && tagContent.contains("Interés moratorio") && !(tagContent.contains("Interés moratorio*"))
						) {

							countWhile++;
							if (countWhile <= 2) {
								continue;
							}


						}
					}


					if (lastLine.trim().equals("Fecha Concepto Cuota en Interés Abono al")
							|| (lastLine.trim().equals("Fecha y hora Concepto Cuota en Interés Abono al"))) {
						lines = pageText.split("\\n");
						int ind;
						if (desglose) {
							ind = 6;
						} else {
							if (lastLine.trim().equals("Fecha y hora Concepto Cuota en Interés Abono al")) {
								ind = 9;
							} else {
								ind = 7;
							}

						}
						identificador = lines[lines.length - ind];
						if (identificador.equals("Cuotas cero interés*") || identificador.trim().equals("Ampliar plazo cero interés") ||
								lines[lines.length - (ind + 1)].trim().equals("Ampliar plazo cero interés") ||
								lines[lines.length - (ind + 1)].trim().equals("Otras líneas de financiamiento") ||
								identificador.trim().equals("Cuoticas") ||
								identificador.trim().equals("Ampliar plazo cuoticas") ||
								identificador.trim().equals("Crédito personal") ||
								identificador.trim().equals("Crédito moto") ||
								identificador.trim().equals("Plan liquidez - Refinanciamiento") ||
								identificador.trim().equals("Plan apoyo - Refinanciamiento") ||
								identificador.trim().equals("Plan solidario - Refinanciamiento")

						) {
//
							if (lines[lines.length - (ind + 1)].trim().equals("Ampliar plazo cero interés")) {
								identificador = lines[lines.length - (ind + 1)];
							}
							matcher = extratorMatcher("div");
							while (matcher.find()) {

								tagContent = matcher.group(1);
								if (tagContent.contains("<strong>" + identificador.trim())) {
									String section = extratorModule(tagContent, "section","");
									int indexSection = getTagIndexStart(section, "section", "");
									newContent = "<br><br><br>";
									int indx = 4;
									if (desglose) {
										newContent = "<br><br>";
										if (!(identificador.trim().equals("Ampliar plazo cero interés"))) {
											indx = 2;
										}


									}


									int indexContent = getTagContentIndexWithSection(lines[lines.length - indx], section, "tr");

									modifyHtml(indexSection + indexContent, newContent);
									return true;


								}
								if (tagContent.contains("<strong>" + lines[lines.length - (ind)])) {
									String section = extratorModule(tagContent, "section","");
									int indexSection = getTagIndexStart(section, "section", "");
									newContent = "<br><br><br>";
									int indx = 4;
									if (desglose) {
										newContent = "<br><br>";
										indx = 2;

									}
									int indexContent = getTagContentIndexWithSection(lines[lines.length - indx], section, "tr");

									modifyHtml(indexSection + indexContent, newContent);
									return true;

								}
							}

						} else {
							int countLine = 0;
							band = false;

							String[] lineas = pageText.split("\\n");
							for (int i = lineas.length - 1; i >= 0; i--) {
								countLine++;
								if (band) {
									matcher = extratorMatcher("tr");
									while (matcher.find()) {

										tagContent = matcher.group(1);

										if (searchContentTable(tagContent, lineas[i], true, 3, 1)) {
											band = false;
											return true;
										}

									}
									band = false;
								}
								if (lineas[i].contains("Desglose de la cuota")) {
									band = true;
								}
								if (lineas[i].equals("Cuotas cero interés*")
										|| lineas[i].trim().equals("Ampliar plazo cero interés") ||
										lineas[i].trim().equals("Ampliar plazo cuoticas") ||
										lineas[i].trim().equals("Cuoticas") ||
										lineas[i].trim().equals("Crédito personal") ||
										lineas[i].trim().equals("Crédito moto") ||
										lineas[i].trim().equals("Plan liquidez - Refinanciamiento") ||
										lineas[i].trim().equals("Plan apoyo - Refinanciamiento") ||
										lineas[i].trim().equals("Plan solidario - Refinanciamiento")

								) {
									break;
								}
							}
							String line1 = lines[lines.length - countLine];
							if (line1.equals("Cuotas cero interés*") || line1.trim().equals("Ampliar plazo cero interés") ||
									line1.trim().equals("Cuoticas") ||
									line1.trim().equals("Ampliar plazo cuoticas") ||
									line1.trim().equals("Crédito personal") ||
									line1.trim().equals("Crédito moto") ||
									line1.trim().equals("Plan liquidez - Refinanciamiento") ||
									line1.trim().equals("Plan apoyo - Refinanciamiento") ||
									line1.trim().equals("Plan solidario - Refinanciamiento")

							) {

								matcher = extratorMatcher("div");
								while (matcher.find()) {

									tagContent = matcher.group(1);
									tagContent = tagContent.replaceAll("&nbsp;", " ");
									if (tagContent.contains("<strong>" + line1.trim())) {
										String section = extratorModule(tagContent, "section","");
										int indexSection = getTagIndexStart(section, "section", lastLine);
										int indexContent = getTagContentIndexWithSection(lines[lines.length - 4], section, "tr");
										newContent = "<br><br><br>";
										if (desglose) {
											newContent += "<br>";
										}
										modifyHtml(indexSection + indexContent, newContent);
										return true;


									}
								}

							}

						}


					}


					if (lastLine.contains("[****") && !(lastLine.contains("principal")) || lastLine.trim().contains("límite de pago colones principal")) {
						if (!(lastLine.trim().contains("límite de pago colones principal"))) {
							String moneda = headerMatchOnpage(pageText.split("\\n"));
							if ((moneda) != null) {
								String title = sectionMatchTag(pageText.split("\\n"));
								if (title != null) {
									title = getTagContentMatch(extratorMatcher("div"), "<strong>" + title);
									if (title != null) {
										String sectionTitle = extratorModule(title, "section","");
										String tagName = extratorMatcherWithFilter(sectionTitle, lastLine, "tr");
										int start = getTagIndexStart(tagName, "tr", lastLine);
										int end = getTagIndexEnd(tagName, "tr", lastLine);

										newContent = "<br><br>" +
												"<tr>" +
												"<th style=\"text-align: left; max-width: 60px; min-width: 60px; white-space: nowrap\">" +
												"Fecha <br>" +
												lastLine +
												"</th>" +
												"<th style=\"text-align: left; max-width: 400px; min-width: 400px\">" +
												"Concepto" +
												"</th>" +
												"<th style=\"text-align: right; min-width: 80px; max-width: 80px\">" +
												"Cuota en<br> " + moneda +
												"</th>" +
												"<th style=\"text-align: right; min-width: 30px; max-width: 30px\">" +
												"Interés" +
												"</th>" +
												"<th style=\"text-align: right; min-width: 50px\">" +
												"Abono al<br> principal" +
												"</th>" +
												"</tr>" +
												"<tr>"
										;

										replaceTag(start, end, newContent);
										return true;

									}
								}

							}
						}
					}


					if (lastLine.contains("colones principal")  || (lastLine.contains("[****") && lastLine.contains("dólares principal"))) {
						lines = pageText.split("\\n");
						if (lastLine.trim().contains("límite de pago colones principal")) {
							identificador = lines[lines.length - 10];
						} else {
							identificador = lines[lines.length - 8];
						}
						if (identificador.equals("Cuotas cero interés*") || lines[lines.length - 9].trim().equals("Ampliar plazo cero interés") ||
								identificador.trim().equals("Cuoticas") ||
								identificador.trim().equals("Ampliar plazo cuoticas") ||
								identificador.trim().equals("Crédito personal") ||
								identificador.trim().equals("Crédito moto") ||
								identificador.trim().equals("Plan liquidez - Refinanciamiento") ||
								identificador.trim().equals("Plan apoyo - Refinanciamiento") ||
								identificador.trim().equals("Plan solidario - Refinanciamiento")

						) {
							if (lines[lines.length - 9].trim().equals("Ampliar plazo cero interés")) {
								identificador = lines[lines.length - 9];
							}
							matcher = extratorMatcher("div");
							while (matcher.find()) {
								tagContent = matcher.group(1);
								tagContent = tagContent.replaceAll("&nbsp;", " ");
								if (tagContent.contains("<strong>" + identificador.trim())) {
									String section = extratorModule(tagContent, "section","");
									int indexSection = getTagIndexStart(section, "section", "");
									int indexContent = getTagContentIndexWithSection(lines[lines.length - 3], section, "tr");
									modifyHtml(indexSection + indexContent, "<br><br><br><br><br>");
									return true;
								}
							}

						} else {
							int countLine = 0;

							String[] lineas = pageText.split("\\n");
							for (int i = lineas.length - 1; i >= 0; i--) {
								countLine++;
								if (lineas[i].equals("Cuotas cero interés*")
										|| lineas[i].trim().equals("Ampliar plazo cero interés") ||
										lineas[i].trim().equals("Cuoticas") ||
										lineas[i].trim().equals("Ampliar plazo cuoticas") ||
										lineas[i].trim().equals("Crédito personal") ||
										lineas[i].trim().equals("Crédito moto") ||
										lineas[i].trim().equals("Plan liquidez - Refinanciamiento") ||
										lineas[i].trim().equals("Plan apoyo - Refinanciamiento") ||
										lineas[i].trim().equals("Plan solidario - Refinanciamiento")

								) {
									break;
								}
							}
							String line1 = lines[lines.length - countLine];
							if (line1.equals("Cuotas cero interés*") || line1.trim().equals("Ampliar plazo cero interés") ||
									line1.trim().equals("Cuoticas") ||
									line1.trim().equals("Ampliar plazo cuoticas") ||
									line1.trim().equals("Crédito personal") ||
									line1.trim().equals("Crédito moto") ||
									line1.trim().equals("Plan liquidez - Refinanciamiento") ||
									line1.trim().equals("Plan apoyo - Refinanciamiento") ||
									line1.trim().equals("Ampliar plazo cero interés") ||
									line1.trim().equals("Plan solidario - Refinanciamiento")

							) {
								matcher = extratorMatcher("div");
								while (matcher.find()) {

									tagContent = matcher.group(1);
									tagContent = tagContent.replaceAll("&nbsp;", " ");
									if (tagContent.contains("<strong>" + line1.trim())) {
										String section = extratorModule(tagContent, "section","");
										int indexSection = getTagIndexStart(section, "section", "");
										int indexContent = getTagContentIndexWithSection(lines[lines.length - 4], section, "tr");
										newContent = "<br><br><br><br><br>";
										if (desglose) {
											newContent += "<br>";
										}
										modifyHtml(indexSection + indexContent, newContent);
										return true;


									}
								}

							}

						}


					}


					if( line.trim().contains("Tasa de interés Nominal anual Total anualizada Anual máxima (TAM) Moratoria mensual")){
						lastLine="Tasa de interés Nominal anual Total anualizada Anual máxima (TAM) Moratoria mensual";
					}
					if (lastLine.contains("Interés de periodos anteriores")){
						return true;
					}
					if (lastLine.trim().contains("Transacciones y Intereses, cargos y otros")){

						lines = getPageText().split("\\n");
						if(lines[lines.length - 3].trim().equals( "Resumen de movimientos")){
							lastLine = lines[lines.length - 3];
							newContent="<br><br><br><br><br><br><br>";
							matcher = extratorMatcher("div");
							while (matcher.find()) {
								tagContent = matcher.group(1);
								if (tagContent.contains("<strong>")) {
									if (tagContent.contains("<strong>" + lastLine.trim())) {
										index = getTagIndexStart(tagContent, "div", "");
										modifyHtml(index,newContent);
										return true;
									}
								}
							}

						}
					}
					if (lastLine.trim().contains("servicios voluntarios")){

						lines = getPageText().split("\\n");
						if(lines[lines.length - 4].trim().contains("Resumen de movimientos")){
							lastLine = lines[lines.length - 4];
							newContent="<br><br><br><br><br><br><br><br><br>";
							matcher = extratorMatcher("div");
							while (matcher.find()) {
								tagContent = matcher.group(1);
								if (tagContent.contains("<strong>")) {
									if (tagContent.contains("<strong>" + lastLine.trim())) {
										index = getTagIndexStart(tagContent, "div", "");
										modifyHtml(index,newContent);
										return true;
									}
								}
							}

						}
					}

					if (lastLine.trim().equals("Tasa de interés Dólares Colones")
							|| lastLine.trim().equals("Concepto Colones Dólares")
							||lastLine.trim().equals("Tasa de interés Colones" )|| lastLine.trim().equals("Colones Dólares")|| lastLine.trim().equals("Colones")) {


						lines = getPageText().split("\\n");

						if(lines[lines.length - 3].trim().equals( "Otras líneas de financiamiento")){
							lastLine = lines[lines.length - 3];
							newContent="<br><br><br><br><br><br>";


						}else {
							lastLine = lines[lines.length - 2];
							newContent="<br><br><br><br>";
						}
						matcher = extratorMatcher("div");
						while (matcher.find()) {
							tagContent = matcher.group(1);
							if (tagContent.contains("<strong>")){
								if (tagContent.contains("<strong>"+lastLine.trim())){

									if (lastLine.equals("Movimientos de la tarjeta de crédito") ||
											lastLine.trim().equals("Detalle de pagos del periodo") ||
											lastLine.trim().equals("Detalle de transacciones")||
											lastLine.trim().equals("Detalle de intereses")||
											lastLine.trim().equals("Detalle de otros cargos")||
											lastLine.trim().equals("Detalle de servicios de elección voluntaria")||
											lastLine.trim().equals("Cargos por gestión de cobro")||
											lastLine.trim().equals("Resumen de movimientos")

									)


									{

										index = getTagIndexStart(tagContent, "div", "");
										modifyHtml(index,newContent);
										return true;
									}
									if (lastLine.equals("Otras líneas de financiamiento") ||
											lastLine.trim().equals("Resumen de tarjeta de crédito") ||
											lastLine.trim().equals("Detalle de pago"))

									{

										index = getTagIndexStart(extratorModule(tagContent, "section",""), "section", "");
										modifyHtml(index,"<br><br><br><br><br><br><br>");
										return true;
									}
									if (lastLine.trim().equals("Cuotas cero interés*") || lastLine.trim().equals("Ampliar plazo cero interés")||
											lastLine.trim().equals("Cuoticas")||
											lastLine.trim().equals("Ampliar plazo cuoticas") ||
											lastLine.trim().equals("Crédito personal")||
											lastLine.trim().equals("Crédito moto")||
											lastLine.trim().equals("Plan liquidez - Refinanciamiento")||
											lastLine.trim().equals("Plan apoyo - Refinanciamiento")||
											lastLine.trim().equals("Plan solidario - Refinanciamiento")

									){


										index = getTagIndexStart(extratorModule(tagContent, "section",""), "section", "");
										modifyHtml(index,newContent);
										return true;

									}


								}
							}
						}




					}
					if (lastLine.trim().contains("Total cuotas colones") && lastLine.contains("Nominal anual")){
						lines = getPageText().split("\\n");
						if(lines[lines.length - 4].trim().equals( "Otras líneas de financiamiento")){
							lastLine = lines[lines.length - 4];
							newContent="<br><br><br><br><br>";

						}else {
							lastLine = lines[lines.length - 3];
							newContent="<br><br><br><br>";
						}
						while (matcher.find()) {
							tagContent = matcher.group(1);
							if (tagContent.contains("<strong>")){
								if (tagContent.contains(lastLine.trim())){
									index = getTagIndexStart(tagContent, "section", lastLine);
									modifyHtml(index,newContent);
								}
							}
						}
					}

					if (lastLine.trim().equals("Fecha Concepto Lugar / Moneda Colones Dólares")){
						lines = getPageText().split("\\n");
						lastLine = lines[lines.length - 2];
						matcher=extratorMatcher("div");
						while (matcher.find()) {
							tagContent = matcher.group(1);
							if (tagContent.contains("<strong>")){
								if (tagContent.contains(lastLine.trim())){
									index = getTagIndexStart(tagContent, "div", lastLine);
									modifyHtml(index,"<br><br><br><br>");
									return true;
								}
							}
						}
					}



					if (lastLine.equals("de cuenta.") && i==numberPages){
						return true;
					}

					if(duplicate>1){
						if ((lastLine.contains("Interés moratorio"))){
							duplicate=1;
						}
					}
					if(searchContentTable(tagContent, lastLine, desglose, spacious, duplicate)){
						return true;
					}


				}
				return false;

			case "columns":
				if (lastLine.trim().contains("de interés Nominal anual Total anualizada Anual máxima (TAM) Moratoria mensual")){
					return true;
				}
				if(getI()!=numberPages-1){
					return false;
				}
				if (lastLine.trim().equals("Abreviaciones")) {
					String module=(extratorModule("<strong>Abreviaciones</strong>", "section",""));
					index = getTagIndexStart(module, "section", lastLine);
					modifyHtml(index, "<br><br><br>");
					return true;
				}


				String[] words = lastLine.split("\\s+");
				line="";
				for (int i = 0; i < words.length; i++) {
					// Reemplazar el carácter especial en el elemento actual del words
					words[i] = words[i].replaceAll(Character.toString('•'), "");
				}
				System.arraycopy(words, 1, words, 0, words.length - 1);
				words = Arrays.copyOf(words, words.length - 1);
				for (String word:
						words) {
					line+=word + " ";

				}
				if (line !=""){
					lastLine=line;
				}
				Matcher columns = extratorMatcher("div");
				while (columns.find()){
					tagContent=columns.group(1);
					tagContent = tagContent.replaceAll("</b>", " ").trim();
					tagContent = tagContent.replaceAll("/n", " ").trim();
//					leer y separar por \n pafra conseguir frase
					if (searchContentColums(tagContent, lastLine.trim())){
						return true;
					}
				}
				return false;

		}

		return false;
	}
	private boolean caseStrong(String lastLine, String tagContent, String module) {
		int index = 0;
		if (tagContent.contains(lastLine.trim())) {

			if (lastLine.equals("Movimientos de la tarjeta de crédito") ||
					lastLine.trim().equals("Detalle de pagos del periodo") ||
					lastLine.trim().equals("Detalle de transacciones")||
					lastLine.trim().equals("Detalle de intereses")||
					lastLine.trim().equals("Detalle de otros cargos")||
					lastLine.trim().equals("Detalle de servicios de elección voluntaria")||
					lastLine.trim().equals("Cargos por gestión de cobro")||
					lastLine.trim().equals("Resumen de movimientos"))
			{


				index = getTagIndexStart(tagContent, "div", lastLine);
				modifyHtml(index,"<br><br><br>");
				return true;
			}
			if (lastLine.equals("Otras líneas de financiamiento") ||
					lastLine.trim().equals("Resumen de tarjeta de crédito") ||
					lastLine.trim().equals("Detalle de pago"))

			{

				index = getTagIndexStart(extratorModule(tagContent, module,""), module, lastLine);
				modifyHtml(index,"<br><br><br>");
				return true;
			}
			if (lastLine.trim().equals("Aspectos destacados") ||
					lastLine.trim().equals("Abreviaciones")){
				index = getTagIndexStart(extratorModule(tagContent, module,""), module, lastLine);
				modifyHtml(index,"<br><br><br>");
				return true;

			}
			if (lastLine.trim().equals("Cuotas cero interés*") || lastLine.trim().equals("Ampliar plazo cero interés")||
					lastLine.trim().equals("Cuoticas")||
					lastLine.trim().equals("Ampliar plazo cuoticas") ||
					lastLine.trim().equals("Crédito personal")||
					lastLine.trim().equals("Crédito moto")||
					lastLine.trim().equals("Plan liquidez - Refinanciamiento")||
					lastLine.trim().equals("Plan apoyo - Refinanciamiento")||
					lastLine.trim().equals("Plan solidario - Refinanciamiento")

			){
				String[] liness = pageText.split("\\n");
				String line	= liness[liness.length - 2];
				String newContent="<br><br>";

				if (line.trim().equals("Otras líneas de financiamiento")){
					tagContent="<strong>"+line.trim();
					newContent+="<br><br><br>";
				}

				index = getTagIndexStart(extratorModule(tagContent, module,lastLine), module, lastLine);
				modifyHtml(index,newContent);
				return true;

			}
		}
		return false;
	}
	private boolean caseLeyenda(String lastLine, String tagContent) throws IOException {
		if (getNumberPages()==getI()){
			return true;
		}
		int count = 0;
		String lineAux="";
		String newContent="";
		int countAux=0;
		String[] words = lastLine.split("\\s+");

		if (isEquals(tagContent, lastLine)){
			String[] lines;
			setI(getI() + 1);
			String fileName = fileExists(getDirectory() + "/" + getName()) ?
					getDirectory() + "/" + getName() :
					getDirectory() + "/Update-" + getName();
			lines = readerPage(fileName).split("\\n");
			if(isEquals(tagContent, lines[0])){
				setI(getI() - 1);
				lines = getPageText().split("\\n");
				if (isEquals(tagContent, lines[lines.length-2])){
					lines = getPageText().split("\\n");
					newContent = "<br><br>";
				} else {
					newContent = "<br>";
				}
				modifyHtml(getTagIndexStart(tagContent, "p", lastLine), newContent);

			}else{setI(getI() - 1);}

			return true;
		}
		return false;
	}
	private boolean caseDetallePago(String tagContent) {

		String module = extratorModule(tagContent, "section","");
		String table = extratorModule(tagContent, "table","");
		if (table==null){
			table=module;
		}
		if (module==null){
			return false;
		}
		if (module.contains("<strong>Detalle de pago</strong>")) {
			int rowIndex = getRowNum("tr", table, tagContent);
			if(tagContent.contains("*")){
				if(!(tagContent.contains("</p>"))){
					rowIndex+=5;
				}

				else{
					rowIndex+=2;
				}


			}
			if(!(tagContent.contains("</p>"))){
				rowIndex+=9;
			}

			int index = getTagIndexStart(module, "section", "");
			String newContent = "";
			for (int k = 0; k < rowIndex+2; k++) {
				newContent += "<br>";

			}
			modifyHtml(index, newContent);
			return true;



		}
		return false;
	}
	private boolean isLastTr(String tagContent, String tag, String module) {
		String section= extratorModule(tagContent,"section","");
		String lastTr="";
		if (section==null){
			return false;
		}
		Pattern trPattern = Pattern.compile("<tr[^>]*>");
		Matcher trMatcher = trPattern.matcher(tagContent);
		int trCount = 0;
		while (trMatcher.find()) {
			trCount++;
			if (trCount > 1) {
				return false;
			}
		}
		String tabla= extratorMatcherWithFilter(section, tagContent, module);
		Pattern tagPattern = Pattern.compile("(<"+tag+"[^>]*>[\\s\\S]*?</"+tag+">)");
		Matcher matcher = tagPattern.matcher(tabla);
		String lastTag = "";
		while (matcher.find()) {
			lastTr=lastTag;
			lastTag = matcher.group(1);


		}
		if (lastTr!=""){
			if (!(lastTr.contains(tagContent))){
				if (lastTag.contains("<strong") && !(tagContent.contains("<strong"))){
					return lastTr.contains(tagContent);
				}
			}

			lastTr=lastTag;
		}



		assert lastTag != null;
		return lastTr.contains(tagContent);

	}
	private int getTagIndexStart(String tag, String module, String lastLine) {
		Matcher matcher= getTagIndex(module, tag, lastLine);
		return  matcher.start();
	}
	private int getTagIndexEnd(String tag, String module,String lastLine) {
		Matcher matcher= getTagIndex(module, tag,lastLine);
		return  matcher.end();
	}
	private Matcher getTagIndex(String module, String tag,String lastLine) {
		int duplicate;
		if (isProduct(lastLine)){
			duplicate=1;
		}else{
			if (lastLine!=""){
				duplicate= duplicate(lastLine);
				if (duplicate==0){
					duplicate=1;
				}
			}else{
				duplicate=1;
			}
		}



		Matcher matcher = extratorMatcher(module);
		while (matcher.find()) {
			String moduleAux = matcher.group(1);
			if (moduleAux.equals(tag)) {
				if (duplicate==1){
					return matcher;
				}
				if (duplicate>1){
					duplicate--;
				}


			}
		}
		return matcher;
	}
	private int getRowNum(String tag, String module, String tagContent) {
		int count=0;
		Pattern tagPattern = Pattern.compile("(<" + tag + "[^>]*>[\\s\\S]*?</" + tag + ">)");
		Matcher matcher = tagPattern.matcher(module);
		while(matcher.find())
		{
			String content = matcher.group(1);
			count++;

		}
		return count;
	}
	private int getNumberOfPages(String filePath) throws IOException {
		try (PdfDocument pdfDocument = openPdfDocument(filePath)) {
			return pdfDocument.getNumberOfPages();
		}
	}
	private Matcher getReversedMatcher(String module, String tagContent, String tag) {
		if (module==null){
			return null;
		}
		List<String> reversedResults = new ArrayList<>();
		Pattern pattern = Pattern.compile("(<" + tag + "[^>]*>[\\s\\S]*?</" + tag + ">)");
		Matcher matcher = pattern.matcher(module);

		boolean startSaving = false;
		while (matcher.find()) {
			String content= matcher.group(1);
			if (content.contains(tagContent)) {
				startSaving = true;
				break;
			}
			if (!startSaving) {
				reversedResults.add(matcher.group(1));
			}

		}
		Collections.reverse(reversedResults);
		String reversedInput = String.join("", reversedResults);
		Matcher reversedMatcher = pattern.matcher(reversedInput);

		if (reversedMatcher.find()) {
			return reversedMatcher;
		} else {

			return pattern.matcher("");
		}
	}
	private int countRowsInTable(String tag, String module, String tagContent) {
		int count=0;
		Pattern tagPattern = Pattern.compile("(<" + tag + "[^>]*>[\\s\\S]*?</" + tag + ">)");
		Matcher matcher = tagPattern.matcher(module);
		while(matcher.find())
		{
			String content = matcher.group(1);
			count++;
			if (content.contains(tagContent)){
				return count;
			}
		}
		return count;
	}
	private String extratorModule(String tag, String module, String lastLine) {
		Matcher matcher=extratorMatcher(module);
		int numMatch=0;
		String moduleContent = null;
		while (matcher.find()) {
			moduleContent = matcher.group(1);
			if (moduleContent.toLowerCase().trim().contains(tag.trim().toLowerCase())) {
				numMatch+=1;

				if (tag.contains("Interés corriente")
						|| tag.contains("Interés moratorio")
						|| tag.contains("Reversión de intereses")
						|| tag.contains("Interés de otras líneas de financiamiento")
						|| tag.contains("Interés de periodos anteriores")) {

					if (moduleContent.contains("<strong>Detalle de pago</strong>") && getI() != 1) {
						continue;
					}

					return moduleContent;
				} else {
					int duplicate;
					if (lastLine!=""){
						duplicate= duplicate(lastLine);
						if (duplicate==0){
							duplicate=1;
						}
					}else{
						duplicate=1;
					}
					if (numMatch==duplicate){
						return moduleContent;
					}

				}

			}
		}
		return null;
	}
	private boolean extratorModuleText(String tag, String module, String text){
		Matcher matcher=extratorMatcher(module);
		String moduleContent = null;
		while (matcher.find()) {
			moduleContent = matcher.group(1);
			if (moduleContent.contains(tag) && moduleContent.contains(text)) {

				return true;
			}
		}
		return false;
	}

	public Matcher extratorMatcher(String tag ){
		Pattern tagPattern = Pattern.compile("(<"+tag+"[^>]*>[\\s\\S]*?</"+tag+">)");

		return tagPattern.matcher(getHtml());
	}
	public String extratorMatcherWithFilter(String module, String tagContent, String tag){
		Pattern pattern = Pattern.compile("(<" + tag + "[^>]*>[\\s\\S]*?</" + tag + ">)");
		Matcher matcher = pattern.matcher(module);
		while (matcher.find()){
			String content=matcher.group(1);
			if (content.contains(tagContent)){
				return content;
			}
		}
		return "";
	}
	private boolean fileExists(String filePath) {
		return new File(filePath).exists();
	}
	private void deleteFile(String filePath) {
		File archive = new File(filePath);
		archive.delete();
	}
	public String readerLastline(String page) {
		if (page.contains("cuenta.html.php"))
		{
			System.out.println("Error encontrado en el HTML: "+getName());
		}
		String[] lines = page.split("\\n");
		return lines[lines.length - 1];

	}
	public String readerPage(String filename) throws IOException {
		try (PdfReader pdf = new PdfReader(filename);
			 PdfDocument reopened = new PdfDocument(pdf)) {
			return PdfTextExtractor.getTextFromPage(reopened.getPage(getI()));
		}
	}
	private void modifyHtml(int index, String newContent) {
		modifiedHtml.setLength(0);
		modifiedHtml.append(html, 0,index)
				.append(newContent)
				.append(html, index, html.length());
		setHtml(modifiedHtml.toString());
	}
	private void replaceTag(int startIndex, int endIndex, String newContent) {
		modifiedHtml.setLength(0);
		modifiedHtml.append(html, 0, startIndex)
				.append(newContent)
				.append(html, endIndex, html.length());
		setHtml(modifiedHtml.toString());
	}


	private PdfDocument openPdfDocument(String filePath) throws IOException {
		PdfReader pdfReader = new PdfReader(filePath);
		return new PdfDocument(pdfReader);
	}
	private void closePdfDocument(PdfDocument pdfDocument) {
		pdfDocument.close();
	}
	private void setupPdfDocument(PdfDocument pdfDocument, PdfDocumentInfo pdfDocumentInfo, Document document) {
		document.setMargins(0, 0, 0, 0);
		pdfDocument.setUserProperties(false);
		pdfDocument.setDefaultPageSize(new PageSize(PageSize.LETTER));
		pdfDocumentInfo.setAuthor("Credix World S.A");
		pdfDocumentInfo.setCreator("Credix World S.A");
		pdfDocumentInfo.setSubject("Credix World S.A");
		pdfDocumentInfo.setTitle("Estado de Cuenta Credix World S.A");
		pdfDocumentInfo.addCreationDate();
	}
	private void propertiesPdf(PdfWriter pdfWriter, String str) {
		ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setCharset("UTF-8");
		PdfDocument pdfDocument = new PdfDocument(pdfWriter);
		PdfDocumentInfo pdfDocumentInfo = pdfDocument.getDocumentInfo();
		Document document = new Document(pdfDocument);
		setupPdfDocument(pdfDocument, pdfDocumentInfo, document);

		converterProperties.setCharset("UTF-8");
		try {
			HtmlConverter.convertToPdf(str, pdfDocument, converterProperties);
		} catch (Exception e) {
			System.out.println("Error converting HTML to PDF: " + e.getMessage());
			e.printStackTrace();
			throw e;
		} finally {
			pdfDocument.close();
		}

	}

	public void updatePdf() {
		try {
			PdfWriter pdfWriter = new PdfWriter(new File(getDirectory() + "/Update-" + getName()));
			propertiesPdf(pdfWriter, getHtml());

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		footerPdf();
	}
	private void footerPdf() {

		try {
			PdfReader lectorPDF = new PdfReader(getDirectory() + "/Update-" + getName());
			PdfWriter escritorPDF = new PdfWriter(getDirectory() + "/" + getName());
			PdfDocument pdf = new PdfDocument(lectorPDF, escritorPDF);

			for (int numeroPagina = 1; numeroPagina <= pdf.getNumberOfPages(); numeroPagina++) {
				PdfPage pagina = pdf.getPage(numeroPagina);
				PdfCanvas canvas = new PdfCanvas(pagina);

				canvas.beginText().setFontAndSize(PdfFontFactory.createFont(), 8)
						.setFillColorGray(0.5f)
						.moveText(580, 10)
						.showText(numeroPagina + "/" + pdf.getNumberOfPages())

						.endText();
			}

			pdf.close();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}



