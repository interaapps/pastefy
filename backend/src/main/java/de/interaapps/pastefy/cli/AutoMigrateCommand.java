package de.interaapps.pastefy.cli;

import org.javawebstack.orm.ORM;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "automigrate",
    mixinStandardHelpOptions = true,
    description = "Automatically migrate the database to the latest version"
)
public class AutoMigrateCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("Automigrate");
        ORM.autoMigrate();
        System.out.println("Automigrated");
        return 0;
    }
}
