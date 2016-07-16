package com.watchtogether.autonomic.selforg.red5.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.watchtogether.autonomic.selforg.red5.manager.components.api.Actuator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Adaptor;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Coordinator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.DecisionMaker;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Estimator;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Filter;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Model;
import com.watchtogether.autonomic.selforg.red5.manager.components.api.Sensor;
import com.watchtogether.autonomic.selforg.red5.manager.config.ConfigReader;
import com.watchtogether.autonomic.selforg.red5.manager.group.GroupManager;

public class ManagerStarter {

	private static Adaptor adaptor;
	private static List<Sensor> sensors = new ArrayList<>();
	private static List<Filter> filters = new ArrayList<>();
	private static Model model;
	private static Estimator estimator;
	private static DecisionMaker dm;
	private static Actuator actuator;
	private static Coordinator coordinator;
	
	private static final Logger logger = LoggerFactory.getLogger(ManagerStarter.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigReader reader = ConfigReader.getInstance(args[0]);
		
		reader.parseConfig();
		
		GroupManager groupManager = GroupManager.getManager();
		
		createSensor();
		filters.add((Filter)createComponent(reader.getFilterClass(), reader.getFilterParams()));
		model = (Model)createComponent(reader.getModelClass(), reader.getModelParams());
		estimator = (Estimator)createComponent(reader.getEstimatorClass(), reader.getEstimatorParams());
		dm = (DecisionMaker)createComponent(reader.getDecisionMakerClass(), reader.getDecisionMakerParams());
		actuator = (Actuator)createComponent(reader.getActuatorClass(), null);
		adaptor = (Adaptor)createComponent(reader.getAdaptorClass(), reader.getAdaptorParams());
		adaptor.setDecisionMaker(dm);
		createCoordinator();
		
		logger.info("Manager initialized");
		
		//set shutdown hook to terminate gracefully
		//this should pass the count token for the adaptor
		ControlLoopShutdownHook shutdownHook = new ControlLoopShutdownHook();
		Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
		
		startCoordinator();
		startAdaptor();
	}

	private static void startAdaptor() {
		Thread adaptThread = new Thread(adaptor);
		adaptThread.start();
		logger.info("Adaptor started");
	}

	private static void startCoordinator() {
		Thread coordThread = new Thread(coordinator);
		coordThread.start();
		logger.info("Coordinator started");
	}

	@SuppressWarnings("unchecked")
	private static void createCoordinator() {
		ConfigReader reader = ConfigReader.getInstance();
		
		String className = reader.getCoordinatorClass();
		try {
			Class<Coordinator> clazz = (Class<Coordinator>) Class.forName(className);
			Constructor<Coordinator>[] construct = (Constructor<Coordinator>[]) clazz.getDeclaredConstructors();
			coordinator = construct[0].newInstance(sensors, filters, model, estimator, dm, actuator, reader.getCoordinatorParams());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private static Object createComponent(String className, EnumMap<?, Float> params) {
		try {
			Class clazz = Class.forName(className);
			Constructor[] construct = clazz.getDeclaredConstructors();
			
			if (params != null)
				return (Object)construct[0].newInstance(params);
			else
				return (Object)construct[0].newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void createSensor() {
		ConfigReader reader = ConfigReader.getInstance();
		
		String className = reader.getSensorClass();
		try {
			Class<Sensor> clazz = (Class<Sensor>) Class.forName(className);
			Constructor<Sensor>[] construct = (Constructor<Sensor>[]) clazz.getDeclaredConstructors();
			sensors.add(construct[0].newInstance(reader.getManagedApp(), reader.getManagedPort()));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void shutdownGracefully() {
		adaptor.shutdownGracefully();
		logger.info("Manager shutdown");
	}

}
