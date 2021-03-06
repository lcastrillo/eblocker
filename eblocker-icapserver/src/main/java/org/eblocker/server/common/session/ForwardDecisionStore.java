/*
 * Copyright 2020 eBlocker Open Source UG (haftungsbeschraenkt)
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the EUPL
 * (the "License"); You may not use this work except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 *   https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.eblocker.server.common.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eblocker.server.common.transaction.Decision;

public class ForwardDecisionStore {

	private Map<String, Decision> store;
	
	public Decision popDecision(String url) {
		Decision decision = Decision.NO_DECISION;
		synchronized(this) {
			if (store != null) {
				if (store.containsKey(url)) {
					decision = store.remove(url);
				}
			}
		}
		return decision;
	}
	
	public void addDecision(String url, Decision decision) {
		synchronized(this) {
			if (store == null) {
				store = new ConcurrentHashMap<>();
			}
			store.put(url, decision);
		}
	}

}
