package com.bishop.synthetic_human.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private KafkaTemplate<String, String> kafka;

    @Around("@annotation(watch)")
    public Object auditAny(ProceedingJoinPoint pjp, WeylandWatchingYou watch) throws Throwable {
        String method = pjp.getSignature().toShortString();
        Object[] args = pjp.getArgs();

        if (watch.mode() == AuditMode.KAFKA && watch.kafkaTopic().isBlank()) {
            throw new IllegalStateException(
                    "Для AuditMode.KAFKA в @WeylandWatchingYou нужно указать kafkaTopic"//можно сделать annotation processor чтобы не компилировалось без топика
            );
        }

        String resultStr;
        try {
            Object ret = pjp.proceed();
            resultStr = "OK";
            send(watch, method, args, resultStr);
            return ret;
        } catch (Throwable ex) {
            resultStr = ex.getMessage();
            send(watch, method, args, resultStr);
            throw ex;
        }
    }

    private void send(WeylandWatchingYou watch, String method,
                      Object[] args, String result) {
        String msg = String.format(
                "Method=%s; args=%s; result=%s",
                method, Arrays.toString(args), result
        );

        if (watch.mode() == AuditMode.CONSOLE) {
            System.out.println("[AUDIT] " + msg);
        }

        if (watch.mode() == AuditMode.KAFKA) {
            kafka.send(watch.kafkaTopic(), msg);
        }
    }
}
