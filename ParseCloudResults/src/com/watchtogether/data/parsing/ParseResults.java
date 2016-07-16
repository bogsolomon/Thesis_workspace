package com.watchtogether.data.parsing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ParseResults {

	public static void main(String[] args) {
		try {
			processFolder(Paths.get(args[0]), Paths.get(args[1]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void processFolder(Path inPath, Path outPath)
			throws IOException {
		if (!Files.exists(outPath)) {
			Files.createDirectory(outPath);
		}
		
		for (Path path : Files.newDirectoryStream(inPath)) {
			if (Files.isDirectory(path)) {
				processFolder(path, Paths.get(outPath.toString(), path
						.getFileName().toString()));
			} else {
				processFile(path, Paths.get(outPath.toString(), path
						.getFileName().toString()));
			}
		}
	}

	private static void processFile(Path inPath, Path outPath) throws IOException {
		List<String> lines = Files.readAllLines(inPath, Charset.defaultCharset());
		
		Files.createFile(outPath);
		BufferedWriter bw = Files.newBufferedWriter(outPath, Charset.defaultCharset());
		
		for (String line:lines) {
			if (!line.contains("Error")) {
				bw.write(line);
				bw.newLine();
			}
		}
		
		bw.close();
	}

}
