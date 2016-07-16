package com.watchtogether.load.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class LogStatsParser {

	public static void main(String[] args) {
		File logFile = new File(args[0]);
		String outFileName = args[1];
		HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();
		
		BufferedReader br = null;
		BufferedWriter bw1 = null;
		//BufferedWriter bw2 = null;
		
		String line;
				
		int increment = 5;
		
		try {
			for (int j=1;j<11;j++) {
				int i=1;
				
				String name = args[0].replace(increment+"u", (increment*j)+"u");
				
				br = new BufferedReader(new FileReader(name));
				bw1 = new BufferedWriter(new FileWriter(outFileName+(j*5)+"users_56.txt"));
				//bw1.write("time,clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
				bw1.write("clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
				
				//bw2 = new BufferedWriter(new FileWriter(outFileName+(j*10)+"users_64.txt"));
				//bw2.write("time,clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
				
				while ((line = br.readLine())!=null) {
					if (line.contains("<stats>")) {
						String time = line.substring(0, line.indexOf(" "));
						String server = line.substring(line.indexOf("rtmp:")+5, line.indexOf("/wtRed5_cld"));
						server = server.replaceAll("\\.", "_");
						server = server.replaceAll("\\:", "_");
						
						/*if (!writerMap.containsKey(server)) {
							File f = new File(outFileName+server+".log");
							if (!f.exists())
								f.getParentFile().mkdirs();
							BufferedWriter bwNew = new BufferedWriter(new FileWriter(outFileName+server+".log"));
							bwNew.write("time,clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
							writerMap.put(server, bwNew);
						}
						
						bw = writerMap.get(server);*/
						
						String clients = line.substring(line.indexOf("<users>")+7, line.indexOf("</users>"));
						String localClients = line.substring(line.indexOf("<localUsers>")+"<localUsers>".length(), line.indexOf("</localUsers>"));
						String streamsIn = line.substring(line.indexOf("<clientStreams>")+"<clientStreams>".length(), line.indexOf("</clientStreams>"));
						String streamsOut = line.substring(line.indexOf("<clientOutStreams>")+"<clientOutStreams>".length(), line.indexOf("</clientOutStreams>"));;
						String latency = line.substring(line.indexOf("<avgLatency2>")+"<avgLatency2>".length(), line.indexOf("</avgLatency2>"));
						String bwIn = line.substring(line.indexOf("<clientInStreamBw>")+"<clientInStreamBw>".length(), line.indexOf("</clientInStreamBw>"));
						String bwOut = line.substring(line.indexOf("<clientOutStreamBw>")+"<clientOutStreamBw>".length(), line.indexOf("</clientOutStreamBw>"));
						String cpu = line.substring(line.indexOf("<cpu>")+"<cpu>".length(), line.indexOf("</cpu>"));
						
						if (server.contains("6_4")) {
							//bw2.write(time+","+clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
						} else {
							//bw1.write(time+","+clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
							bw1.write(clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
						}
						
						if (i == 134)
							break;
						
						i++;
					}
				}
				
				/*Iterator<Entry<String, BufferedWriter>> it = writerMap.entrySet().iterator();
				
				while (it.hasNext()) {
					BufferedWriter bw1 = it.next().getValue();
					bw1.flush();
					bw1.close();
				}*/
				bw1.flush();
				bw1.close();
				
				//bw2.flush();
				//bw2.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

