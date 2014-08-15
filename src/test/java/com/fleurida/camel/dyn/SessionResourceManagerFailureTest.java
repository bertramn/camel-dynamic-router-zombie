package com.fleurida.camel.dyn;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

import com.fleurida.camel.session.ReleaseResourceRQ;
import com.fleurida.camel.session.ReleaseResourceRS;
import com.fleurida.camel.session.SessionResourceRoute;

public class SessionResourceManagerFailureTest extends
		CamelBlueprintTestSupport {

	private boolean debugBeforeMethodCalled;
	private boolean debugAfterMethodCalled;

	// override this method, and return the location of our Blueprint XML file
	// to be used for testing ... note, if multiple files contain a
	// camelContext, the last file wins
	@Override
	protected String getBlueprintDescriptor() {
		return "/OSGI-INF/blueprint/blueprint.xml";
	}

	@Override
	protected boolean includeTestBundle() {
		return true;
	}

	@Override
	public void setUp() throws Exception {

		super.setUp();

		// wait until route is available cause we are testing the real route
		// from the production blueprint and this needs time to start
		new NotifyBuilder(context()).whenDone(1).create();

	}

	@Test
	public void testTransactionRollback() throws Exception {

		try {

			Map<String, Object> headers = new HashMap<String, Object>();
			// for testing purporses only
			// need to set this for the dynamic router to piece together the
			// target
			headers.put(DynamicSourceRouter.SOURCE_ADAPTER, "ADAPTER-ONE");

			// put on the actual route
			@SuppressWarnings("unused")
			ReleaseResourceRS rs = template.requestBodyAndHeaders(
					SessionResourceRoute.DIRECT_START, new ReleaseResourceRQ()
							.withVersion("1.0").withSayWhat("Fail"), headers,
					ReleaseResourceRS.class);

			assertTrue("Never exepect to get to here", false);

		} catch (CamelExecutionException ex) {

			log.info("Expected failure. Message was {}", ex.getCause()
					.getMessage());

		} catch (Throwable e) {
			assertTrue("Never exepct to catch " + e.getClass().getName(), false);

		}

		// assert on the debugBefore/debugAfter methods below being called as
		// we've enabled the debugger
		assertTrue(debugBeforeMethodCalled);
		assertTrue(debugAfterMethodCalled);

	}

	@Override
	protected int getShutdownTimeout() {
		return super.getShutdownTimeout() + 60;
	}

	@Override
	public boolean isUseDebugger() {
		// must enable debugger
		return true;
	}

	@Override
	protected void debugBefore(Exchange exchange,
			org.apache.camel.Processor processor,
			ProcessorDefinition<?> definition, String id, String label) {
		log.info("Before " + definition + " with body "
				+ exchange.getIn().getBody());
		debugBeforeMethodCalled = true;
	}

	@Override
	protected void debugAfter(Exchange exchange,
			org.apache.camel.Processor processor,
			ProcessorDefinition<?> definition, String id, String label,
			long timeTaken) {
		log.info("After " + definition + " with body "
				+ exchange.getIn().getBody());
		debugAfterMethodCalled = true;
	}

}
