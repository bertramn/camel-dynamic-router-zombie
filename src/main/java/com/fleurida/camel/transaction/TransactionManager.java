package com.fleurida.camel.transaction;

/**
 * To make it easier to expose the spring based transaction manager in OSGi with
 * specific interface.
 * 
 */
public interface TransactionManager extends
		org.springframework.transaction.PlatformTransactionManager {

}
