package eu.credential.wallet.notification_dispatcher_service.api.impl;

import eu.credential.wallet.notification_dispatcher_service.api.*;
import eu.credential.wallet.notification_dispatcher_service.model.*;

import eu.credential.wallet.notification_dispatcher_service.model.Error;
import eu.credential.wallet.notification_dispatcher_service.model.StatusResponse;

import java.util.List;
import eu.credential.wallet.notification_dispatcher_service.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T16:44:14.895+02:00")
public class StatusApiServiceImpl extends StatusApiService {
    @Override
    public Response statusGet(SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
