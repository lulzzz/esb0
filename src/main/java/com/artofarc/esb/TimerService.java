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
package com.artofarc.esb;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.artofarc.esb.context.GlobalContext;
import com.artofarc.esb.context.WorkerPoolThreadFactory;
import com.artofarc.esb.message.BodyType;
import com.artofarc.esb.message.ESBMessage;

public class TimerService extends ConsumerPort {

	private final String _workerPool;
	private final int _initialDelay, _period;

	private ScheduledExecutorService _scheduledExecutorService;

	private volatile TimerTask _timerTask;
	private volatile ScheduledFuture<?> _future;

	public TimerService(String uri, String workerPool, int initialDelay, int period) {
		super(uri);
		_workerPool = workerPool;
		_initialDelay = initialDelay;
		_period = period;
	}

	public void init(GlobalContext globalContext) {
		_scheduledExecutorService = globalContext.getWorkerPool(_workerPool).getScheduledExecutorService();
		if (super.isEnabled()) {
			enable(true);
		}
	}

	@Override
	public boolean isEnabled() {
		return _timerTask != null;
	}

	@Override
	public void enable(boolean enable) {
		if (enable) {
			if (_timerTask == null) {
				_timerTask = new TimerTask() {

					@Override
					public void run() {
						ESBMessage message = new ESBMessage(BodyType.INVALID, null);
						try {
							process(WorkerPoolThreadFactory.getContext(), message);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Exception in forked action pipeline", e);
						}
					}
				};
				_future = _scheduledExecutorService.scheduleAtFixedRate(_timerTask, _initialDelay, _period, TimeUnit.SECONDS);
			}
		} else {
			stop();
		}
	}

	public void stop() {
		if (_future != null) {
			_future.cancel(false);
			_future = null;
		}
		if (_timerTask != null) {
			_timerTask.cancel();
			_timerTask = null;
		}
	}

}
