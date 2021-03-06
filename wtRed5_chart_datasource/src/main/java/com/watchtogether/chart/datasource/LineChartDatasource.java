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

public class LineChartDatasource extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String count = req.getParameter("count");
		int size = 1;
		
		if (count != null) {
			size = Integer.parseInt(count);
		}
		
		resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		resp.setDateHeader("Expires", 0); // Proxies.
		
		String minDataPointCount = getServletContext().getInitParameter("minDataPointCount");
		String maxDataPointCount = getServletContext().getInitParameter("maxDataPointCount");
		String dataSize = getServletContext().getInitParameter("dataSize");
		
		Writer outWriter = resp.getWriter();
		
		List<List<Double>> data = new ArrayList<>();
		List<Double> chartData = generateData(Integer.parseInt(minDataPointCount), Integer.parseInt(maxDataPointCount), Double.parseDouble(dataSize));
		data.add(chartData);
		
		for (int i=1;i<size;i++) {
			chartData = generateData(chartData.size(), Double.parseDouble(dataSize));
			data.add(chartData);
		}
		
		generateJSON(outWriter, data);
	}

	private void generateJSON(Writer outWriter, List<List<Double>> chartData) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(outWriter, chartData);
	}

	private List<Double> generateData(int minCount, int maxCount, double dataSize) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		double size = random.nextInt(minCount, maxCount);
		
		List<Double> data = new ArrayList<>();
		
		for (int i=0;i<size;i++) {
			data.add(random.nextDouble(dataSize));
		}
		
		return data;
	}
	
	private List<Double> generateData(int size, double dataSize) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		List<Double> data = new ArrayList<>();
		
		for (int i=0;i<size;i++) {
			data.add(random.nextDouble(dataSize));
		}
		
		return data;
	}
}
