package com.watchtogether.load.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class LongLogStatsParser {

	public static void main(String[] args) {
		String outFileName = args[1];
		HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();
		
		BufferedReader br = null;
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		
		BufferedWriter messageLatencyBw = null;
		
		String line;
		
		String serv1 = null;
		String serv2 = null;
		
		File f = new File(outFileName);
		if (!f.exists()) {
			f.mkdirs();
			
			f = new File(outFileName+"\\serv1\\");
			f.mkdir();
			
			f = new File(outFileName+"\\serv2\\");
			f.mkdir();
		}
				
		try {
			br = new BufferedReader(new FileReader(args[0]));
			
			while ((line = br.readLine())!=null) {
				if (line.contains("TEST STARTED")) {
					String nr = line.substring(line.indexOf("count:")+7, line.indexOf("--")-1);
					
					Integer number = Integer.parseInt(nr);
					if (number < 10) {
						nr = "0"+nr;
					}
					
					bw1 = new BufferedWriter(new FileWriter(outFileName+"\\serv1\\"+nr+"users_serv1.txt"));
					bw2 = new BufferedWriter(new FileWriter(outFileName+"\\serv2\\"+nr+"users_serv2.txt"));
					messageLatencyBw = new BufferedWriter(new FileWriter(outFileName+"\\"+nr+"_messageLats.txt"));
					//bw1.write("time,clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
					bw1.write("clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
					bw2.write("clients,localClients,streamsIn,streamsOut,latency,bwIn,bwOut,cpu"+System.getProperty("line.separator"));
				} else if (line.contains("TEST ENDED")) {
					bw1.flush();
					bw1.close();
					bw2.flush();
					bw2.close();
					messageLatencyBw.flush();
					messageLatencyBw.close();
				} else if (line.contains("<stats>")) {
					String time = line.substring(0, line.indexOf(" "));
					String server = line.substring(line.indexOf("rtmp:")+5, line.indexOf("/wtRed5_cld"));
					server = server.replaceAll("\\.", "_");
					server = server.replaceAll("\\:", "_");
					
					if (serv1 == null) {
						serv1 = server;
					} else if (!serv1.equals(server)) {
						serv2 = server;
					}
					
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
					
					Integer streamsInInt = Integer.parseInt(streamsIn); 
					
					if (streamsInInt > 0) {
						if (server.equals(serv1)) {
							//bw2.write(time+","+clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
							bw1.write(clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
						} else if (server.equals(serv2)) {
							//bw1.write(time+","+clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
							bw2.write(clients+","+localClients+","+streamsIn+","+streamsOut+","+latency+","+bwIn+","+bwOut+","+cpu+System.getProperty("line.separator"));
						}
					}
				} else if (line.contains("sendMediaMessage")) {
					String messageLatency = line.substring(line.indexOf("sendMediaMessage")+"sendMediaMessage ".length());
					
					messageLatencyBw.write(messageLatency+System.getProperty("line.separator"));
				}
				
				/*Iterator<Entry<String, BufferedWriter>> it = writerMap.entrySet().iterator();
				
				while (it.hasNext()) {
					BufferedWriter bw1 = it.next().getValue();
					bw1.flush();
					bw1.close();
				}*/
				
				//bw2.flush();
				//bw2.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

