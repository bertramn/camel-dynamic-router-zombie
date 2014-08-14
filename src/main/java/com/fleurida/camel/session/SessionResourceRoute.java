package com.fleurida.camel.session;

import org.apache.camel.builder.RouteBuilder;

import com.fleurida.camel.dyn.DynamicSourceRouter;

/**
 * Manages dynamic access to a {@link SessionResource}
 * 
 */
public class SessionResourceRoute extends RouteBuilder {

	// also use him
	public static String DIRECT_START = "direct-vm:PLATFORM/SessionResourceManager-1.0";

	@Override
	public void configure() throws Exception {

		from(DIRECT_START).bean(DynamicSourceRouter.build());

		// TODO add content based routing and check that the adapter name is set
	}

}
