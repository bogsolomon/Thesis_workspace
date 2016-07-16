package com.watchtogether.cloud.loadbalancer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.watchtogether.cloud.loadbalancer.dao.CloudLoadBalancer;
import com.watchtogether.cloud.loadbalancer.dao.Config;

/**
 * Receives a request from a Flash client and returns a list of clouds the
 * client should use in JSON format, based on IP information regarding the
 * client.
 * 
 * @author Bogdan Solomon
 * 
 */
public class HighLevelLoadBalancerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Random rand = new Random();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String remoteHost = req.getRemoteHost();

		Config config = ConfigReader.getConfig();
		
		List<CloudLoadBalancer> cloudBalancers = config.getLoadBalancers();
		
		Config returnConfig = null;
		
		if (cloudBalancers.size() > config.getMaxReturnLB()) {
			returnConfig = new Config();
			List<CloudLoadBalancer> returnedBalancers = new ArrayList<>();
			
			//for the moment we just choose some random load balancers to return
			Set<Integer> returnedIndexes = new TreeSet<Integer>();
			
			while (returnedIndexes.size() < config.getMaxReturnLB()) {
				int index = rand.nextInt(cloudBalancers.size());
				
				if (!returnedIndexes.contains(index)) {
					returnedBalancers.add(cloudBalancers.get(index));
					returnedIndexes.add(index);
				}
			}
			
			returnConfig.setLoadBalancers(returnedBalancers);
		} else {
			returnConfig = config;
		}
		
		ConfigReader.marshalConfig(returnConfig, resp.getWriter());
	}
}
