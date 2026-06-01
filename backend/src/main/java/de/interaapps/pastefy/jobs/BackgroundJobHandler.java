package de.interaapps.pastefy.jobs;

import de.interaapps.pastefy.model.database.BackgroundJob;

public interface BackgroundJobHandler {
    void handle(BackgroundJob job) throws Exception;
}
