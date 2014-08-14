package com.fleurida.camel.session;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;

import com.fleurida.camel.dyn.DynamicSourceRouter;

/**
 * Manages dynamic access to a {@link SessionResource}
 * 
 */
public class SessionResourceManager implements SessionResource {

	protected final transient Logger log = LoggerFactory.getLogger(getClass());

	@Produce
	ProducerTemplate template;

	@Override
	public ReleaseResourceRS release(ReleaseResourceRQ rq)
			throws ReleaseFailedException {

		Map<String, Object> headers = new HashMap<String, Object>();
		// for testing purporses only
		// need to set this for the dynamic router to piece together the target
		headers.put(DynamicSourceRouter.SOURCE_ADAPTER, "ADAPTER-ONE");

		// put on the actual route
		ReleaseResourceRS rs = template.requestBodyAndHeaders(
				SessionResourceRoute.DIRECT_START, rq, headers,
				ReleaseResourceRS.class);

		return rs;

	}

}
