package com.watchtogether.data.parsing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ModifyLatexTables {

	public static void main(String[] args) throws IOException {
		Path filePath = Paths.get(args[0]);
		Path fileOutPath = Paths.get(args[1]);

		List<String> lines = Files.readAllLines(filePath,
				Charset.defaultCharset());
		BufferedWriter bw = Files.newBufferedWriter(fileOutPath,
				Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);

		bw.write("\\chapter{Model Identification Performance Results} % Write in your own chapter title");
		bw.newLine();
		bw.write("\\label{appendix:modelidentifperfresults}");
		bw.newLine();
		bw.write("\\lhead{Appendix \\ref{appendix:modelidentifperfresults}. \\emph{Model Identification Performance Results}} % Write in your own chapter title to set the page header");
		bw.newLine();
		bw.newLine();
		
		int figureCount = 0;
		int maxFig = 6;
		String figureLoc = "";
		String caption = "";
		String label = "";

		for (String line : lines) {
			if (line.contains("\\begin{figure}")) {
				if (figureCount == 0) {
					bw.write(line);
					bw.newLine();
					bw.write("\\centering");
					bw.newLine();
				}

				figureCount++;
			} else if (line.startsWith("\\includegraphics")) {
				figureLoc = line.substring(line.indexOf("{") + 1,
						line.lastIndexOf("}"));
			} else if (line.startsWith("\\caption")) {
				caption = line.substring(line.indexOf("{") + 1,
						line.lastIndexOf("}"));
			} else if (line.startsWith("\\label")) {
				label = line.substring(line.indexOf("{") + 1,
						line.lastIndexOf("}"));
			} else if (line.contains("\\end{figure}")) {
				bw.write("\\subfloat["+caption+"]{");
				bw.newLine();
				bw.write("\\includegraphics[width=0.45\\linewidth]{"+figureLoc+"}");
				bw.newLine();
				bw.write("\\label{"+label+"}}");
				bw.newLine();
				
				if (figureCount == maxFig) {
					maxFig = 10;
					figureCount = 0;
					bw.write("\\end{figure}");
					bw.newLine();
					bw.write("\\clearpage");
					bw.newLine();
				} else {
					bw.write("\\quad");
					bw.newLine();
				}
			}
		}

		bw.write("\\end{figure}");
		bw.newLine();
		bw.close();
	}

}
