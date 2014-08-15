package com.fleurida.camel.dyn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.DynamicRouter;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.language.XPath;

public class DynamicSourceRouter {

	private static final Logger log = LoggerFactory
			.getLogger(DynamicSourceRouter.class);

	public static final String SOURCE_ADAPTER = "DynamicRouterSourceAdapter";

	private String endpointPrefix = "direct-vm";

	/**
	 * A dynamic router that can be used to send a platform complient
	 * 
	 * @param previous
	 *            the privious routing slip that is used to work out when to
	 *            stop routing. If the previous routing slip is set to some
	 *            value, this router will stop the routing
	 * @param sourceAdapter
	 *            the name of the platform adapter to route to
	 * @param messageType
	 *            the name of the root element node
	 * @param messageVersion
	 *            the version of the root element, aka message version
	 * @param exchange
	 *            the exchange that is to be routed in case we need anything
	 *            from it
	 * 
	 * @return the dynamically constructed endpoint to route to
	 */
	@DynamicRouter
	public String route(
			@Header(Exchange.SLIP_ENDPOINT) String previous,
			@Header(DynamicSourceRouter.SOURCE_ADAPTER) String sourceAdapter,
			@XPath(value = "/child::node()/local-name()", resultType = String.class) String messageType,
			@XPath(value = "/child::node()/@version", resultType = String.class) String messageVersion,
			Exchange exchange) {

		// return as soon as we have advanced in the slip
		if (previous != null) {
			return null;
		}

		if (messageType == null) {
			// throw configuration error
			throw new IllegalArgumentException(
					"cannot determine message type from routed message");

		}

		if (messageVersion == null || "".equals(messageVersion.trim())) {
			log.warn("No request version found on {}, defaulting to 1.0",
					messageType);
			messageVersion = "1.0";
		}

		if (log.isDebugEnabled()) {
			log.debug("Routing to {}:{}/{}-{}", endpointPrefix, sourceAdapter,
					messageType, messageVersion);
		}

		return String.format("%s:%s/%s-%s", endpointPrefix, sourceAdapter,
				messageType, messageVersion);

	}

	public static DynamicSourceRouter build() {
		DynamicSourceRouter router = new DynamicSourceRouter();
		return router;
	}

	public static DynamicSourceRouter build(String prefix) {
		return new DynamicSourceRouter().withEndpointPrefix(prefix);
	}

	public DynamicSourceRouter withEndpointPrefix(String prefix) {
		this.endpointPrefix = prefix;
		return this;
	}

}
