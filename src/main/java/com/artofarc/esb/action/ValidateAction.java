/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artofarc.esb.action;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.xml.validation.Schema;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQResultSequence;

import com.artofarc.esb.context.Context;

public class ValidateAction extends AssignAction {

	private final Schema _schema;

	public ValidateAction(Schema schema, String expression, Collection<Map.Entry<String, String>> namespaces) {
		super(null, expression, namespaces, Collections.<String> emptyList());
		_schema = schema;
	}

	@Override
	protected void processSequence(Context context, XQResultSequence resultSequence, Map<String, Object> destMap) throws Exception {
		if (!resultSequence.next()) {
			throw new ExecutionException(this, "Expression had no result");
		}
		try (AutoCloseable timer = context.getTimeGauge().createTimer("validator.validate")) {
//			XQItem xqItem = resultSequence.getItem();
//			xqItem.writeItem(System.out, null);
//			xqItem.writeItemToSAX(_schema.newValidatorHandler());
			resultSequence.writeItemToSAX(_schema.newValidatorHandler());
		} catch (XQException e) {
			throw new ExecutionException(this, "Validation failed", e.getCause());
		}
	}

}
