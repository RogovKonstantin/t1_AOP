package com.bishop.synthetic_human.service;

import com.bishop.synthetic_human.audit.AuditMode;
import com.bishop.synthetic_human.audit.WeylandWatchingYou;
import com.bishop.synthetic_human.dto.CommandRequest;
import com.bishop.synthetic_human.exception.QueueOverflowException;
import com.bishop.synthetic_human.model.Priority;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class CommandService {
    private static final Logger log = LoggerFactory.getLogger(CommandService.class);

    private final ThreadPoolExecutor criticalExecutor;
    private final ThreadPoolExecutor commonExecutor;
    private final MeterRegistry registry;

    private final CommandService self;

    @Autowired
    public CommandService(MeterRegistry registry,
                          @Lazy CommandService self) {
        this.registry = registry;
        this.self     = self;

        this.criticalExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        this.commonExecutor   = new ThreadPoolExecutor(
                5, 10, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @WeylandWatchingYou(mode = AuditMode.KAFKA, kafkaTopic = "audit-topic")
    public boolean submit(CommandRequest cmd) {
        Runnable task = () -> self.doWork(cmd);

        if (cmd.getPriority() == Priority.CRITICAL) {
            criticalExecutor.execute(task);
        } else {
            try {
                commonExecutor.execute(task);
            } catch (RejectedExecutionException ex) {
                throw new QueueOverflowException("Очередь переполнена");
            }
        }
        return true;
    }

    @WeylandWatchingYou(mode = AuditMode.CONSOLE)
    public boolean doWork(CommandRequest cmd) {
        log.info("Executing work: {}", cmd.getDescription());
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        registry
                .counter("android.tasks.completed", "author", cmd.getAuthor())
                .increment();
        return true;
    }

    public ThreadPoolExecutor getCommonExecutor() {
        return commonExecutor;
    }
}
