package com.kaixin.core.api;

import com.codahale.metrics.Timer;
import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.util.MetricUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.*;

@Path("/api/metric")
@Produces(MediaType.APPLICATION_JSON)
public class MetricApi {

	private static final Logger _log = LoggerFactory.getLogger(MetricApi.class);


	@GET
	@Path("all")
	public Object all() {
		return MetricUtil.getMetric();
		
	}

	@GET
	@Path("timer")
	public Object timer(@QueryParam("name") String name) {
		SortedMap<String, Timer> timers = MetricUtil.getMetric().getTimers();
		if (name == null)
			return timers;
		else if (timers.containsKey(name))
			return timers.get(name);
		else
			return "{}";
	}

	/*
	 * 给UI用的接口 ，并把单位换算为毫秒
	 */
	@GET
	@Path("list")
	public List<Map> list()  {
		SortedMap<String, Timer> timers = MetricUtil.getMetric().getTimers();

		List<Map> results = new ArrayList<>();
		for (String key : timers.keySet()) {
			Map<String,Object> timer = new HashMap<>();
			timer.put("name", key);
			timer.put("totalTime", timers.get(key).getCount() * timers.get(key).getSnapshot().getMean() / 1000);
			timer.put("count", timers.get(key).getCount());
			timer.put("meanRate", timers.get(key).getMeanRate());
			timer.put("oneMinuteRate", timers.get(key).getOneMinuteRate());
			timer.put("fiveMinuteRate", timers.get(key).getFiveMinuteRate());
			timer.put("fifteenMinuteRate", timers.get(key).getFifteenMinuteRate());

			timer.put("meanMs", timers.get(key).getSnapshot().getMean() / 1000);
			timer.put("medianMs", timers.get(key).getSnapshot().getMedian() / 1000);
			timer.put("maxMs", timers.get(key).getSnapshot().getMax() / (double)1000);
			timer.put("minMs", timers.get(key).getSnapshot().getMin() / (double)1000);
			timer.put("75thMs", timers.get(key).getSnapshot().get75thPercentile() / 1000);
			timer.put("95thMs", timers.get(key).getSnapshot().get95thPercentile() / 1000);
			timer.put("99thMs", timers.get(key).getSnapshot().get99thPercentile() / 1000);
			results.add(timer);
		}
		return results;
	}

}
