package com.buaa.academic.admin.client;

import com.buaa.academic.model.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
public abstract class FeignOperation<D> extends Thread {

    private static int count;

    private final String tag;

    private Result<D> result;

    public FeignOperation(String tag) {
        super(String.format("feign-thread-%02x", (count++) % 0xff));
        count %= 0xff;
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public Result<D> getResult() {
        return this.result;
    }

    @Override
    public void run() {
        try {
            this.result = apply();
        }
        catch (Exception e) {
            log.warn("Failed to apply FeignOperation '" + this.getTag() + '\'');
            LoggerFactory.getLogger(e.getClass()).warn(e.getMessage());
        }
    }

    public abstract Result<D> apply();

}
