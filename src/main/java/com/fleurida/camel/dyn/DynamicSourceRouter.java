package com.fleurida.camel.dyn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.Body;
import org.apache.camel.DynamicRouter;
import org.apache.camel.Exchange;
import org.apache.camel.Header;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DynamicSourceRouter {

	private static final Logger log = LoggerFactory
			.getLogger(DynamicSourceRouter.class);

	public static final String SOURCE_ADAPTER = "DynamicRouterSourceAdapter";

	private String endpointPrefix = "direct-vm";

	@DynamicRouter
	public String route(
			@Header(DynamicSourceRouter.SOURCE_ADAPTER) String adapterName,
			@Header(Exchange.SLIP_ENDPOINT) String slip, @Body Document body) {

		// return as soon as we have advanced in the slip
		if (slip != null) {
			return null;
		}

		// determine request type (message payload
		// TODO also need to do version number in the future

		Element root = body.getDocumentElement();
		// need to make sure to grab only the local name
		String rqType = root.getLocalName();

		String version = root.getAttribute("version");
		if (version == null || "".equals(version.trim())) {
			log.warn("No request version found on {}, defaulting to 1.0",
					rqType);
			version = "1.0";
		}

		if (log.isDebugEnabled()) {
			log.debug("Routing to {}:{}/{}-{}", endpointPrefix, adapterName,
					rqType, version);
		}

		return String.format("%s:%s/%s-%s", endpointPrefix, adapterName,
				rqType, version);

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
