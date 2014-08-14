package com.fleurida.camel.dyn;

import org.junit.Test;
import org.apache.camel.BeanInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

import com.fleurida.camel.session.ReleaseResourceRQ;
import com.fleurida.camel.session.SessionResourceManager;

public class SessionResourceManagerFailureTest extends
		CamelBlueprintTestSupport {

	private boolean debugBeforeMethodCalled;
	private boolean debugAfterMethodCalled;

	@BeanInject
	SessionResourceManager sm;

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

		sm.release(new ReleaseResourceRQ().withVersion("1.0").withSayWhat(
				"Fail"));
		

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
