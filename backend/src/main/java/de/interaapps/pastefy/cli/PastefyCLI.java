package de.interaapps.pastefy.cli;

import picocli.CommandLine;

@CommandLine.Command(
    name = "pastefy",
    mixinStandardHelpOptions = true,
    version = "Pastefy CLI",
    description = "Command line interface for Pastefy",
        subcommands = {
            AutoMigrateCommand.class,
            StartServerCommand.class,
            AutoMigrateElasticCommand.class,
            SyncToElasticCommand.class,
            SyncToMinioCommand.class,
            TestingCommand.class
        }
)
public class PastefyCLI implements Runnable {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
