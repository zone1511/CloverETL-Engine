/* 
*    This file is part of jETeL/Clover - Java based ETL 
*    application framework, Copyright (C) 2002-06  
*    David Pavlis <david_pavlis@hotmail.com>
*    
*    This library is free software; you can redistribute it and/or
*    modify it under the terms of the GNU Lesser General Public
*    License as published by the Free Software Foundation
*    version 2.1 of the License.
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
*    Copyright (C) 2006 
*    Linagora <http://linagora.com>
*    Francois Armand <farmand@linagora.com>
*/

package com.linagora.component;


import java.io.IOException;

import javax.naming.directory.SearchControls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.XMLConfigurationException;
import org.jetel.graph.Node;
import org.jetel.graph.TransformationGraph;
import org.jetel.util.ComponentXMLAttributes;
import org.w3c.dom.Element;

import com.linagora.ldap.LdapParser;

/**
 * <h3>LdapReader Component</h3>
 *
 * <!-- This class is intended to provide a mean to read information 
 * from an LDAP directory. -->
 *
 * <table border="1">
 *  <th>Component:</th>
 * <tr><td><h4><i>Name:</i></h4></td>
 * <td>LdapReader</td></tr>
 * <tr><td><h4><i>Category:</i></h4></td>
 * <td></td></tr>
 * <tr><td><h4><i>Description:</i></h4></td>
 * <td>Provides the logic to extract search result of an LDAP directory
 * and transform them into Jetel Data Records.
 * <br>The metadata provided throuh output port/edge must precisely 
 * describe the structure of read object.</td></tr>
 * <tr><td><h4><i>Inputs:</i></h4></td>
 * <td>0</td></tr>
 * <tr><td><h4><i>Outputs:</i></h4></td>
 * <td>[0]- output records</td></tr>
 * <tr><td><h4><i>Comment:</i></h4></td>
 * <td>Results of the search must have the same objectClass</td></tr>
 * </table>
 *  <br>
 *  
 *  <table border="1">
 *  <th>XML attributes:</th>
 *  <tr><td><b>type</b></td><td>"LDAP_READER"</td></tr>
 *  <tr><td><b>id</b></td><td>component identification</td>
 *  <tr><td><b>ldapUrl</b><td>Ldap url of the directory, on the form "ldap://host:port/"</td>
 *  <tr><td><b>base</b></td><td>Base DN used for the LDAP search</td>
 *  <tr><td><b>filter</b></td><td>Filter used for the LDAP connection</td>
 *  <tr><td><b>scope</b><br></td>Scope of the search request, must be one of <b>object</b>, <b>onelevel</b> or <b>subtree</b><td></td>
 *  <tr><td><b>user</b><br><i>optional</i></td>The user DN to used when connecting to directory.<td></td>
 *  <tr><td><b>password</b><br><i>optional</i></td>The password to used when connecting to directory.<td></td>
 * <!-- to be added <tr><td><b>DataPolicy</b></td><td>specifies how to handle misformatted or incorrect data.  'Strict' (default value) aborts processing, 'Controlled' logs the entire record while processing continues, and 'Lenient' attempts to set incorrect data to default values while processing continues.</td></tr> -->
 *  </table>
 * 
 * <br>Note:
 * 
 * <h4>Example (public LDAP):</h4>
 * <pre>&lt;id="INPUT1" type="LDAP_READER" ldapUrl="ldap://ldap.uninett.no:389/" base="ou=people,dc=uninett,dc=no" filter="uid=*" scope="SUBTREE"&gt;</pre>
 *
 * <h4>Example:</h4>
 * <pre>&lt;id="INPUT1" type="LDAP_READER" ldapUrl="ldap://foobar.com:389/" base="ou=people,dc=foo,dc=bar" filter="uid=*" scope="subtree" user="uid=Manager,dc=foo,dc=bar" password="manager_pass" /&gt;</pre>
 * 
 * 
 * @author Francois Armand - Linagora
 * @since august 2006
 */
public class LdapReader extends Node {

	/*
	 * Things read from the xml .grf file
	 */
	private static final String XML_FILTER_ATTRIBUTE = "filter";
	private static final String XML_BASE_ATTRIBUTE = "base";
	private static final String XML_SCOPE_ATTRIBUTE = "scope";
	private static final String XML_LDAPURL_ATTRIBUTE = "ldapUrl";
	private static final String XML_USER_ATTRIBUTE = "user";
	private static final String XML_PASSWORD_ATTRIBUTE = "password";

	/**
	 * Component type
	 */
	public final static String COMPONENT_TYPE = "LDAP_READER";
	
	
	
	/**
	 * Readers haven't any input port
	 * TODO: how to provide several output port ?
	 */
	private final static int OUTPUT_PORT = 0;
	
	/**
	 * The LDAP parser connected to the directory
	 */
	private LdapParser parser = null;
	private String base;
	private String filter;
	private int scope;
	private String ldapUrl;
	private String user;
	private String passwd;

	/**
	 * A logger for the class
	 */
	private static Log logger = LogFactory.getLog(LdapReader.class);


	/**
	 * Default constructor
	 * @param id of the object in graph
	 * @param ldapUrl to connect to
	 * @param base ldap base 
	 * @param filter filter to use in search request
	 * @param scope of the research TODO : explain the meaning of possible values
	 */
	public LdapReader(String id, String ldapUrl, String base, String filter, int scope) {
		super(id);
		this.base = base;
		this.ldapUrl = ldapUrl;
		this.filter = filter;
		this.scope = scope;
	}

	
	/**
	 * Constructor with authentification params
	 * @param id of the object in graph
	 * @param ldapUrl to connect to
	 * @param base ldap base 
	 * @param filter filter to use in search request
	 * @param scope of the research TODO : explain the meaning of possible values
	 * @param user the user dn to log on as
	 * @param passwd user's password
	 */
	public LdapReader(String id, String ldapUrl, String base, String filter, 
			int scope, String user, String passwd) {
		this(id, ldapUrl, base, filter, scope);
		this.user = user;
		this.passwd = passwd;
	}
	
	public String getType() {
		return COMPONENT_TYPE;
	}

	public void init() throws ComponentNotReadyException {
		super.init();
		// test that we have at least one output port
		if (outPorts.size() < 1) {
			throw new ComponentNotReadyException(getId() + ": atleast one output port has to be defined!");
		}
		if(this.user != null) {
			this.parser = new LdapParser(this.ldapUrl, this.base, this.filter, this.scope, this.user, this.passwd);
		} else {
			this.parser = new LdapParser(this.ldapUrl, this.base, this.filter, this.scope);
		}	
			
		/*
		 * TODO : well... I don't know how to add LdapConnection node to transformation graphe.
		 * But it will be better if LdapConnection node exist, as it is for DB connection.
		 * For now, there is a connection by parser.
		 */
//		parser.open(getGraph().getLdapConnection(this.ldapConnectionId), getOutputPort(OUTPUT_PORT).getMetadata());
//		LdapConnection ldapCon = new LdapConnection("id_ldapCon",ldapUrl,"","");
		parser.init(getOutputPort(OUTPUT_PORT).getMetadata());
		/*
		 * Well... some other things to do ?
		 */
	}
	
	/**
	 * This is the main method. It is call at while data are available.
	 * this method is a copy of run() mathod from DBInput table.
	 */
	public void run() {
		// we need to create data record - take the metadata from first output port
		DataRecord record = new DataRecord(this.getOutputPort(OUTPUT_PORT).getMetadata());
		record.init();

		try {
			// till it reaches end of data or it is stopped from outside
			while ((record = parser.getNext(record)) != null && runIt) {
				//broadcast the record to all connected Edges
				this.writeRecordBroadcast(record);
			}
		} catch (IOException ex) {
			resultMsg = ex.getMessage();
			resultCode = Node.RESULT_ERROR;
			closeAllOutputPorts();
			return;
		} catch (Exception ex) {
			resultMsg = ex.getMessage();
			resultCode = Node.RESULT_FATAL_ERROR;
			return;
		}
		// we are done, close all connected output ports to indicate end of stream
		broadcastEOF();
		//close the parser
		parser.close();
		if (runIt) {
			resultMsg = "OK";
		} else {
			resultMsg = "STOPPED";
		}
		resultCode = Node.RESULT_OK;
	}

	/**
	 * Validate the config
	 */
    @Override
    public ConfigurationStatus checkConfig(ConfigurationStatus status) {
        //TODO
        return status;
    }

	/*
	 * Isn't this function mandatory ??
	 */
	public static Node fromXML(TransformationGraph graph, Element nodeXML) throws XMLConfigurationException {
		ComponentXMLAttributes xattribs = new ComponentXMLAttributes(nodeXML, graph);
		LdapReader aLdapReader = null;
		int i_scope;
		
		String scope = xattribs.getString(XML_SCOPE_ATTRIBUTE, null);
		if(scope.equalsIgnoreCase("OBJECT")) {
			i_scope = SearchControls.OBJECT_SCOPE;
		} else if(scope.equalsIgnoreCase("ONELEVEL")) {
			i_scope = SearchControls.ONELEVEL_SCOPE;
		} else if(scope.equalsIgnoreCase("SUBTREE")) {
			i_scope = SearchControls.SUBTREE_SCOPE;
		} else {
			StringBuffer msg = new StringBuffer();
			if (scope == null) {
				msg.append("Missing scope specification");
			} else {
				msg.append("Invalid scope specification \"").append(scope).append("\"");
			}
			msg.append(" in component ").append(xattribs.getString(Node.XML_ID_ATTRIBUTE, "unknown ID"));
			msg.append("; defaulting to scope \"OBJECT\"");
			logger.warn(msg.toString());

			i_scope = SearchControls.OBJECT_SCOPE;			
		}

		try {
			if(xattribs.exists(XML_USER_ATTRIBUTE) && xattribs.exists(XML_PASSWORD_ATTRIBUTE) ) {
				aLdapReader = new LdapReader(
						xattribs.getString(Node.XML_ID_ATTRIBUTE),
						xattribs.getString(XML_LDAPURL_ATTRIBUTE),
						xattribs.getString(XML_BASE_ATTRIBUTE),
						xattribs.getString(XML_FILTER_ATTRIBUTE),
						i_scope,
						xattribs.getString(XML_USER_ATTRIBUTE),
						xattribs.getString(XML_PASSWORD_ATTRIBUTE));
			} else {
				aLdapReader = new LdapReader(
						xattribs.getString(Node.XML_ID_ATTRIBUTE),
						xattribs.getString(XML_LDAPURL_ATTRIBUTE),
						xattribs.getString(XML_BASE_ATTRIBUTE),
						xattribs.getString(XML_FILTER_ATTRIBUTE),
						i_scope);
			}
		} catch (Exception ex) {
			throw new XMLConfigurationException(COMPONENT_TYPE + ":" + xattribs.getString(XML_ID_ATTRIBUTE," unknown ID ") + ":" + ex.getMessage(), ex);
		}
		
		return aLdapReader;
	}
	
}
