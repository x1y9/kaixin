package com.kaixin.core.filter;

import com.kaixin.core.util.MetricUtil;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/*
 * Jersey的listener，目前用于性能记录
 */
public class MetricListener implements ApplicationEventListener {

	@Override
	public void onEvent(ApplicationEvent event) {
	}

	@Override
	public RequestEventListener onRequest(RequestEvent requestEvent) {
        if (PropsUtil.getBoolean(PropsKeys.METRIC_REST_REQUEST_ENABLE))
            return new FusionRequestEventListener();
        else
            return null;
	}

	public static class FusionRequestEventListener implements RequestEventListener {
        private volatile long startTime;

        @Override
        public void onEvent(RequestEvent requestEvent) {
            switch (requestEvent.getType()) {
                case RESOURCE_METHOD_START:
                    startTime = MetricUtil.getCurrentTime();
                    break;

                case RESOURCE_METHOD_FINISHED:
                    String url = requestEvent.getContainerRequest().getMethod().toLowerCase() + "." + requestEvent.getContainerRequest().getPath(false);
    	    		MetricUtil.updateTimer(PropsUtil.getBoolean(PropsKeys.METRIC_REST_REQUEST_ENABLE), "http." + url, startTime);

                    break;
            }
        }
    }

}
