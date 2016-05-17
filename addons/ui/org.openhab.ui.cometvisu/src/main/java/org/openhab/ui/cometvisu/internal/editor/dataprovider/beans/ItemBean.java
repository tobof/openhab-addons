/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.editor.dataprovider.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ItemBean} is a helper bean used by the dataprovider-servlet
 * which delivers some additional data for the CometVisu-Editor
 * 
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class ItemBean extends DataBean {
    public Map<String, String> hints = new HashMap<String, String>();
}
