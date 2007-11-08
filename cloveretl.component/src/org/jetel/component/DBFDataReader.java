/*
*    jETeL/Clover.ETL - Java based ETL application framework.
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
import org.apache.commons.logging.LogFactory;
import org.jetel.data.DataRecord;
import org.jetel.database.dbf.DBFDataParser;
import org.jetel.exception.BadDataFormatException;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.IParserExceptionHandler;
import org.jetel.exception.ParserExceptionHandlerFactory;
import org.jetel.exception.PolicyType;
import org.jetel.exception.XMLConfigurationException;
import org.jetel.graph.Node;
import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.util.MultiFileReader;
import org.jetel.util.SynchronizeUtils;
import org.jetel.util.property.ComponentXMLAttributes;
import org.w3c.dom.Element;

/**
 *  <h3>dBase Table/Data Reader Component</h3>
 *
 * <!-- Parses specified input data file (in form of dBase table) and broadcasts the records to all connected out ports -->
 *
 * <table border="1">
 *  <th>Component:</th>
 * <tr><td><h4><i>Name:</i></h4></td>
 * <td>DBFDataReader</td></tr>
 * <tr><td><h4><i>Category:</i></h4></td>
 * <td></td></tr>
 * <tr><td><h4><i>Description:</i></h4></td>
 * <td>Reads records from specified dBase data file and broadcasts the records to all connected out ports.</td></tr>
 * <tr><td><h4><i>Inputs:</i></h4></td>
 * <td></td></tr>
 * <tr><td><h4><i>Outputs:</i></h4></td>
 * <td>At least one output port defined/connected.</td></tr>
 * <tr><td><h4><i>Comment:</i></h4></td>
 * <td>This component needs metadata specified as fix-length - type="fixed"<br>
 * Also, first field in metadata must be String field with length 1 which is used as
 * indicator of deleted records in DBF.<br>
 * Such metadata can be automatically generated by Clover's utility <code>DBFAnalyzer</code>. Its main
 * class can be executed as <code>java -cp "clover.engine.jar" org.jetel.database.dbf.DBFAnalyzer</code><br>
 * <i>Note: DBFAnalyzer generates additional information from DBF file (<code>dataOffset</code> and <code>recordSize</code>), but these are
 * not neccessary.</i></td></tr>
 * </table>
 *  <br>
 *  <table border="1">
 *  <th>XML attributes:</th>
 *  <tr><td><b>type</b></td><td>"DBF_DATA_READER"</td></tr>
 *  <tr><td><b>id</b></td><td>component identification</td>
 *  <tr><td><b>fileURL</b></td><td>path to the input table file</td>
 *  <tr><td><b>dataPolicy</b><br><i>optional</i></td><td>specifies how to handle misformatted or incorrect data.  'Strict' (default value) aborts processing, 'Controlled' logs the entire record while processing continues, and 'Lenient' attempts to set incorrect data to default values while processing continues.</td>
 *  <tr><td><b>charset</b><br><i>optional</i></td><td>Which character set to use for decoding field's data.  Default value is deduced from DBF table header. If it is
 *  specified as part of metadata at record level, then it is take from there.</td>
 *  <tr><td><b>skipRows</b><br><i>optional</i></td><td>specifies how many records/rows should be skipped from the source file. Good for handling files where first rows is a header not a real data. Dafault is 0.</td>
 *  <tr><td><b>numRecords</b><br><i>optional</i></td><td>max number of parsed records</td>
 *  </tr>
 *  </table>
 *
 *  <h4>Example:</h4>
 *  <pre>&lt;Node type="DBF_DATA_READER" id="InputFile" fileURL="/tmp/customers.dbf" /&gt;</pre>
 *  <h5>Example metadata:</h5>
 *  <pre>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 * &lt;Record charset=&quot;windows-1252&quot; dataOffset=&quot;456&quot; name=&quot;feature_dbf&quot; recordSize=&quot;35&quot; type=&quot;fixed&quot;&gt;
 *       &lt;Field name=&quot;_IS_DELETED_&quot; nullable=&quot;true&quot; size=&quot;1&quot; type=&quot;string&quot;/&gt;
 *       &lt;Field name=&quot;INDEX&quot; nullable=&quot;true&quot; size=&quot;7&quot; type=&quot;numeric&quot;/&gt;
 *       &lt;Field name=&quot;TYPE&quot; nullable=&quot;true&quot; size=&quot;9&quot; type=&quot;string&quot;/&gt;
 *       &lt;Field name=&quot;NAME&quot; nullable=&quot;true&quot; size=&quot;11&quot; type=&quot;numeric&quot;/&gt;
 *       &lt;Field name=&quot;STREET&quot; nullable=&quot;true&quot; size=&quot;1&quot; type=&quot;string&quot;/&gt;
 *       &lt;Field name=&quot;CITY&quot; nullable=&quot;true&quot; size=&quot;6&quot; type=&quot;numeric&quot;/&gt;
 * &lt;/Record&gt;
 *  </pre>
 *
 * @author      dpavlis
 * @since       June 28, 2004
 * @revision    $Revision$
 * @see         org.jetel.database.dbf.DBFDataParser
 */

public class DBFDataReader extends Node {

	private static final String XML_DATAPOLICY_ATTRIBUTE = "dataPolicy";
	private static final String XML_FILEURL_ATTRIBUTE = "fileURL";
	private static final String XML_CHARSET_ATTRIBUTE = "charset";
    private static final String XML_RECORD_SKIP_ATTRIBUTE = "skipRows";
    private static final String XML_NUMRECORDS_ATTRIBUTE = "numRecords";
	/**  Description of the Field */
	public final static String COMPONENT_TYPE = "DBF_DATA_READER";

	private static Log logger = LogFactory.getLog(DBFDataReader.class);

	private final static int OUTPUT_PORT = 0;
    private MultiFileReader reader;
    private PolicyType policyType;
	private String fileURL;
    private int skipRows=0; // do not skip rows by default
    private int numRecords = -1;
	
	private DBFDataParser parser;


	/**
	 *Constructor for the DBFDataReader object
	 *
	 * @param  id       Description of the Parameter
	 * @param  fileURL  Description of the Parameter
	 */
	public DBFDataReader(String id, String fileURL) {
		super(id);
		this.fileURL = fileURL;
		parser = new DBFDataParser();
	}


	/**
	 *Constructor for the DBFDataReader object
	 *
	 * @param  id       Description of the Parameter
	 * @param  fileURL  Description of the Parameter
	 * @param  charset  Description of the Parameter
	 */
	public DBFDataReader(String id, String fileURL, String charset) {
		super(id);
		this.fileURL = fileURL;
		parser = new DBFDataParser(charset);
	}

	@Override
	public Result execute() throws Exception {
		// we need to create data record - take the metadata from first output port
		DataRecord record = new DataRecord(getOutputPort(OUTPUT_PORT).getMetadata());
		record.init();

		// till it reaches end of data or it is stopped from outside
		try {
			while (record != null && runIt) {
			    try {
			        if((record = reader.getNext(record)) != null) {
			            //broadcast the record to all connected Edges
			            writeRecordBroadcast(record);
			        }
			    } catch(BadDataFormatException bdfe) {
			        if(policyType == PolicyType.STRICT) {
			            throw bdfe;
			        } else {
			            logger.info(bdfe.getMessage());
			        }
			    }
			    SynchronizeUtils.cloverYield();
			}
		} catch (Exception e) {
			throw e;
		}finally{
			reader.close();
			broadcastEOF();
		}
        return runIt ? Result.FINISHED_OK : Result.ABORTED;
	}

	
	/**
	 *  Description of the Method
	 *
	 * @exception  ComponentNotReadyException  Description of the Exception
	 * @since                                  April 4, 2002
	 */
	public void init() throws ComponentNotReadyException {
		super.init();

        // initialize multifile reader based on prepared parser
        reader = new MultiFileReader(parser, getGraph() != null ? getGraph().getRuntimeParameters().getProjectURL() : null, fileURL);
        reader.setLogger(logger);
        reader.setSkip(skipRows);
        reader.setNumRecords(numRecords);
        reader.init(getOutputPort(OUTPUT_PORT).getMetadata());
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Returned Value
	 * @since     May 21, 2002
	 */
	@Override public void toXML(Element xmlElement) {
		super.toXML(xmlElement);
		
		String charSet = this.parser.getCharset();
		if (charSet != null) {
			xmlElement.setAttribute(XML_CHARSET_ATTRIBUTE, this.parser.getCharset());
		}
		PolicyType policyType = this.parser.getPolicyType();
		if (policyType != null) {
			xmlElement.setAttribute(XML_DATAPOLICY_ATTRIBUTE, policyType.toString());
		}
		xmlElement.setAttribute(XML_FILEURL_ATTRIBUTE, this.fileURL);
		if (skipRows != 0){
			xmlElement.setAttribute(XML_RECORD_SKIP_ATTRIBUTE, String.valueOf(skipRows));
		}
		if (numRecords != 0){
			xmlElement.setAttribute(XML_NUMRECORDS_ATTRIBUTE,String.valueOf(numRecords));
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nodeXML  Description of Parameter
	 * @return          Description of the Returned Value
	 * @since           May 21, 2002
	 */
    public static Node fromXML(TransformationGraph graph, Element xmlElement) throws XMLConfigurationException {
		DBFDataReader dbfDataReader = null;
		ComponentXMLAttributes xattribs = new ComponentXMLAttributes(xmlElement, graph);
		
		try {
			if (xattribs.exists(XML_CHARSET_ATTRIBUTE)) {
				dbfDataReader = new DBFDataReader(xattribs.getString(XML_ID_ATTRIBUTE),
						xattribs.getString(XML_FILEURL_ATTRIBUTE),
						xattribs.getString(XML_CHARSET_ATTRIBUTE));
			} else {
				dbfDataReader = new DBFDataReader(xattribs.getString(XML_ID_ATTRIBUTE),
						xattribs.getString(XML_FILEURL_ATTRIBUTE));
			}
			if (xattribs.exists(XML_DATAPOLICY_ATTRIBUTE)) {
				dbfDataReader.setExceptionHandler(ParserExceptionHandlerFactory.getHandler(
					xattribs.getString(XML_DATAPOLICY_ATTRIBUTE)));
			}
            if (xattribs.exists(XML_RECORD_SKIP_ATTRIBUTE)){
            	dbfDataReader.setSkipRows(xattribs.getInteger(XML_RECORD_SKIP_ATTRIBUTE));
            }
            if (xattribs.exists(XML_NUMRECORDS_ATTRIBUTE)){
            	dbfDataReader.setNumRecords(xattribs.getInteger(XML_NUMRECORDS_ATTRIBUTE));
            }
			
        } catch (Exception ex) {
            throw new XMLConfigurationException(COMPONENT_TYPE + ":" + xattribs.getString(XML_ID_ATTRIBUTE," unknown ID ") + ":" + ex.getMessage(),ex);
        }

		return dbfDataReader;
	}


	/**
	

	/**
	 * Adds BadDataFormatExceptionHandler to behave according to DataPolicy.
	 *
	 * @param  handler
	 */
	private void setExceptionHandler(IParserExceptionHandler handler) {
		parser.setExceptionHandler(handler);
	}


	/**  Description of the Method */
    @Override
    public ConfigurationStatus checkConfig(ConfigurationStatus status) {
        super.checkConfig(status);
        
        checkInputPorts(status, 0, 0);
        checkOutputPorts(status, 1, Integer.MAX_VALUE);
        checkMetadata(status, getOutMetadata());

        /* try { // because of stdin
            init();
            free();
        } catch (ComponentNotReadyException e) {
            ConfigurationProblem problem = new ConfigurationProblem(e.getMessage(), ConfigurationStatus.Severity.ERROR, this, ConfigurationStatus.Priority.NORMAL);
            if(!StringUtils.isEmpty(e.getAttributeName())) {
                problem.setAttributeName(e.getAttributeName());
            }
            status.add(problem);
        }*/
        
        return status;
    }
	
	public String getType(){
		return COMPONENT_TYPE;
	}

    /**
     * @param skipRows The skipRows to set.
     */
    public void setSkipRows(int skipRows) {
        this.skipRows = skipRows;
    }
    
    public void setNumRecords(int numRecords) {
        this.numRecords = Math.max(numRecords, 0);
    }

    
    public void setPolicyType(String strPolicyType) {
        setPolicyType(PolicyType.valueOfIgnoreCase(strPolicyType));
    }

	/**
	 * Adds BadDataFormatExceptionHandler to behave according to DataPolicy.
	 *
	 * @param  handler
	 */
	public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
        parser.setExceptionHandler(ParserExceptionHandlerFactory.getHandler(policyType));
	}

    
}

