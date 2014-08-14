package com.fleurida.camel.dyn;

import org.junit.Test;
import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

public class TransactedRouteCascadeFailureTest extends
		CamelBlueprintTestSupport {

	private static String UT_TRANSACTED_MASTER_ROUTE = "direct:unit-test-master-route";

	private static String UT_TRANSACTED_NESTED_ROUTE = "direct:unit-test-nested-route";

	private static String REQUEST_SUCCESS = "complete nicely";
	private static String REQUEST_FAIL_MASTER = "fail master";
	private static String REQUEST_FAIL_NESTED = "fail nested";

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

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from(UT_TRANSACTED_MASTER_ROUTE)
						.routeId(UT_TRANSACTED_MASTER_ROUTE)
						// is transacted with the test tm
						.transacted()
						// actual route
						.choice()
						.when(body().isEqualTo(REQUEST_FAIL_MASTER))
						.throwException(
								new RuntimeException("Failed on "
										+ UT_TRANSACTED_MASTER_ROUTE))
						.otherwise()
						// pass on to nesting level 1
						.to(UT_TRANSACTED_NESTED_ROUTE).end()
						// mock for verification
						.to("mock:d").end();

				from(UT_TRANSACTED_NESTED_ROUTE)
						.routeId(UT_TRANSACTED_NESTED_ROUTE)
						.transacted()
						.choice()
						.when(body().isEqualTo(REQUEST_FAIL_NESTED))
						.throwException(
								new RuntimeException("Failed on "
										+ UT_TRANSACTED_NESTED_ROUTE))
						.otherwise().end();

			}

		};

	}

	@Test
	public void testTransactionRollback() throws Exception {

		// set mock expectations
		getMockEndpoint("mock:d").expectedMessageCount(1);

		try {
			// send a message
			template.sendBody(UT_TRANSACTED_MASTER_ROUTE, REQUEST_FAIL_NESTED);

		} catch (RuntimeException e) {
			log.warn("test catch", e);
		}

		MockEndpoint mep = getMockEndpoint("mock:d");

		// assert mocks
		assertMockEndpointsSatisfied();

		Exchange exch = mep.getExchanges().get(0);
		String response = exch.getIn().getBody(String.class);

		assertNotNull(response);

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
