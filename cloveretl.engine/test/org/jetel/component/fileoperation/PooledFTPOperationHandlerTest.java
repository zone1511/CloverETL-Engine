/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.component.fileoperation;


public class PooledFTPOperationHandlerTest extends FTPOperationHandlerTest {
	
	protected PooledFTPOperationHandler handler = null;
	
	@Override
	protected IOperationHandler createOperationHandler() {
		return handler = new PooledFTPOperationHandler();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
	}

	@Override
	public void testGetPriority() {
//		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.copy(FTPOperationHandler.FTP_SCHEME, FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.move(FTPOperationHandler.FTP_SCHEME, FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.delete(FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.create(FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.resolve(FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.info(FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.list(FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.read(FTPOperationHandler.FTP_SCHEME)));
		assertEquals(IOperationHandler.TOP_PRIORITY, handler.getPriority(Operation.write(FTPOperationHandler.FTP_SCHEME)));
	}

	@Override
	public void testCanPerform() {
//		assertTrue(handler.canPerform(Operation.copy(FTPOperationHandler.FTP_SCHEME, FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.move(FTPOperationHandler.FTP_SCHEME, FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.delete(FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.create(FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.resolve(FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.info(FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.list(FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.read(FTPOperationHandler.FTP_SCHEME)));
		assertTrue(handler.canPerform(Operation.write(FTPOperationHandler.FTP_SCHEME)));
	}

}