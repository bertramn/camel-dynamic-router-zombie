package com.fleurida.camel.transaction;

import java.util.UUID;

public class TransactionObject {

	String id = UUID.randomUUID().toString();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null && obj instanceof TransactionObject && (this.id
				.equals(((TransactionObject) obj).getId())));
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), id);
	}

}