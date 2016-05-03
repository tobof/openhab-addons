/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.rrs.beans;

/**
 * {@link Entry} is used by the CometVisu rss-plugin
 * 
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class Entry {
    public String id;
    public String title;
    public String content;
    public String tags;
    public String state;
    public long publishedDate;
}
