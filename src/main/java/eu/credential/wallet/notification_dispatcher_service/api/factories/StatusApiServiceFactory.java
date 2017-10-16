package eu.credential.wallet.notification_dispatcher_service.api.factories;

import eu.credential.wallet.notification_dispatcher_service.api.StatusApiService;
import eu.credential.wallet.notification_dispatcher_service.api.impl.StatusApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-29T16:44:14.895+02:00")
public class StatusApiServiceFactory {
    private final static StatusApiService service = new StatusApiServiceImpl();

    public static StatusApiService getStatusApi() {
        return service;
    }
}
