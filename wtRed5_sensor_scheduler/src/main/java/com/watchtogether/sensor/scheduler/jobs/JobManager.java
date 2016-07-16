package com.watchtogether.sensor.scheduler.jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdScheduler;

import com.watchtogether.sensor.scheduler.entity.ObservationOffering;
import com.watchtogether.sensor.scheduler.entity.SensorSubscription;

public class JobManager {

	private static Map<String, JobDetail> sensorIdToTrigger = new HashMap<String, JobDetail>();

	public static void createJobForNewSensor(SensorSubscription subs,
			Map<String, String> sensorIdToRequestURL,
			List<ObservationOffering> offerings, Properties jmsProperties, Scheduler scheduler) {

		JobDetail jd = JobBuilder.newJob(SensorDataJob.class).withIdentity(
				"sensorjob" + subs.getSensorId()).build();

		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("sensorjobtrigger" + subs.getSensorId())
				.withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								//.withIntervalInMinutes(6).repeatForever())
							   .withIntervalInSeconds(30).repeatForever())
				.startAt(new Date()).build();

		jd.getJobDataMap().put("requestURLFormat",
				sensorIdToRequestURL.get(subs.getSensorId()));

		String offeringId = "";

		for (ObservationOffering off : offerings) {
			if (off.getStationId().equals(subs.getSensorId())) {
				offeringId = off.getStationName();
				break;
			}
		}

		System.out.println("Creating new job with datamap: ");
		System.out.println("offeringId: " + offeringId);
		System.out.println("sensorInfo: " + subs.getSensorId());

		jd.getJobDataMap().put("offeringId", offeringId);
		jd.getJobDataMap().put("sensorInfo", subs);
		jd.getJobDataMap().put("jmsProps", jmsProperties);

		try {
			scheduler.scheduleJob(jd, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		sensorIdToTrigger.put(subs.getSensorId(), jd);
	}

	public static void modifySensorJob(SensorSubscription subs, Scheduler scheduler) {
		System.out
				.println("Modifying job with sensorID: " + subs.getSensorId());

		JobDetail jd = sensorIdToTrigger.get(subs.getSensorId());

		JobDataMap jdMap = jd.getJobDataMap();
		jdMap.put("sensorInfo", subs);

		try {
			scheduler.addJob(jd, true);
			scheduler.triggerJob(jd.getKey(), jdMap);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public static void removeJobForSensor(SensorSubscription subs, Scheduler scheduler) {
		System.out.println("Deleting job with sensorID: " + subs.getSensorId());

		JobDetail jd = sensorIdToTrigger.remove(subs.getSensorId());

		try {
			scheduler.deleteJob(jd.getKey());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
