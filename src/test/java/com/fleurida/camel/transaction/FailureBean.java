package com.fleurida.camel.transaction;

import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailureBean {

	protected final transient Logger log = LoggerFactory.getLogger(getClass());

	String path;

	public FailureBean(String path) {
		this.path = path;
	}

	public String release(@Body TransactionObject txo) {

		log.warn("Fabricate a failure on {} for {}", path, txo);
		throw new RuntimeException("Failed on " + path + " for " + txo);

	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " on " + path;
	}
}
