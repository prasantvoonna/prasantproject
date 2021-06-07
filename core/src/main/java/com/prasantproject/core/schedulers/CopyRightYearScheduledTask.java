package com.prasantproject.core.schedulers;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.ContentVariation;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sling.api.resource.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple demo for cron-job like tasks that get executed regularly.
 * It also demonstrates how property values can be set. Users can
 * set the property values in /system/console/configMgr
 */
@Designate(ocd= CopyRightYearScheduledTask.Config.class)
@Service
@Component(service=Runnable.class)
public class CopyRightYearScheduledTask implements Runnable {

    @ObjectClassDefinition(name="AA Copy Right Year Change Scheduled Task",
                           description = "A cron-job to change Copy Right Year on Specific day")
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression for Copy Right Year Change")
        String scheduler_expression() default "0 0/1 * * * ?";

        @AttributeDefinition(name = "Concurrent task",
                             description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String myParameter;
    
    @Override
    public void run() {
        logger.debug("CopyRightYearScheduledTask is now running, myParameter='{}'", myParameter);
    }

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Activate
    protected void activate() throws ContentFragmentException, LoginException {
        logger.debug("CopyRightYearScheduledTask is now running ");

        Map<String, Object> param = new HashMap<String, Object>();
        param.put(ResourceResolverFactory.SUBSERVICE, "writeService");
        ResourceResolver resolver = null;

        resolver = resolverFactory.getServiceResourceResolver(param);
        logger.debug(resolver.getUserID());

        //Get the resource of content fragment as below.
        Resource fragmentResource = resolver.getResource("/content/dam/prasantproject/copyrightyearCF");

        //Adapt it to a fragment resource
        if (fragmentResource != null) {
            ContentFragment fragment = fragmentResource.adaptTo(ContentFragment.class);
            // the resource is now accessible through the API
            ContentElement title = fragment.getElement("CopyRightYear");
            ContentVariation masterVariation = title.getVariation("master");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy");
            LocalDateTime now = LocalDateTime.now();

            masterVariation.setContent(dtf.format(now), "text/plain");
            myParameter = "Value Set To" + masterVariation.getContent();
        }else {
            myParameter = "Value Not Set";
        }
    }

}
