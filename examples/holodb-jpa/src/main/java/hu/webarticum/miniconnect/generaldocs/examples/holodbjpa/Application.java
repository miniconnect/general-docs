package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition( info = @Info( title = "REST API Demo", description = "REST API Demo powered by HoloDB (InnOtdk 2025)" ) )
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

}
