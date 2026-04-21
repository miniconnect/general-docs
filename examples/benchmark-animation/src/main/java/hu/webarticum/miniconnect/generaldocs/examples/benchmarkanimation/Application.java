package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import io.micronaut.context.ApplicationContext;
import picocli.CommandLine;

public class Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext = ApplicationContext.run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(applicationContext::close));
        CommandLine commandLine = new CommandLine(applicationContext.getBean(ApplicationCommand.class));
        commandLine.execute(args);
    }

}
