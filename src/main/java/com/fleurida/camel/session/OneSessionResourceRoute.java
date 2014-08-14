package com.fleurida.camel.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;

public class OneSessionResourceRoute extends RouteBuilder {

	protected final transient Logger log = LoggerFactory.getLogger(getClass());

	public static String DIRECT_START = "direct-vm:ADAPTER-ONE/ReleaseResourceRQ-1.0";

	@Override
	public void configure() throws Exception {

		from(DIRECT_START).routeId(DIRECT_START)
				// set the operation name on the route so CXF doesn't get
				// confused
				.setHeader(CxfConstants.OPERATION_NAME, simple("Release"))
				.setHeader(CxfConstants.OPERATION_NAMESPACE,
						simple("http://fleurida.com/camel/session"))
				// cxf out
				.to("cxf:bean:sessionResourceSE");

	}

}
