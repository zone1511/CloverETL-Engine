/*
 *  jETeL/Clover - Java based ETL application framework.
 *  Created on Apr 26, 2003
 *  Copyright (C) 2003, 2002  David Pavlis, Wes Maciorowski
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jetel.data;

import org.jetel.exception.JetelException;
import org.jetel.metadata.DataRecordMetadata;

/**
 * @author maciorowski
 *
 */
public class SQLDataParser implements DataParser {

	/* (non-Javadoc)
	 * @see org.jetel.data.DataParser#getNext()
	 */
	public DataRecord getNext() throws JetelException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jetel.data.DataParser#open(java.lang.Object, org.jetel.metadata.DataRecordMetadata)
	 */
	public void open(Object inputDataSource, DataRecordMetadata _metadata) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jetel.data.DataParser#close()
	 */
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
