package org.oliot.epcis.service.capture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oliot.epcis.configuration.Configuration;
import org.oliot.epcis.service.capture.mongodb.MongoCaptureUtil;
import org.oliot.model.jsonschema.JsonSchemaLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * Copyright (C) 2015 Jaewook Jack Byun
 *
 * This project is part of Oliot (oliot.org), pursuing the implementation of
 * Electronic Product Code Information Service(EPCIS) v1.1 specification in
 * EPCglobal.
 * [http://www.gs1.org/gsmp/kc/epcglobal/epcis/epcis_1_1-standard-20140520.pdf]
 * 
 *
 * @author Jaewook Jack Byun, Ph.D student
 * 
 *         Korea Advanced Institute of Science and Technology (KAIST)
 * 
 *         Real-time Embedded System Laboratory(RESL)
 * 
 *         bjw0829@kaist.ac.kr, bjw0829@gmail.com
 *         
 * @author Sungpil Woo, Master student
 * 
 *         Korea Advanced Institute of Science and Technology (KAIST)
 * 
 *         Real-time Embedded System Laboratory(RESL)
 * 
 *         woosungpil@kaist.ac.kr, woosungpil7@gmail.com
 */

@Controller
@RequestMapping("/JsonEventCapture")
public class JsonEventCapture implements ServletContextAware {

	@Autowired
	ServletContext servletContext;

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	public String asyncPost(String inputString){
		String result = post(inputString);
		return result;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public String post(@RequestBody String inputString) {
		Configuration.logger.info(" EPCIS Json Document Capture Started.... ");

		if(Configuration.isCaptureVerfificationOn == true){
		//JSONParser parser = new JSONParser();
		JsonSchemaLoader schemaloader = new JsonSchemaLoader();
			try { 
				
				JSONObject json = new JSONObject(inputString);
				JSONObject schema_json = schemaloader.getGeneralschema();
				
				if(!validate(json, schema_json)){
					Configuration.logger.info("Json Document is invalid" + " about general_validcheck");
					return "Error: Json Document is not valid" + "general_validcheck";
				}
				JSONObject json2 = json.getJSONObject("epcis");
				JSONObject json3 = json2.getJSONObject("EPCISBody");
				JSONArray json4 = json3.getJSONArray("EventList");
				
				ApplicationContext ctx = new GenericXmlApplicationContext(
						"classpath:MongoConfig.xml");
				MongoOperations mongoOperation = (MongoOperations) ctx
						.getBean("mongoTemplate");
				
				for(int i = 0 ; i < json4.length(); i++){
					
					if(json4.getJSONObject(i).has("ObjectEvent") == true){
						
						/* startpoint of validation logic for ObjectEvent */
						
						JSONObject objecteventschema_json = schemaloader.getObjectEventschema();
						
						if(!validate(json4.getJSONObject(i).getJSONObject("ObjectEvent"), objecteventschema_json)){

							Configuration.logger.info("Json Document is not valid" + " detail validation check for objectevent");
							return "Error: Json Document is not valid" + " for detail validation check for objectevent";
						}
						/* finish validation logic for ObjectEvent */
						
						if(json4.getJSONObject(i).getJSONObject("ObjectEvent").has("any")){
							/* start finding namespace in the any field. */
							JSONObject anyobject = json4.getJSONObject(i).getJSONObject("ObjectEvent").getJSONObject("any");
							String namespace ="";
							String anyobject_str = anyobject.toString();
							String obj_field = anyobject_str.substring(1, anyobject_str.length()-1);
							boolean namespace_flag = false;
							
							StringTokenizer st1 = new StringTokenizer(obj_field, ",");
					        while(st1.hasMoreTokens()){
					            String temp = st1.nextToken();
					            StringTokenizer st2 = new StringTokenizer(temp, "\"");
					            while(st2.hasMoreTokens()){
					                String temp1 = st2.nextToken();
					                if(temp1.substring(0,1).equals("@")){
					                	namespace_flag = true;
					                	namespace = temp1.substring(1,temp1.length());
					                }
					            }
					        }
					        
					        if(!namespace_flag){
					        	Configuration.logger.info("Json Document doesn't have namespace in any field");
								return "Error: Json Document doesn't have namespace in any field" + " for detail validation check for objectevent";
		                
					        }
							/* finish finding namespace in the any field. */
					        
					        /* Start Validation whether each component use correct name space */
							StringTokenizer validation_st1 = new StringTokenizer(obj_field, ",");
							int indexcount = 0;
					        while(validation_st1.hasMoreTokens()){
					            String temp = validation_st1.nextToken();
					            StringTokenizer validation_st2 = new StringTokenizer(temp, "\"");
					            while(validation_st2.hasMoreTokens()){
					            	indexcount++;
					            	String temp1 = validation_st2.nextToken();
					                if(indexcount % 3 == 1){
					                	
					                	if(temp1.length()<namespace.length()){
					                		Configuration.logger.info("Json Document use invalid namespace in anyfield");
											return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for objectevent";
					                	}
					                	
					                	if(temp1.substring(0,1).equals("@")){
						                	// then, It is namespace that doesn't need to be validated
					                	}
					                	else{
					                		if(!temp1.substring(0,namespace.length()).equals(namespace)){
					                			Configuration.logger.info("Json Document use invalid namespace in anyfield");
												return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for objectevent";
					                		}
					                		
					                	}
					                	
					                }
					            }
					        }
					        /* Finish validation whether each component use correct name space */
					        
						}
						
						if (Configuration.backend.equals("MongoDB")) {
							MongoCaptureUtil m = new MongoCaptureUtil();
							m.objectevent_capture(json4.getJSONObject(i).getJSONObject("ObjectEvent"), mongoOperation);
						}
					}
					else if(json4.getJSONObject(i).has("AggregationEvent") == true){
						
						/* startpoint of validation logic for AggregationEvent */
						JSONObject aggregationeventschema_json = schemaloader.getAggregationEventschema();
						
						if(!validate(json4.getJSONObject(i).getJSONObject("AggregationEvent"), aggregationeventschema_json)){

							Configuration.logger.info("Json Document is not valid" + " detail validation check for aggregationevent");
							return "Error: Json Document is not valid" + " for detail validation check for aggregationevent";
						}
						/* finish validation logic for AggregationEvent */
						
						if(json4.getJSONObject(i).getJSONObject("AggregationEvent").has("any")){
							/* start finding namespace in the any field. */
							JSONObject anyobject = json4.getJSONObject(i).getJSONObject("AggregationEvent").getJSONObject("any");
							String namespace ="";
							String anyobject_str = anyobject.toString();
							String obj_field = anyobject_str.substring(1, anyobject_str.length()-1);
							boolean namespace_flag = false;
							
							StringTokenizer st1 = new StringTokenizer(obj_field, ",");
					        while(st1.hasMoreTokens()){
					            String temp = st1.nextToken();
					            StringTokenizer st2 = new StringTokenizer(temp, "\"");
					            while(st2.hasMoreTokens()){
					                String temp1 = st2.nextToken();
					                if(temp1.substring(0,1).equals("@")){
					                	namespace_flag = true;
					                	namespace = temp1.substring(1,temp1.length());
					                }
					            }
					        }
					        
					        if(!namespace_flag){
					        	Configuration.logger.info("Json Document doesn't have namespace in any field");
								return "Error: Json Document doesn't have namespace in any field" + " for detail validation check for aggregationevent";
		                
					        }
							/* finish finding namespace in the any field. */
					        
					        /* Start Validation whether each component use correct name space */
							StringTokenizer validation_st1 = new StringTokenizer(obj_field, ",");
							int indexcount = 0;
					        while(validation_st1.hasMoreTokens()){
					            String temp = validation_st1.nextToken();
					            StringTokenizer validation_st2 = new StringTokenizer(temp, "\"");
					            while(validation_st2.hasMoreTokens()){
					            	indexcount++;
					            	String temp1 = validation_st2.nextToken();
					                if(indexcount % 3 == 1){
					                	
					                	if(temp1.length()<namespace.length()){
					                		Configuration.logger.info("Json Document use invalid namespace in anyfield");
											return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for aggregationevent";
					                	}
					                	
					                	if(temp1.substring(0,1).equals("@")){
						                	// then, It is namespace that doesn't need to be validated
					                	}
					                	else{
					                		if(!temp1.substring(0,namespace.length()).equals(namespace)){
					                			Configuration.logger.info("Json Document use invalid namespace in anyfield");
												return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for aggregationevent";
					                		}
					                		
					                	}
					                	
					                }
					            }
					        }
					        /* Finish validation whether each component use correct name space */
					        
						}
						
						if (Configuration.backend.equals("MongoDB")) {
							MongoCaptureUtil m = new MongoCaptureUtil();
							m.aggregationevent_capture(json4.getJSONObject(i).getJSONObject("AggregationEvent"), mongoOperation);
						}
					}
					else if(json4.getJSONObject(i).has("TransformationEvent") == true){
						
						/* startpoint of validation logic for TransFormationEvent */
						JSONObject tranformationeventschema_json = schemaloader.getTransformationEventschema();
						
						if(!validate(json4.getJSONObject(i).getJSONObject("TransformationEvent"), tranformationeventschema_json)){

							Configuration.logger.info("Json Document is not valid" + " detail validation check for TransFormationEvent");
							return "Error: Json Document is not valid" + " for detail validation check for TransFormationEvent";
						}
						/* finish validation logic for TransFormationEvent */
						
						if(json4.getJSONObject(i).getJSONObject("TransformationEvent").has("any")){
							/* start finding namespace in the any field. */
							JSONObject anyobject = json4.getJSONObject(i).getJSONObject("TransformationEvent").getJSONObject("any");
							String namespace ="";
							String anyobject_str = anyobject.toString();
							String obj_field = anyobject_str.substring(1, anyobject_str.length()-1);
							boolean namespace_flag = false;
							
							StringTokenizer st1 = new StringTokenizer(obj_field, ",");
					        while(st1.hasMoreTokens()){
					            String temp = st1.nextToken();
					            StringTokenizer st2 = new StringTokenizer(temp, "\"");
					            while(st2.hasMoreTokens()){
					                String temp1 = st2.nextToken();
					                if(temp1.substring(0,1).equals("@")){
					                	namespace_flag = true;
					                	namespace = temp1.substring(1,temp1.length());
					                }
					            }
					        }
					        
					        if(!namespace_flag){
					        	Configuration.logger.info("Json Document doesn't have namespace in any field");
								return "Error: Json Document doesn't have namespace in any field" + " for detail validation check for TransFormationEvent";
		                
					        }
							/* finish finding namespace in the any field. */
					        
					        /* Start Validation whether each component use correct name space */
							StringTokenizer validation_st1 = new StringTokenizer(obj_field, ",");
							int indexcount = 0;
					        while(validation_st1.hasMoreTokens()){
					            String temp = validation_st1.nextToken();
					            StringTokenizer validation_st2 = new StringTokenizer(temp, "\"");
					            while(validation_st2.hasMoreTokens()){
					            	indexcount++;
					            	String temp1 = validation_st2.nextToken();
					                if(indexcount % 3 == 1){
					                	
					                	if(temp1.length()<namespace.length()){
					                		Configuration.logger.info("Json Document use invalid namespace in anyfield");
											return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for TransFormationEvent";
					                	}
					                	
					                	if(temp1.substring(0,1).equals("@")){
						                	// then, It is namespace that doesn't need to be validated
					                	}
					                	else{
					                		if(!temp1.substring(0,namespace.length()).equals(namespace)){
					                			Configuration.logger.info("Json Document use invalid namespace in anyfield");
												return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for TransFormationEvent";
					                		}
					                		
					                	}
					                	
					                }
					            }
					        }
					        /* Finish validation whether each component use correct name space */
					        
						}
						
						if (Configuration.backend.equals("MongoDB")) {
							MongoCaptureUtil m = new MongoCaptureUtil();
							m.transformationevent_capture(json4.getJSONObject(i).getJSONObject("TransformationEvent"), mongoOperation);
						}
					}
					else if(json4.getJSONObject(i).has("TransactionEvent") == true){
						
						/* startpoint of validation logic for TransFormationEvent */
						JSONObject transactioneventschema_json = schemaloader.getTransactionEventschema();
						
						if(!validate(json4.getJSONObject(i).getJSONObject("TransactionEvent"), transactioneventschema_json)){

							Configuration.logger.info("Json Document is not valid." + " detail validation check for TransactionEvent");
							return "Error: Json Document is not valid" + " for detail validation check for TransactionEvent";
						}
						/* finish validation logic for TransFormationEvent */
						
						if(json4.getJSONObject(i).getJSONObject("TransactionEvent").has("any")){
							/* start finding namespace in the any field. */
							JSONObject anyobject = json4.getJSONObject(i).getJSONObject("TransactionEvent").getJSONObject("any");
							String namespace ="";
							String anyobject_str = anyobject.toString();
							String obj_field = anyobject_str.substring(1, anyobject_str.length()-1);
							boolean namespace_flag = false;
							
							StringTokenizer st1 = new StringTokenizer(obj_field, ",");
					        while(st1.hasMoreTokens()){
					            String temp = st1.nextToken();
					            StringTokenizer st2 = new StringTokenizer(temp, "\"");
					            while(st2.hasMoreTokens()){
					                String temp1 = st2.nextToken();
					                if(temp1.substring(0,1).equals("@")){
					                	namespace_flag = true;
					                	namespace = temp1.substring(1,temp1.length());
					                }
					            }
					        }
					        
					        if(!namespace_flag){
					        	Configuration.logger.info("Json Document doesn't have namespace in any field");
								return "Error: Json Document doesn't have namespace in any field" + " for detail validation check for TransFormationEvent";
		                
					        }
							/* finish finding namespace in the any field. */
					        
					        /* Start Validation whether each component use correct name space */
							StringTokenizer validation_st1 = new StringTokenizer(obj_field, ",");
							int indexcount = 0;
					        while(validation_st1.hasMoreTokens()){
					            String temp = validation_st1.nextToken();
					            StringTokenizer validation_st2 = new StringTokenizer(temp, "\"");
					            while(validation_st2.hasMoreTokens()){
					            	indexcount++;
					            	String temp1 = validation_st2.nextToken();
					                if(indexcount % 3 == 1){
					                	
					                	if(temp1.length()<namespace.length()){
					                		Configuration.logger.info("Json Document use invalid namespace in anyfield");
											return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for TransactionEvent";
					                	}
					                	
					                	if(temp1.substring(0,1).equals("@")){
						                	// then, It is namespace that doesn't need to be validated
					                	}
					                	else{
					                		if(!temp1.substring(0,namespace.length()).equals(namespace)){
					                			Configuration.logger.info("Json Document use invalid namespace in anyfield");
												return "Error: Json Document use invalid namespace in anyfield" + " for detail validation check for TransactionEvent";
					                		}
					                		
					                	}
					                	
					                }
					            }
					        }
					        /* Finish validation whether each component use correct name space !*/
					        
						}
						
						if (Configuration.backend.equals("MongoDB")) {
							MongoCaptureUtil m = new MongoCaptureUtil();
							m.transactionevent_capture(json4.getJSONObject(i).getJSONObject("TransactionEvent"), mongoOperation);
						}
					}
					else{
						Configuration.logger.info("Json Document is not valid. " + " It doesn't have standard event_type");
						return "Error: Json Document is not valid" + " It doesn't have standard event_type";
					}
					
				}
				if(json4.length() !=0)
					Configuration.logger.info(" EPCIS Document : Captured ");
				
	        } catch(JSONException e) {
	        	Configuration.logger.info(" Json Document is not valid " + "second_validcheck");
	        } catch(Exception e){
	        	Configuration.logger.log(Level.ERROR, e.toString());
	        }
		
		return "EPCIS Document : Captured ";
		
		}
		else{
			JSONObject json = new JSONObject(inputString);
			JSONObject json2 = json.getJSONObject("epcis");
			JSONObject json3 = json2.getJSONObject("EPCISBody");
			JSONArray json4 = json3.getJSONArray("EventList");	
				
			ApplicationContext ctx = new GenericXmlApplicationContext(
					"classpath:MongoConfig.xml");
			MongoOperations mongoOperation = (MongoOperations) ctx
					.getBean("mongoTemplate");
			
			for(int i = 0 ; i < json4.length(); i++){
				if(json4.getJSONObject(i).has("ObjectEvent") == true){
					
					if (Configuration.backend.equals("MongoDB")) {
						MongoCaptureUtil m = new MongoCaptureUtil();
						m.objectevent_capture(json4.getJSONObject(i).getJSONObject("ObjectEvent"), mongoOperation);
					}
				}
				else if(json4.getJSONObject(i).has("AggregationEvent") == true){
					if (Configuration.backend.equals("MongoDB")) {
						MongoCaptureUtil m = new MongoCaptureUtil();
						m.aggregationevent_capture(json4.getJSONObject(i).getJSONObject("AggregationEvent"), mongoOperation);
					}
				}
				else if(json4.getJSONObject(i).has("TransformationEvent") == true){
					if (Configuration.backend.equals("MongoDB")) {
						MongoCaptureUtil m = new MongoCaptureUtil();
						m.transformationevent_capture(json4.getJSONObject(i).getJSONObject("TransformationEvent"), mongoOperation);
					}
				}
				else if(json4.getJSONObject(i).has("TransactionEvent") == true){
					if (Configuration.backend.equals("MongoDB")) {
						MongoCaptureUtil m = new MongoCaptureUtil();
						m.transactionevent_capture(json4.getJSONObject(i).getJSONObject("TransactionEvent"), mongoOperation);
					}
				}
			}
			
			((AbstractApplicationContext) ctx).close();
			
			return "EPCIS Document : Captured ";
		}
	}

	static InputStream getXMLDocumentInputStream(String xmlString) {
		InputStream stream = new ByteArrayInputStream(
				xmlString.getBytes(StandardCharsets.UTF_8));
		return stream;
	}

	public static String getDataFromInputStream(ServletInputStream is)
			throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		String data = writer.toString();
		return data;
	}


	private static boolean validate(JSONObject Json, JSONObject schema_obj) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode input_node = mapper.readTree(Json.toString());
			JsonNode schema_node = mapper.readTree(schema_obj.toString());
	
	        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	        final JsonSchema schema = factory.getJsonSchema(schema_node);
	        ProcessingReport report;
	        report = schema.validate(input_node);
	        Configuration.logger.info("validation process report : "+ report);
			return report.isSuccess();
			
		} catch (IOException e) {
			Configuration.logger.log(Level.ERROR, e.toString());
			return false;
		} catch (ProcessingException e) {
			Configuration.logger.log(Level.ERROR, e.toString());
			return false;
		}
	}

}
