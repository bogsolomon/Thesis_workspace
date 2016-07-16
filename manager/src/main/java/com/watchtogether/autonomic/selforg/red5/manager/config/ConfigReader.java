package com.watchtogether.autonomic.selforg.red5.manager.config;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.watchtogether.autonomic.selforg.red5.manager.components.util.AdaptorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.CoordinatorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.DecisionMakerParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.EstimatorParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.FilterParameterKeys;
import com.watchtogether.autonomic.selforg.red5.manager.components.util.ModelParameterKeys;

public class ConfigReader {

	private File configFile = null;
	private static ConfigReader instance = null;
	
	private static String MANAGED_SERVER = "managed-server";
	private static String MANAGE_GROUP = "management-group";
	
	private static String PARAM = "param";
	
	private static String SENSOR = "sensor";
	private static String FILTER = "filter";
	private static String MODEL = "model";
	private static String COORDINATOR = "coordinator";
	private static String ESTIMATOR = "estimator";
	private static String DECISION_MAKER = "decisionMaker";
	private static String ACTUATOR = "actuator";
	private static String ADAPTOR = "adaptor";
	
	private static String PORT = "port";
	private static String APP = "app";
	private static String GROUP_NAME = "group_name";
	private static String FILE_LOCATION = "file_location";
	
	private static String CLASS = "class";
	
	private static String NAME = "name";
	private static String VALUE = "value";
	
	private String managedPort;
	private String managedApp; 
	private String groupName;
	private String fileLocation;
		
	private String sensorClass;
	private String filterClass;
	private String modelClass;
	private String coordinatorClass;
	private String estimatorClass;
	private String decisionMakerClass;
	private String actuatorClass;
	private String adaptorClass;
	
	private EnumMap<FilterParameterKeys, Float> filterParams = new EnumMap<FilterParameterKeys, Float>(FilterParameterKeys.class);
	private EnumMap<ModelParameterKeys, Float> modelParams = new EnumMap<ModelParameterKeys, Float>(ModelParameterKeys.class);
	private EnumMap<CoordinatorParameterKeys, Float> coordinatorParams = new EnumMap<CoordinatorParameterKeys, Float>(CoordinatorParameterKeys.class);
	private EnumMap<EstimatorParameterKeys, Float> estimatorParams = new EnumMap<EstimatorParameterKeys, Float>(EstimatorParameterKeys.class);
	private EnumMap<DecisionMakerParameterKeys, Float> decisionMakerParams = new EnumMap<DecisionMakerParameterKeys, Float>(DecisionMakerParameterKeys.class);
	private EnumMap<AdaptorParameterKeys, Float> adaptorParams = new EnumMap<AdaptorParameterKeys, Float>(AdaptorParameterKeys.class);
	
	private ConfigReader(String f) {
		this.configFile = new File(f);
	}
	
	public static ConfigReader getInstance(String f) {
		if (ConfigReader.instance == null) {
			instance = new ConfigReader(f);
		} 
		
		return instance;
	}
	
	public static ConfigReader getInstance() {
		return instance;
	}

	public void parseConfig() {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = fact.newDocumentBuilder();
			Document doc = builder.parse(configFile);
			doc.normalizeDocument();
			
			Element managedServer = (Element)doc.getElementsByTagName(MANAGED_SERVER).item(0);
			
			managedPort = managedServer.getAttribute(PORT);
			managedApp = managedServer.getAttribute(APP);
			
			Element managementGroup = (Element)doc.getElementsByTagName(MANAGE_GROUP).item(0);
			
			groupName = managementGroup.getAttribute(GROUP_NAME);
			fileLocation = managementGroup.getAttribute(FILE_LOCATION);
			
			extractSensor(doc);
			
			extractFilter(doc);
			
			extractModel(doc);
			
			extractCoordinator(doc);
			
			extractEstimator(doc);
			
			extractDecisionMaker(doc);
			
			extractActuator(doc);
			
			extractAdaptor(doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void extractAdaptor(Document doc) {
		Element adaptorConfig = (Element)doc.getElementsByTagName(ADAPTOR).item(0);
		
		adaptorClass = adaptorConfig.getAttribute(CLASS);
		
		extractParams(adaptorConfig, AdaptorParameterKeys.class, adaptorParams);
	}

	private void extractActuator(Document doc) {
		Element actuatorConfig = (Element)doc.getElementsByTagName(ACTUATOR).item(0);
		
		actuatorClass = actuatorConfig.getAttribute(CLASS);
	}

	private void extractDecisionMaker(Document doc) {
		Element dmConfig = (Element)doc.getElementsByTagName(DECISION_MAKER).item(0);
		
		decisionMakerClass = dmConfig.getAttribute(CLASS);
		
		extractParams(dmConfig, DecisionMakerParameterKeys.class, decisionMakerParams);
	}

	private void extractEstimator(Document doc) {
		Element coordinatorConfig = (Element)doc.getElementsByTagName(ESTIMATOR).item(0);
		
		estimatorClass = coordinatorConfig.getAttribute(CLASS);
		
		extractParams(coordinatorConfig, EstimatorParameterKeys.class, estimatorParams);
	}

	private void extractCoordinator(Document doc) {
		Element coordinatorConfig = (Element)doc.getElementsByTagName(COORDINATOR).item(0);
		
		coordinatorClass = coordinatorConfig.getAttribute(CLASS);
		
		extractParams(coordinatorConfig, CoordinatorParameterKeys.class, coordinatorParams);
	}

	private void extractModel(Document doc) {
		Element modelConfig = (Element)doc.getElementsByTagName(MODEL).item(0);
		
		modelClass = modelConfig.getAttribute(CLASS);
		
		extractParams(modelConfig, ModelParameterKeys.class, modelParams);
	}

	private <T extends Enum<T>> void extractParams(Element config, Class<T> classType, Map<T, Float> params) {
		NodeList nodeList = config.getElementsByTagName(PARAM);
		
		for (int i=0; i<nodeList.getLength(); i++) {
			Element e = (Element)nodeList.item(i);
			String name = e.getAttribute(NAME);
			Float value = Float.parseFloat(e.getAttribute(VALUE));
			params.put(Enum.valueOf(classType, name), value);
		}
	}

	private void extractFilter(Document doc) {
		Element filterConfig = (Element)doc.getElementsByTagName(FILTER).item(0);
		
		filterClass = filterConfig.getAttribute(CLASS);
		
		extractParams(filterConfig, FilterParameterKeys.class, filterParams);
	}

	private void extractSensor(Document doc) {
		Element sensorConfig = (Element)doc.getElementsByTagName(SENSOR).item(0);
		
		sensorClass = sensorConfig.getAttribute(CLASS);
	}

	public String getManagedPort() {
		return managedPort;
	}

	public String getManagedApp() {
		return managedApp;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getSensorClass() {
		return sensorClass;
	}

	public String getFilterClass() {
		return filterClass;
	}

	public EnumMap<FilterParameterKeys, Float> getFilterParams() {
		return filterParams;
	}

	public String getModelClass() {
		return modelClass;
	}

	public EnumMap<ModelParameterKeys, Float> getModelParams() {
		return modelParams;
	}

	public String getCoordinatorClass() {
		return coordinatorClass;
	}

	public EnumMap<CoordinatorParameterKeys, Float> getCoordinatorParams() {
		return coordinatorParams;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public String getEstimatorClass() {
		return estimatorClass;
	}

	public String getDecisionMakerClass() {
		return decisionMakerClass;
	}

	public String getActuatorClass() {
		return actuatorClass;
	}

	public String getAdaptorClass() {
		return adaptorClass;
	}

	public EnumMap<EstimatorParameterKeys, Float> getEstimatorParams() {
		return estimatorParams;
	}

	public EnumMap<DecisionMakerParameterKeys, Float> getDecisionMakerParams() {
		return decisionMakerParams;
	}

	public EnumMap<AdaptorParameterKeys, Float> getAdaptorParams() {
		return adaptorParams;
	}
}
