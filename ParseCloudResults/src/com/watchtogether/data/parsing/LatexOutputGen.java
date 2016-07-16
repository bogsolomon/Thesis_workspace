package com.watchtogether.data.parsing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LatexOutputGen {

	public static void main(String[] args) {
		Path folderPath = Paths.get(args[0]);

		try (DirectoryStream<Path> stream = Files
				.newDirectoryStream(folderPath)) {
			for (Path entry : stream) {
				processDataFolder(entry, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processDataFolder(Path folderPath, String testCaseIn) throws IOException {
		Path outputFile = Paths.get(folderPath.toString(), "latex_code.txt");
		Files.deleteIfExists(outputFile);
		Files.createFile(outputFile);
		BufferedWriter writer = Files.newBufferedWriter(outputFile,
				Charset.defaultCharset());
		String testCase = folderPath.getName(folderPath.getNameCount() - 1)
				.toString();
		
		if (testCaseIn != null) {
			testCase = testCaseIn;
		}
		
		try (DirectoryStream<Path> stream = Files
				.newDirectoryStream(folderPath)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) { 
					processDataFolder(entry, testCase);
				} else {
					String name = entry.getName(entry.getNameCount() - 1).toString();
					if (name.endsWith(".png")) {
						processImage(writer, testCase, entry, name);
					} else if (name.equals("results.txt")) {
						processTable(writer, testCase, entry, name);
					}
				}
			}
		}
		
		writer.close();
	}

	private static void processTable(BufferedWriter writer, String testCase,
			Path entry, String name) throws IOException {
		List<String> lines = Files.readAllLines(entry, Charset.defaultCharset());
		List<Double> cpuMeans = new ArrayList<>();
		List<Double> cpuMedians = new ArrayList<>();
		List<Double> latMeans = new ArrayList<>();
		List<Double> latMedians = new ArrayList<>();
		List<Double> msgLatMeans = new ArrayList<>();
		List<Double> msgLatMedians = new ArrayList<>();
		int type = 0;
		int lineCnt = 0;
		
		for (String line:lines) {
			if (line.startsWith("CPU Stats")) {
				type = 1;
				lineCnt = 0;
			} else if (line.startsWith("Latency Stats")) {
				type = 2;
				lineCnt = 0;
			} else if (line.startsWith("Message Lat Stats")) {
				type = 3;
				lineCnt = 0;
			} else if (line.length() > 0) {
				if (lineCnt == 0) {
					String[] tokens = line.split("	");
					for (String token:tokens) {
						Double val = Double.valueOf(token);
						
						if (type == 1) {
							cpuMedians.add(val);
						} else if (type == 2) {
							latMedians.add(val);
						} else if (type == 3) {
							msgLatMedians.add(val);
						}
					}
				} else if (lineCnt == 1) {
					String[] tokens = line.split("	");
					
					for (String token:tokens) {
						Double val = Double.valueOf(token);
						
						if (type == 1) {
							cpuMeans.add(val);
						} else if (type == 2) {
							latMeans.add(val);
						} else if (type == 3) {
							msgLatMeans.add(val);
						}
					}
				}
				
				lineCnt++;
			}
		}
		
		String val = testCase;

		int startIndex = val.indexOf("serv");
		int serverCount = Integer.valueOf(val.substring(
				startIndex - 1, startIndex));
		startIndex = val.indexOf("sess");
		int sessCount = Integer.valueOf(val.substring(
				startIndex - 1, startIndex));
		startIndex = val.indexOf("str");
		int strCount = Integer.valueOf(val.substring(
				startIndex - 1, startIndex));
		int cldCount = -1;

		startIndex = val.indexOf("cld");
		if (startIndex != -1) {
			cldCount = Integer.valueOf(val.substring(
					startIndex - 1, startIndex));
		}
		
		writer.write("\\begin{table}");
		writer.newLine();
		if (cldCount == -1) {
			writer.write("\\caption{Median and Mean CPU, Latencies for "
					+ serverCount + " Server, "
					+ sessCount + " Session, " + strCount + " Stream}");
		} else {
			writer.write("\\caption{Median and Mean CPU, Latencies for "
					+ cldCount + " Cloud, " + serverCount + " Server, "
					+ sessCount + " Session, " + strCount + " Stream}");
		}
		writer.newLine();
		if (cldCount == -1) {
			writer.write("\\label{table:" + serverCount + "serv_"
					+ sessCount + "sess_" + strCount + "str"
					+ "}");
		} else {
			writer.write("\\label{table:"+ cldCount + "cld_" 
					+ serverCount + "serv_"
					+ sessCount + "sess_" + strCount + "str"
					+ "}");
		}
		writer.newLine();
		writer.write("\\begin{tabu} to\\linewidth{|X[c]|X[c]|X[c]|X[c]|X[c]|X[c]|X[c]|X[c]|}");
		writer.newLine();
		writer.write("\\everyrow{\\hline}");
		writer.newLine();
		writer.write("\\hline");
		writer.newLine();
		writer.write("Number of Users & Median Latency (ms) & Mean Latency (ms) & Median Message Latency (ms) & Mean Message Latency (ms) & Median CPU (\\%) & Mean CPU (\\%) & Outgoing Streams\\\\");
		writer.newLine();
		writer.write("\\taburowcolors 2{Gray!20..LimeGreen!50}");
		writer.newLine();
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		for (int i=0;i<cpuMeans.size();i++) {
			writer.write((i+2)+" & "+ df.format(latMedians.get(i))+" & "+df.format(latMeans.get(i))+" & "
					+ df.format(msgLatMedians.get(i))+" & "+df.format(msgLatMeans.get(i))+" & "
					+ df.format(cpuMedians.get(i))+" & "+df.format(cpuMeans.get(i))+" & "+ 0+" \\\\");
			writer.newLine();
		}
		
		writer.write("\\end{tabu}");
		writer.newLine();
		writer.write("\\end{table}");
		writer.newLine();
	}

	private static void processImage(BufferedWriter writer, String testCase,
			Path entry, String name) throws IOException {
		String type = null;

		if (name.startsWith("latency")) {
			type = "Latencies";
		} else if (name.startsWith("cpu")) {
			type = "CPU";
		} else if (name.startsWith("bandwidth")) {
			type = "Bandwidth";
		} else {
			type = "Message Latencies";
		}

		int startIndex = name.indexOf("-") + 1;
		int userCount = Integer.valueOf(name.substring(
				startIndex++, name.indexOf(".")));

		String val = testCase;

		startIndex = val.indexOf("serv");
		int serverCount = Integer.valueOf(val.substring(
				startIndex - 1, startIndex));
		startIndex = val.indexOf("sess");
		int sessCount = Integer.valueOf(val.substring(
				startIndex - 1, startIndex));
		startIndex = val.indexOf("str");
		int strCount = Integer.valueOf(val.substring(
				startIndex - 1, startIndex));
		int cldCount = -1;

		startIndex = val.indexOf("cld");
		if (startIndex != -1) {
			cldCount = Integer.valueOf(val.substring(
					startIndex - 1, startIndex));
		}

		writer.write("\\begin{figure}");
		writer.newLine();
		writer.write("\\centering");
		writer.newLine();
		writer.write("\\includegraphics[width=0.9\\linewidth]{redesigned_code_final/"
				+ testCase
				+ "/"
				+ entry.getName(entry.getNameCount() - 1) + "}");
		writer.newLine();
		if (cldCount == -1) {
			if (type.equals("Bandwidth")) {
				writer.write("\\caption{" + type + " for "
						+ (userCount + 1) + " to " + (userCount + 7)
						+ " Clients - "
						+ serverCount + " Server, " + sessCount
						+ " Session, " + strCount + " Stream}");
			} else {
				writer.write("\\caption{" + type + " for "
						+ (userCount + 1) + ", " + (userCount + 2)
						+ " and " + (userCount + 3) + " Clients - "
						+ serverCount + " Server, " + sessCount
						+ " Session, " + strCount + " Stream}");
			}
		} else {
			writer.write("\\caption{" + type + " for "
					+ (userCount + 1) + ", " + (userCount + 2)
					+ " and " + (userCount + 3) + " Clients - "
					+ cldCount + " Cloud, "
					+ serverCount + " Server, " + sessCount
					+ " Session, " + strCount + " Stream}");
		}
		writer.newLine();
		if (cldCount == -1) {
			writer.write("\\label{fig:" + serverCount + "serv_"
					+ sessCount + "sess_" + strCount + "str_"
					+ (userCount + 1) + "u_" + name.substring(0, 3)
					+ "}");
		} else {
			writer.write("\\label{fig:"+ cldCount + "cld_" 
					+ serverCount + "serv_"
					+ sessCount + "sess_" + strCount + "str_"
					+ (userCount + 1) + "u_" + name.substring(0, 3)
					+ "}");
		}
		writer.newLine();
		writer.write("\\end{figure}");
		writer.newLine();
	}

}
