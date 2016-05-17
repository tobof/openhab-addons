/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
 * 
 * @author Robert Bausdorf
 * 
 *
 */
@XmlRootElement(name = "switch")
@XmlType(propOrder = { "state", "mode", "lock"})
public class SwitchModel {
	public static final BigDecimal ON = new BigDecimal("1");
	public static final BigDecimal OFF = new BigDecimal("0");

	private BigDecimal state;
	private String mode;
	private BigDecimal lock;

	public BigDecimal getState() {
		return state;
	}
	
	public void setState(BigDecimal state) {
		this.state = state;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public BigDecimal getLock() {
		return lock;
	}
	
	public void setLock(BigDecimal lock) {
		this.lock = lock;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("state", this.getState())
			.append("mode", this.getMode())
			.append("lock", this.getLock())
			.toString();
	}
}
