package de.interaapps.pastefy.cli;

import de.interaapps.pastefy.services.PasteService;
import org.javawebstack.orm.ORM;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "test",
    mixinStandardHelpOptions = true,
    description = "Automatically migrate the database to the latest version"
)
public class TestingCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        PasteService.getAllPastes(null);
        return 0;
    }
}
