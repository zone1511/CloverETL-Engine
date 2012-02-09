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
package org.jetel.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetel.data.Defaults;
import org.jetel.data.parser.AbstractXmlSaxParser;
import org.jetel.data.parser.AbstractXmlSaxParser.MyHandler;
import org.jetel.data.parser.XMLEXmlSaxParser;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationProblem;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.ConfigurationStatus.Priority;
import org.jetel.exception.ConfigurationStatus.Severity;
import org.jetel.exception.JetelException;
import org.jetel.exception.XMLConfigurationException;
import org.jetel.graph.Node;
import org.jetel.graph.Result;
import org.jetel.graph.TransformationGraph;
import org.jetel.util.AutoFilling;
import org.jetel.util.ReadableChannelIterator;
import org.jetel.util.file.FileURLParser;
import org.jetel.util.file.FileUtils;
import org.jetel.util.property.ComponentXMLAttributes;
import org.jetel.util.property.PropertyRefResolver;
import org.jetel.util.property.RefResFlag;
import org.jetel.util.string.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <h3>XMLExtract Component</h3>
 *
 * <!-- Provides the logic to parse a xml file and filter to different ports based on
 * a matching element. The element and all children will be turned into a
 * Data record -->
 *
 * <table border="1">
 *  <th>Component:</th>
 * <tr><td><h4><i>Name:</i></h4></td>
 * <td>XMLExtract</td></tr>
 * <tr><td><h4><i>Category:</i></h4></td>
 * <td></td></tr>
 * <tr><td><h4><i>Description:</i></h4></td>
 * <td>Provides the logic to parse a xml file and filter to different ports based on
 * a matching element. The element and all children will be turned into a
 * Data record.</td></tr>
 * <tr><td><h4><i>Inputs:</i></h4></td>
 * <td>0</td></tr>
 * <tr><td><h4><i>Outputs:</i></h4></td>
 * <td>Output port[0] defined/connected. Depends on mapping definition.</td></tr>
 * <tr><td><h4><i>Comment:</i></h4></td>
 * <td></td></tr>
 * </table>
 *  <br>
 *  <table border="1">
 *  <th>XML attributes:</th>
 *  <tr><td><b>type</b></td><td>"XML_EXTRACT"</td></tr>
 *  <tr><td><b>id</b></td><td>component identification</td>
 *  <tr><td><b>sourceUri</b></td><td>location of source XML data to process</td>
 *  <tr><td><b>useNestedNodes</b></td><td><b>true</b> if nested unmapped XML elements will be used as data source; <b>false</b> if will be ignored</td>
 *  <tr><td><b>mapping</b></td><td>&lt;mapping&gt;</td>
 *  </tr>
 *  </table>
 *
 * Provides the logic to parse a xml file and filter to different ports based on
 * a matching element. The element and all children will be turned into a
 * Data record.<br>
 * Mapping attribute contains mapping hierarchy in XML form. DTD of mapping:<br>
 * <code>
 * &lt;!ELEMENT Mappings (Mapping*)&gt;<br>
 * 
 * &lt;!ELEMENT Mapping (Mapping*)&gt;<br>
 * &lt;!ATTLIST Mapping<br>
 * &nbsp;element NMTOKEN #REQUIRED<br>      
 * &nbsp;&nbsp;//name of binded XML element<br>  
 * &nbsp;outPort NMTOKEN #IMPLIED<br>      
 * &nbsp;&nbsp;//name of output port for this mapped XML element<br>
 * &nbsp;parentKey NMTOKEN #IMPLIED<br>     
 * &nbsp;&nbsp;//field name of parent record, which is copied into field of the current record<br>
 * &nbsp;&nbsp;//passed in generatedKey atrribute<br> 
 * &nbsp;generatedKey NMTOKEN #IMPLIED<br>  
 * &nbsp;&nbsp;//see parentKey comment<br>
 * &nbsp;sequenceField NMTOKEN #IMPLIED<br> 
 * &nbsp;&nbsp;//field name, which will be filled by value from sequence<br>
 * &nbsp;&nbsp;//(can be used to generate new key field for relative records)<br> 
 * &nbsp;sequenceId NMTOKEN #IMPLIED<br>    
 * &nbsp;&nbsp;//we can supply sequence id used to fill a field defined in a sequenceField attribute<br>
 * &nbsp;&nbsp;//(if this attribute is omited, non-persistent PrimitiveSequence will be used)<br>
 * &nbsp;xmlFields NMTOKEN #IMPLIED<br>     
 * &nbsp;&nbsp;//comma separeted xml element names, which will be mapped on appropriate record fields<br>
 * &nbsp;&nbsp;//defined in cloverFields attribute<br>
 * &nbsp;cloverFields NMTOKEN #IMPLIED<br>  
 * &nbsp;&nbsp;//see xmlFields comment<br>
 * &gt;<br>
 * </code>
 * All nested XML elements will be recognized as record fields and mapped by name
 * (except elements serviced by other nested Mapping elements), if you prefere other mapping
 * xml fields and clover fields than 'by name', use xmlFields and cloveFields attributes
 * to setup custom fields mapping. 'useNestedNodes' component attribute defines
 * if also child of nested xml elements will be mapped on the current clover record.
 * Record from nested Mapping element could be connected via key fields with parent record produced
 * by parent Mapping element (see parentKey and generatedKey attribute notes).
 * In case that fields are unsuitable for key composing, extractor could fill
 * one or more fields with values comming from sequence (see sequenceField and sequenceId attribute). 
 * 
 * For example: given an xml file:<br>
 * <code>
 * &lt;myXML&gt; <br>
 * &nbsp;&lt;phrase&gt; <br>
 * &nbsp;&nbsp;&lt;text&gt;hello&lt;/text&gt; <br>
 * &nbsp;&nbsp;&lt;localization&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;chinese&gt;how allo yee dew ying&lt;/chinese&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;german&gt;wie gehts&lt;/german&gt; <br>
 * &nbsp;&nbsp;&lt;/localization&gt; <br>
 * &nbsp;&lt;/phrase&gt; <br>
 * &nbsp;&lt;locations&gt; <br>
 * &nbsp;&nbsp;&lt;location&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;name&gt;Stormwind&lt;/name&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;description&gt;Beautiful European architecture with a scenic canal system.&lt;/description&gt; <br>
 * &nbsp;&nbsp;&lt;/location&gt; <br>
 * &nbsp;&nbsp;&lt;location&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;name&gt;Ironforge&lt;/name&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;description&gt;Economic capital of the region with a high population density.&lt;/description&gt; <br>
 * &nbsp;&nbsp;&lt;/location&gt; <br>
 * &nbsp;&lt;/locations&gt; <br>
 * &nbsp;&lt;someUselessElement&gt;...&lt;/someUselessElement&gt; <br>
 * &nbsp;&lt;someOtherUselessElement/&gt; <br>
 * &nbsp;&lt;phrase&gt; <br>
 * &nbsp;&nbsp;&lt;text&gt;bye&lt;/text&gt; <br>
 * &nbsp;&nbsp;&lt;localization&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;chinese&gt;she yee lai ta&lt;/chinese&gt; <br>
 * &nbsp;&nbsp;&nbsp;&lt;german&gt;aufweidersehen&lt;/german&gt; <br>
 * &nbsp;&nbsp;&lt;/localization&gt; <br>
 * &nbsp;&lt;/phrase&gt; <br>
 * &lt;/myXML&gt; <br>
 * </code> Suppose we want to pull out "phrase" as one datarecord,
 * "localization" as another datarecord, and "location" as the final datarecord
 * and ignore the useless elements. First we define the metadata for the
 * records. Then create the following mapping in the graph: <br>
 * <code>
 * &lt;node id="myId" type="com.lrn.etl.job.component.XMLExtract"&gt; <br>
 * &nbsp;&lt;attr name="mapping"&gt;<br>
 * &nbsp;&nbsp;&lt;Mapping element="phrase" outPort="0" sequenceField="id"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Mapping element="localization" outPort="1" parentKey="id" generatedKey="parent_id"/&gt;<br>
 * &nbsp;&nbsp;&lt;/Mapping&gt; <br>
 * &nbsp;&nbsp;&lt;Mapping element="location" outPort="2"/&gt;<br>
 * &nbsp;&lt;/attr&gt;<br>
 * &lt;/node&gt;<br>
 * </code> Port 0 will get the DataRecords:<br>
 * 1) id=1, text=hello<br>
 * 2) id=2, text=bye<br>
 * Port 1 will get:<br>
 * 1) parent_id=1, chinese=how allo yee dew ying, german=wie gehts<br>
 * 2) parent_id=2, chinese=she yee lai ta, german=aufwiedersehen<br>
 * Port 2 will get:<br>
 * 1) name=Stormwind, description=Beautiful European architecture with a scenic
 * canal system.<br>
 * 2) name=Ironforge, description=Economic capital of the region with a high
 * population density.<br>
 * <hr>
 * Issue: Enclosing elements having values are not supported.<br>
 * i.e. <br>
 * <code>
 *   &lt;x&gt; <br>
 *     &lt;y&gt;z&lt;/y&gt;<br>
 *     xValue<br>
 *   &lt;/x&gt;<br>
 * </code> there will be no column x with value xValue.<br>
 * Issue: Namespaces are not considered.<br>
 * i.e. <br>
 * <code>
 *   &lt;ns1:x&gt;xValue&lt;/ns1:x&gt;<br>
 *   &lt;ns2:x&gt;xValue2&lt;/ns2:x&gt;<br>
 * </code> will be considered the same x.
 *
 * @author KKou
 */

/*
 * Invariant:
 *  Mappings are preprocessed so that all element and attribute references stored in internal structures
 *  use universal names :
 *  	element="mov:movies" -> element="{http://www.javlin.eu}movies"
 *  	xmlFields="mov:att1" -> xmlFields="{http://www.javlin.eu}att1"
 *  
 *  Default namespace in mapping -> NOT POSSIBLE
 *  We are not able to implement default namespace functionality.
 *  Default namespace (declared without prefix e.g. xmlns="http://www.javlin.eu/default-ns") does
 *  NOT apply to attributes!!! (see www.w3.org/TR/REC-xml-names/#defaulting)
 *  Since we currently do not know which xmlFields 
 *  are attributes and which are elements -> so we do not know which should be expanded by default namespace.
 *  User therefore has to declare explicit namespace prefix in the Mapping, even for
 *  attributes and elements falling into the default namespace in the processed XML document.
 *  
 */
public class XMLExtract extends Node {

    // logger
    private static final Log logger = LogFactory.getLog(XMLExtract.class);

    // xml attributes
    public static final String XML_SOURCEURI_ATTRIBUTE = "sourceUri";
    private static final String XML_USENESTEDNODES_ATTRIBUTE = "useNestedNodes";
    private static final String XML_MAPPING_ATTRIBUTE = "mapping";
    private static final String XML_CHARSET_ATTRIBUTE = "charset";

    // mapping attributes
	private final static String XML_MAPPING_URL_ATTRIBUTE = "mappingURL";
    private static final String XML_ELEMENT = "element";
    private static final String XML_OUTPORT = "outPort";
    private static final String XML_PARENTKEY = "parentKey";
    private static final String XML_GENERATEDKEY = "generatedKey";
    private static final String XML_XMLFIELDS = "xmlFields";
    private static final String XML_CLOVERFIELDS = "cloverFields";
    private static final String XML_SEQUENCEFIELD = "sequenceField";
    private static final String XML_SEQUENCEID = "sequenceId";
    private static final String XML_SKIP_ROWS_ATTRIBUTE = "skipRows";
    private static final String XML_NUMRECORDS_ATTRIBUTE = "numRecords";
	private static final String XML_TRIM_ATTRIBUTE = "trim";
    private static final String XML_VALIDATE_ATTRIBUTE = "validate";
    private static final String XML_XML_FEATURES_ATTRIBUTE = "xmlFeatures";
    private static final String XML_NAMESPACE_BINDINGS_ATTRIBUTE = "namespaceBindings";
    
    /** MiSho Experimental Templates */
    private static final String XML_TEMPLATE_ID = "templateId";
    private static final String XML_TEMPLATE_REF = "templateRef";
    private static final String XML_TEMPLATE_DEPTH = "nestedDepth";
    
    // component name
    public final static String COMPONENT_TYPE = "XML_EXTRACT";
    
    // from which input port to read
	private final static int INPUT_PORT = 0;

	public static final String PARENT_MAPPING_REFERENCE_PREFIX = "..";
	public static final String PARENT_MAPPING_REFERENCE_SEPARATOR = "/";
	public static final String PARENT_MAPPING_REFERENCE_PREFIX_WITHSEPARATOR = PARENT_MAPPING_REFERENCE_PREFIX + PARENT_MAPPING_REFERENCE_SEPARATOR;
	public static final String ELEMENT_VALUE_REFERENCE = "{}.";
	
    // Where the XML comes from
    private InputSource m_inputSource;

    // input file
    private String inputFile;
	private ReadableChannelIterator readableChannelIterator;
	
    // autofilling support
    private AutoFilling autoFilling = new AutoFilling();

	private String xmlFeatures;
	
	private boolean validate;

	private String charset = Defaults.DataParser.DEFAULT_CHARSET_DECODER;
	
	private String mappingURL;

	private String mapping;

	private NodeList mappingNodes;
	
	private XMLEXmlSaxParser parser = new XMLEXmlSaxParser(null, this, PARENT_MAPPING_REFERENCE_PREFIX);
	
    /**
     * Constructs an XML Extract node with the given id.
     */
    public XMLExtract(String id) {
        super(id);
    }
    
    /**
     * Creates an inctence of this class from a xml node.
     * @param graph
     * @param xmlElement
     * @return
     * @throws XMLConfigurationException
     */
    public static Node fromXML(TransformationGraph graph, Element xmlElement) throws XMLConfigurationException {
        ComponentXMLAttributes xattribs = new ComponentXMLAttributes(xmlElement, graph);
        XMLExtract extract;
        
        try {
        	// constructor
            extract = new XMLExtract(xattribs.getString(XML_ID_ATTRIBUTE));
            
            // set input file
            extract.setInputFile(xattribs.getStringEx(XML_SOURCEURI_ATTRIBUTE,RefResFlag.SPEC_CHARACTERS_OFF));
            
            // set dtd schema
//            if (xattribs.exists(XML_SCHEMA_ATTRIBUTE)) {
//            	extract.setSchemaFile(xattribs.getString(XML_SCHEMA_ATTRIBUTE));
//            }
            
            // if can use nested nodes.
            if(xattribs.exists(XML_USENESTEDNODES_ATTRIBUTE)) {
                extract.setUseNestedNodes(xattribs.getBoolean(XML_USENESTEDNODES_ATTRIBUTE));
            }
            
            // set mapping
            String mappingURL = xattribs.getStringEx(XML_MAPPING_URL_ATTRIBUTE, null,RefResFlag.SPEC_CHARACTERS_OFF);
            String mapping = xattribs.getString(XML_MAPPING_ATTRIBUTE, null);
            NodeList nodes = xmlElement.getChildNodes();
            if (mappingURL != null) extract.setMappingURL(mappingURL);
            else if (mapping != null) extract.setMapping(mapping);
            else if (nodes != null && nodes.getLength() > 0){
                //old-fashioned version of mapping definition
                //mapping xml elements are child nodes of the component
            	extract.setNodes(nodes);
            } else {
            	xattribs.getStringEx(XML_MAPPING_URL_ATTRIBUTE,RefResFlag.SPEC_CHARACTERS_OFF); // throw configuration exception
            }

            // set namespace bindings attribute
			if (xattribs.exists(XML_NAMESPACE_BINDINGS_ATTRIBUTE)) {
				Properties props = null;
				try {
					props = new Properties();
					final String content = xattribs.getString(
							XML_NAMESPACE_BINDINGS_ATTRIBUTE, null);
					if (content != null) {
						props.load(new StringReader(content));
					}
				} catch (IOException e) {
					throw new XMLConfigurationException("Unable to initialize namespace bindings",e);
				}
				
				final HashMap<String,String> namespaceBindings = new HashMap<String,String>();
				for (String name : props.stringPropertyNames()) {
					namespaceBindings.put(name, props.getProperty(name));
				}
				
				extract.setNamespaceBindings(namespaceBindings);
			}
            
            // set a skip row attribute
            if (xattribs.exists(XML_SKIP_ROWS_ATTRIBUTE)){
            	extract.setSkipRows(xattribs.getInteger(XML_SKIP_ROWS_ATTRIBUTE));
            }
            
            // set a numRecord attribute
            if (xattribs.exists(XML_NUMRECORDS_ATTRIBUTE)){
            	extract.setNumRecords(xattribs.getInteger(XML_NUMRECORDS_ATTRIBUTE));
            }
            
            if (xattribs.exists(XML_XML_FEATURES_ATTRIBUTE)){
            	extract.setXmlFeatures(xattribs.getString(XML_XML_FEATURES_ATTRIBUTE));
            }
            if (xattribs.exists(XML_VALIDATE_ATTRIBUTE)){
            	extract.setValidate(xattribs.getBoolean(XML_VALIDATE_ATTRIBUTE));
            }
            if (xattribs.exists(XML_CHARSET_ATTRIBUTE)){
            	extract.setCharset(xattribs.getString(XML_CHARSET_ATTRIBUTE));
            }
            
			if (xattribs.exists(XML_TRIM_ATTRIBUTE)){
				extract.setTrim(xattribs.getBoolean(XML_TRIM_ATTRIBUTE));
			}
            return extract;
        } catch (Exception ex) {
            throw new XMLConfigurationException(COMPONENT_TYPE + ":" + xattribs.getString(XML_ID_ATTRIBUTE," unknown ID ") + ":" + ex.getMessage(),ex);
        }
    }
    
	@Deprecated
    private void setNodes(NodeList nodes) {
    	this.mappingNodes = nodes;
	}


	public void setMappingURL(String mappingURL) {
    	this.mappingURL = mappingURL;
    	parser.setMappingURL(mappingURL);
	}


	public void setMapping(String mapping) {
		this.mapping = mapping;
		parser.setMapping(mapping);
	}


	/**
     * Sets the trim indicator.
     * @param trim
     */
	public void setTrim(boolean trim) {
		parser.setTrim(trim);
	}
    
	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();

		if (firstRun()) {
			// sets input file to readableChannelIterator and sets its settings (directory, charset, input port,...)
	        if (inputFile != null) {
	        	createReadableChannelIterator();
	        	this.readableChannelIterator.init();
	        }
		} else {
			autoFilling.reset();
			this.readableChannelIterator.reset();
		}
		
        if (!readableChannelIterator.isGraphDependentSource()) prepareNextSource();
	}	
	

    
    @Override
    public Result execute() throws Exception {
    	Result result;
    	
    	// parse xml from input file(s).
    	if (parser.parse(validate, xmlFeatures, charset, readableChannelIterator, autoFilling, m_inputSource)) {
    		// finished successfully
    		result = runIt ? Result.FINISHED_OK : Result.ABORTED;
    		
    	} else {
    		// an error occurred 
    		result = runIt ? Result.ERROR : Result.ABORTED;
    	}

    	broadcastEOF();
		return result;
    }
    
	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
		//no input channel is closed here - this could be changed in future
	}
	
    /**
     * Perform sanity checks.
     */
    @Override
	public void init() throws ComponentNotReadyException {
        if(isInitialized()) return;
		super.init();
		
		parser.setGraph(getGraph());
		parser.init();
    }
    
    private void createReadableChannelIterator() throws ComponentNotReadyException {
    	TransformationGraph graph = getGraph();
    	URL projectURL = graph != null ? graph.getRuntimeContext().getContextURL() : null;
    	
    	this.readableChannelIterator = new ReadableChannelIterator(
    			getInputPort(INPUT_PORT), 
    			projectURL,
    			inputFile);
    	this.readableChannelIterator.setCharset(charset);
    	this.readableChannelIterator.setPropertyRefResolver(new PropertyRefResolver(graph.getGraphProperties()));
    	this.readableChannelIterator.setDictionary(graph.getDictionary());
    }
	
	/**
	 * Prepares a next source.
	 * @throws ComponentNotReadyException
	 */
	private void prepareNextSource() throws ComponentNotReadyException {
        try {
            if(!nextSource()) {
                //throw new ComponentNotReadyException("FileURL attribute (" + inputFile + ") doesn't contain valid file url.");
            }
        } catch (JetelException e) {
            throw new ComponentNotReadyException(e.getMessage()/*"FileURL attribute (" + inputFile + ") doesn't contain valid file url."*/, e);
        }
	}

	/**
     * Switch to the next source file.
	 * @return
	 * @throws JetelException 
	 */
	private boolean nextSource() throws JetelException {
		ReadableByteChannel stream = null; 
		while (readableChannelIterator.hasNext()) {
			autoFilling.resetSourceCounter();
			autoFilling.resetGlobalSourceCounter();
			stream = readableChannelIterator.nextChannel();
			if (stream == null) continue; // if record no record found
			autoFilling.setFilename(readableChannelIterator.getCurrentFileName());
			File tmpFile = new File(autoFilling.getFilename());
			long timestamp = tmpFile.lastModified();
			autoFilling.setFileSize(tmpFile.length());
			autoFilling.setFileTimestamp(timestamp == 0 ? null : new Date(timestamp));				
			m_inputSource = new InputSource(Channels.newInputStream(stream));
			return true;
		}
        readableChannelIterator.blankRead();
		return false;
	}
	
    @Override
	public String getType() {
        return COMPONENT_TYPE;
    }
    
    @Override
    public ConfigurationStatus checkConfig(ConfigurationStatus status) {

    	if (charset != null && !Charset.isSupported(charset)) {
        	status.add(new ConfigurationProblem(
            		"Charset "+charset+" not supported!", 
            		ConfigurationStatus.Severity.ERROR, this, ConfigurationStatus.Priority.NORMAL));
        }

    	TransformationGraph graph = getGraph();
    	//Check whether XML mapping schema is valid
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new MyHandler();
			InputSource is = null;
			Document doc = null;
			if (this.mappingURL != null) {
				String filePath = FileUtils.getFile(graph.getRuntimeContext().getContextURL(), mappingURL);
				is = new InputSource(new FileInputStream(new File(filePath)));
				ReadableByteChannel ch = FileUtils.getReadableChannel(
						graph != null ? graph.getRuntimeContext().getContextURL() : null, mappingURL);
				doc = AbstractXmlSaxParser.createDocumentFromChannel(ch);
			} else if (this.mapping != null) {
				// inlined mapping
				// don't use the charset of the component's input files, but the charset of the .grf file
		        is = new InputSource(new StringReader(mapping));
				doc = AbstractXmlSaxParser.createDocumentFromString(mapping);
	        }
			if (is != null) {
				saxParser.parse(is, handler);
				Set<String> attributesNames = ((MyHandler) handler).getAttributeNames();
				for (String attributeName : attributesNames) {
					if (!isXMLAttribute(attributeName)) {
						status.add(new ConfigurationProblem("Can't resolve XML attribute: " + attributeName, Severity.WARNING, this, Priority.NORMAL));
					}
				}
			}
			if (doc != null) {
				Element rootElement = doc.getDocumentElement();
				mappingNodes = rootElement.getChildNodes();
				 
		        for (int i = 0; i < mappingNodes.getLength(); i++) {
		            org.w3c.dom.Node node = mappingNodes.item(i);
		            List<String> errors = parser.processMappings(graph, null, node);
		            ConfigurationProblem problem;
		            for (String error : errors) {
		            	problem = new ConfigurationProblem("Mapping error - " + error, Severity.WARNING, this, Priority.NORMAL);
		                status.add(problem);
					}
		        }
			}
		} catch (Exception e) {
			status.add(new ConfigurationProblem("Can't parse XML mapping schema. Reason: " + e.getMessage(), Severity.ERROR, this, Priority.NORMAL));
		} finally {
			parser.getDeclaredTemplates().clear();
		}
		
		// TODO Labels:
		//for (Mapping mapping: getMappings().values()) {
		//	checkUniqueness(status, mapping);
		//}
		// TODO Labels end
		
        try { 
            // check inputs
        	if (inputFile != null) {
        		createReadableChannelIterator();
        		this.readableChannelIterator.checkConfig();
        		
            	URL contextURL = graph != null ? graph.getRuntimeContext().getContextURL() : null;
        		String fName = null; 
        		Iterator<String> fit = readableChannelIterator.getFileIterator();
        		while (fit.hasNext()) {
        			try {
        				fName = fit.next();
        				if (fName.equals("-")) continue;
        				if (fName.startsWith("dict:")) continue; //this test has to be here, since an involuntary warning is caused
        				String mostInnerFile = FileURLParser.getMostInnerAddress(fName);
        				URL url = FileUtils.getFileURL(contextURL, mostInnerFile);
        				if (FileUtils.isServerURL(url)) {
        					//FileUtils.checkServer(url); //this is very long operation
        					continue;
        				}
        				if (FileURLParser.isArchiveURL(fName)) {
        					// test if the archive file exists
        					// getReadableChannel is too long for archives
        					String path = url.getRef() != null ? url.getFile() + "#" + url.getRef() : url.getFile();
        					if (new File(path).exists()) continue;
        					throw new ComponentNotReadyException("File is unreachable: " + fName);
        				}
        				FileUtils.getReadableChannel(contextURL, fName).close();
        			} catch (IOException e) {
        				throw new ComponentNotReadyException("File is unreachable: " + fName, e);
        			} catch (ComponentNotReadyException e) {
        				throw new ComponentNotReadyException("File is unreachable: " + fName, e);
        			}
        		}
        	}
		} catch (ComponentNotReadyException e) {
            ConfigurationProblem problem = new ConfigurationProblem(e.getMessage(), ConfigurationStatus.Severity.WARNING, this, ConfigurationStatus.Priority.NORMAL);
            if(!StringUtils.isEmpty(e.getAttributeName())) {
                problem.setAttributeName(e.getAttributeName());
            }
            status.add(problem);
        } finally {
        	free();
        }
    	
        //TODO
        return status;
    }
    
	private boolean isXMLAttribute(String attribute) {
		//returns true if given attribute is known XML attribute
		if (attribute.equals(XML_ELEMENT) ||
				attribute.equals(XML_OUTPORT) ||
				attribute.equals(XML_PARENTKEY) ||
				attribute.equals(XML_GENERATEDKEY) ||
				attribute.equals(XML_XMLFIELDS) ||
				attribute.equals(XML_CLOVERFIELDS) ||
				attribute.equals(XML_SEQUENCEFIELD) ||
				attribute.equals(XML_SEQUENCEID) ||
				attribute.equals(XML_SKIP_ROWS_ATTRIBUTE) ||
				attribute.equals(XML_NUMRECORDS_ATTRIBUTE) ||
				attribute.equals(XML_TRIM_ATTRIBUTE) ||
				attribute.equals(XML_VALIDATE_ATTRIBUTE) ||
				attribute.equals(XML_XML_FEATURES_ATTRIBUTE) ||
				attribute.equals(XML_TEMPLATE_ID) ||
				attribute.equals(XML_TEMPLATE_REF) ||
				attribute.equals(XML_TEMPLATE_DEPTH)) {
			return true;
		}
		
		return false;
	}
    
    public org.w3c.dom.Node toXML() {
        return null;
    }
    
    /**
     * Set the input source containing the XML this will parse.
     */
    public void setInputSource(InputSource inputSource) {
        m_inputSource = inputSource;
    }
    
    /**
     * Sets an input file.
     * @param inputFile
     */
    public void setInputFile(String inputFile) {
    	this.inputFile = inputFile;
    }
    
	/**
	 * 
	 * @param useNestedNodes
	 */
	public void setUseNestedNodes(boolean useNestedNodes) {
		parser.setUseNestedNodes(useNestedNodes);
	}

    /**
     * Sets skipRows - how many elements to skip.
     * @param skipRows
     */
    public void setSkipRows(int skipRows) {
        parser.setSkipRows(skipRows);
    }
    
    /**
     * Sets numRecords - how many elements to process.
     * @param numRecords
     */
    public void setNumRecords(int numRecords) {
        parser.setNumRecords(numRecords);
    }

    /**
     * Sets the xml feature.
     * @param xmlFeatures
     */
    public void setXmlFeatures(String xmlFeatures) {
    	this.xmlFeatures = xmlFeatures;
	}

    /**
     * Sets validation option.
     * @param validate
     */
    public void setValidate(boolean validate) {
    	this.validate = validate;
	}
    
    /**
     * Sets charset for dictionary and input port reading.
     * @param string
     */
    public void setCharset(String charset) {
    	this.charset = charset;
	}
    
    /**
     * Sets namespace bindings to allow processing that relate namespace prefix used in Mapping
     * and namespace URI used in processed XML document
	 * @param namespaceBindings the namespaceBindings to set
	 */
	private void setNamespaceBindings(HashMap<String, String> namespaceBindings) {
		parser.setNamespaceBindings(namespaceBindings);
	}
	
//    private void resetRecord(DataRecord record) {
//        // reset the record setting the nullable fields to null and default
//        // values. Unfortunately init() does not do this, so if you have a field
//        // that's nullable and you never set a value to it, it will NOT be null.
//        
//        // the reason we need to reset data records is the fact that XML data is
//        // not as rigidly
//        // structured as csv fields, so column values are regularly "missing"
//        // and without a reset
//        // the prior row's value will be present.
//        for (int i = 0; i < record.getNumFields(); i++) {
//            DataFieldMetadata fieldMetadata = record.getMetadata().getField(i);
//            DataField field = record.getField(i);
//            if (fieldMetadata.isNullable()) {
//                // Default all nullables to null
//                field.setNull(true);
//            } else if(fieldMetadata.isDefaultValue()) {
//                //Default all default values to their given defaults
//                field.setToDefaultValue();
//            } else {
//                // Not nullable so set it to the default value (what init does)
//                switch (fieldMetadata.getType()) {
//                    case DataFieldMetadata.INTEGER_FIELD:
//                        ((IntegerDataField) field).setValue(0);
//                        break;
//                        
//                    case DataFieldMetadata.STRING_FIELD:
//                        ((StringDataField) field).setValue("");
//                        break;
//                        
//                    case DataFieldMetadata.DATE_FIELD:
//                    case DataFieldMetadata.DATETIME_FIELD:
//                        ((DateDataField) field).setValue(0);
//                        break;
//                        
//                    case DataFieldMetadata.NUMERIC_FIELD:
//                        ((NumericDataField) field).setValue(0);
//                        break;
//                        
//                    case DataFieldMetadata.LONG_FIELD:
//                        ((LongDataField) field).setValue(0);
//                        break;
//                        
//                    case DataFieldMetadata.DECIMAL_FIELD:
//                        ((NumericDataField) field).setValue(0);
//                        break;
//                        
//                    case DataFieldMetadata.BYTE_FIELD:
//                        ((ByteDataField) field).setValue((byte) 0);
//                        break;
//                        
//                    case DataFieldMetadata.UNKNOWN_FIELD:
//                    default:
//                        break;
//                }
//            }
//        }
//    }
}
