package com.watchtogether.chart.datasource;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SinglePointDatasource extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String count = req.getParameter("count");
		String dataSizeReq = req.getParameter("dataSize");
		String dataSizeType = req.getParameter("dataType");
		
		resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		resp.setDateHeader("Expires", 0); // Proxies.
		
		String dataSize;
		
		if (dataSizeReq == null) {
			dataSize = getServletContext().getInitParameter("dataSize");
		} else {
			dataSize = dataSizeReq;
		}
		
		Writer outWriter = resp.getWriter();
		
		List<? extends Number> value;
		
		if (dataSizeType == null) {
			value = generateDoubleData(dataSize, count);
		} else if (dataSizeType.equals("float")) {
			value = generateFloatData(dataSize, count);
		} else {
			value = new ArrayList<Integer>();
		}
		
		generateJSON(outWriter, value);
	}

	private void generateJSON(Writer outWriter, List<? extends Number> chartData) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(outWriter, chartData);
	}

	private List<Double> generateDoubleData(String dataSize, String count) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		List<Double> data = new ArrayList<>();
		
		for (int i=0;i<Integer.parseInt(count);i++) {
			data.add(random.nextDouble(Double.parseDouble(dataSize)));
		}
		
		return data;
	}
	
	private List<Float> generateFloatData(String dataSize, String count) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		List<Float> data = new ArrayList<>();
		
		for (int i=0;i<Integer.parseInt(count);i++) {
			data.add(random.nextFloat()*Float.parseFloat(dataSize));
		}
		
		return data;
	}
}
