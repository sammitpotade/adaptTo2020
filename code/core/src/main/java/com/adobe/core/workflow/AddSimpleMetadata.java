package com.adobe.core.workflow;

import java.util.Collections;

import javax.jcr.Session;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.DamConstants;

@Component(
        immediate = true,
        service = { WorkflowProcess.class },
        property = { "Constants.SERVICE_DESCRIPTION=Add Metadata to uploaded Asset",
                "process.label=Simple Add Metadata Process" })
public class AddSimpleMetadata implements WorkflowProcess {
	
	 @Reference
	 ResourceResolverFactory resourceResolverFactory;

	 private final Logger logger = LoggerFactory.getLogger(getClass());
	    
	 public void execute(WorkItem witem, WorkflowSession wsession, MetaDataMap args) throws WorkflowException {
		 final WorkflowData workflowData = witem.getWorkflowData();
		 final String path = workflowData.getPayload().toString();
		 logger.debug("NewAssetMetadataProcess execute method for payload '{}' started.", path);
		 
		 ResourceResolver resourceResolver = null;
		 Session session = wsession.adaptTo(Session.class);
		 
		 try {
			resourceResolver = resourceResolverFactory.getResourceResolver(Collections
				         .<String, Object> singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session));
			
			 
			 final Resource assetResource = resourceResolver.getResource(path);
			 
			 if(assetResource != null){
	
				 final Resource metadataResource = assetResource
		                    .getChild(JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER);
				 
				 ModifiableValueMap map = metadataResource.adaptTo(ModifiableValueMap.class);
				 
				 map.put("dam:labNumber", "L736");	 
			 }
			 
			 if (resourceResolver.hasChanges()) {
	             resourceResolver.commit();
	         }
		 } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
}

