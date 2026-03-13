package de.interaapps.pastefy.cli;


import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.elastic.ElasticPaste;
import de.interaapps.pastefy.model.minio.MinioPaste;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.Repo;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@CommandLine.Command(
        name = "movesmallfilestodb",
        mixinStandardHelpOptions = true,
        description = "Automatically moves small S3 files back to DB."
)
public class MoveSmallFilesToDBCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-s", "--size"}, description = "Batch size")
    private int batchSize = 40;

    @CommandLine.Option(names = {"-i", "--iterations"}, description = "Iterations")
    private int iterations = 10000;

    @CommandLine.Option(names = {"-t", "--threads"}, description = "Number of threads")
    private int threadCount = 16;

    @CommandLine.Option(names = {"-o", "--offset"}, description = "Offset")
    private int offset = 0;

    @Override
    public Integer call() throws Exception {
        System.out.println("syncing");
        final int size = this.batchSize;
        final int iterations = this.iterations;
        final int threadCount = 16;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AtomicInteger syncedCount = new AtomicInteger(0);
        AtomicInteger syncedOffCount = new AtomicInteger(0);

        for (int i = 0; i < iterations; i++) {
            int finalI = i;
            executor.submit(() -> {
                System.out.println("iteration " + (finalI + 1) + "/" + iterations);
                Repo.get(Paste.class).query()
                        .whereNull("userId")
                        .where("storageType", Paste.StorageType.S3)
                        .limit(size)
                        .offset(finalI * size + offset)
                        .all()
                        .forEach(p -> {
                            String content = p.getContent();
                            System.out.println(p.getKey());
                            if (content.length() < 2000) {
                                try {
                                    MinioPaste.delete(p);

                                    p.setContent(content);
                                    p.superSave();
                                    syncedOffCount.incrementAndGet();
                                } catch (Exception e) {
                                   e.printStackTrace();
                                }
                            }
                        });
                for (int i1 = 0; i1 < size; i1++) {
                    syncedCount.incrementAndGet();
                }
                System.out.println("iteration " + (finalI + 1) + "/" + iterations + " done ("+syncedCount.get()+"/"+ (iterations * size) + ")");
                System.out.println("New offset: "+(syncedCount.get()-syncedOffCount.get()));
            });
        }



        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.HOURS);
        System.out.println("done");
        return 0;
    }
}
