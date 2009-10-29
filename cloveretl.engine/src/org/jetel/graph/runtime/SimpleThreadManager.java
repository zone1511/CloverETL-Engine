/*
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2002-04  David Pavlis <david_pavlis@hotmail.com>
*    
*    This library is free software; you can redistribute it and/or
*    modify it under the terms of the GNU Lesser General Public
*    License as published by the Free Software Foundation; either
*    version 2.1 of the License, or (at your option) any later version.
*    
*    This library is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    
*    Lesser General Public License for more details.
*    
*    You should have received a copy of the GNU Lesser General Public
*    License along with this library; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package org.jetel.graph.runtime;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.jetel.graph.Result;

/**
 * @author Martin Zatopek (martin.zatopek@javlinconsulting.cz)
 *         (c) Javlin Consulting (www.javlinconsulting.cz)
 *
 * @created 27.2.2008
 */
public class SimpleThreadManager implements IThreadManager {

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.IThreadManager#executeWatchDog(org.jetel.graph.runtime.WatchDog)
	 */
	public Future<Result> executeWatchDog(WatchDog watchDog) {
		watchDog.setThreadManager(this);
		watchDog.init();
		
		FutureTask<Result> futureTask = new FutureTask<Result>(watchDog); 
		Thread watchdogThread = new Thread(futureTask, "WatchDog");
		watchdogThread.start();
		
		return futureTask;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.IThreadManager#executeNode(java.lang.Runnable)
	 */
	public void executeNode(Runnable node) {
		Thread nodeThread = new Thread(node);
		nodeThread.setContextClassLoader(node.getClass().getClassLoader());
		nodeThread.setPriority(Thread.MIN_PRIORITY);
		nodeThread.setDaemon(true);
		nodeThread.start();
	}


	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.IThreadManager#getFreeThreadsCount()
	 */
	public int getFreeThreadsCount() {
		return Integer.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.IThreadManager#releaseNodeThreads(int)
	 */
	public void releaseNodeThreads(int nodeThreadsToRelease) {
		// DO NOTHING
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.IThreadManager#free()
	 */
	public void free() {
		// DO NOTHING
	}

	/* (non-Javadoc)
	 * @see org.jetel.graph.runtime.IThreadManager#freeNow()
	 */
	public void freeNow() {
		// DO NOTHING
	}

}
