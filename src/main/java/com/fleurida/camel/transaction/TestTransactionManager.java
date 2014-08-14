package com.fleurida.camel.transaction;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.fleurida.camel.session.ReleaseResourceRQ;
import com.fleurida.camel.session.SessionResource;

public class TestTransactionManager extends AbstractPlatformTransactionManager
		implements InitializingBean, TransactionManager {

	private static final long serialVersionUID = 1L;

	protected final transient Logger log = LoggerFactory.getLogger(getClass());

	public List<TransactionObject> issuedTransactionObjects = new ArrayList<TransactionObject>();

	public List<TransactionObject> releasedTransactionObjects = new ArrayList<TransactionObject>();

	private SessionResource sessionResource;

	public SessionResource getSessionResource() {
		return sessionResource;
	}

	public void setSessionResource(SessionResource sessionResource) {
		this.sessionResource = sessionResource;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	protected Object doGetTransaction() throws TransactionException {

		TransactionObject txo = new TransactionObject();
		issuedTransactionObjects.add(txo);
		log.info("Get {}", txo);
		return txo;

	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition)
			throws TransactionException {

		TransactionObject txo = (TransactionObject) transaction;
		log.info("Begin {}", txo);

	}

	@Override
	protected void doCommit(DefaultTransactionStatus status)
			throws TransactionException {

		TransactionObject txo = (TransactionObject) status.getTransaction();
		log.info("Commit {}", txo);

	}

	@Override
	protected void doRollback(DefaultTransactionStatus status)
			throws TransactionException {

		TransactionObject txo = (TransactionObject) status.getTransaction();
		log.warn("Rollback {}", txo);
		try {

			ReleaseResourceRQ rq = new ReleaseResourceRQ().withSayWhat(txo
					.getId());

			sessionResource.release(rq);

		} catch (Throwable ignore) {
			log.info("Ignored sm exception: {}", ignore.getMessage());
		}

	}

}
