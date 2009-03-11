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
package org.jetel.component;

import org.apache.commons.logging.Log;
import org.jetel.exception.JetelException;
import org.jetel.graph.TransformationGraph;
import org.jetel.interpreter.data.TLValue;

/**
 *  
 *
 * @author      dpavlis
 * @since       June 25, 2006
 * @revision    $Revision: $
 * @created     June 25, 2006
 * @see         org.jetel.component.RecordTransform
 */
public class RecordTransformCommonTL {

    public static final String FINISHED_FUNCTION_NAME="finished";
    public static final String INIT_FUNCTION_NAME="init";
    public static final String RESET_FUNCTION_NAME="reset";
    
    protected TransformationGraph graph;
    protected Log logger;

    protected String errorMessage;
	 
	protected WrapperTL wrapper;
	protected TLValue semiResult;

    /**Constructor for the DataRecordTransform object */
    public RecordTransformCommonTL(String srcCode, Log logger) {
        this.logger=logger;
        wrapper = new WrapperTL(srcCode, logger);
    }

	/**
	 *  Returns description of error if one of the methods failed
	 *
	 * @return    Error message
	 * @since     April 18, 2002
	 */
	public String getMessage() {
		return errorMessage;
	}
	
	/* (non-Javadoc)
	 * @see org.jetel.component.RecordTransform#signal()
	 * In this implementation does nothing.
	 */
	public void signal(Object signalObject){
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.jetel.component.RecordTransform#getSemiResult()
	 */
	public Object getSemiResult(){
		return semiResult;
	}
	
	
	/* (non-Javadoc)
	 * @see org.jetel.component.RecordTransform#finished()
	 */
	public void finished(){
        // execute finished transformFunction
		semiResult = null;
		try {
			semiResult = wrapper.execute(FINISHED_FUNCTION_NAME,null);
		} catch (JetelException e) {
			//do nothing: function finished is not necessary
		}
	}
	
    /* (non-Javadoc)
     * @see org.jetel.component.RecordTransform#setGraph(org.jetel.graph.TransformationGraph)
     */
    public void setGraph(TransformationGraph graph) {
        this.graph = graph;
    }

    /* (non-Javadoc)
     * @see org.jetel.component.RecordTransform#getGraph()
     */
    public TransformationGraph getGraph() {
        return graph;
    }

    /*
     * (non-Javadoc)
     * @see org.jetel.component.RecordTransform#reset()
     */
	public void reset() {
        // execute reset transformFunction
		semiResult = null;
		try {
			semiResult = wrapper.execute(RESET_FUNCTION_NAME,null);
		} catch (JetelException e) {
			//do nothing: function reset is not necessary
		}
	}
}

