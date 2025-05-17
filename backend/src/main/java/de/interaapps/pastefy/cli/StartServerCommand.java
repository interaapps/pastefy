package de.interaapps.pastefy.cli;

import de.interaapps.pastefy.Pastefy;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "start",
    mixinStandardHelpOptions = true,
    description = "Automatically migrate the database to the latest version"
)
public class StartServerCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("Start server");
        Pastefy instance = Pastefy.getInstance();
        instance.setupServer();
        instance.start();

        // Do not exit
        return 0;
    }
}
