package com.buaa.academic.tool.util;

import com.buaa.academic.tool.translator.Translator;
import net.redhogs.cronparser.CronExpressionDescriptor;
import net.redhogs.cronparser.Options;

import java.text.ParseException;

public class NaturalCron {

    public static String describe(String cron) {
        if (cron == null)
            throw new NullPointerException("cron expression must not be null");
        try {
            String description = CronExpressionDescriptor.getDescription(cron, Options.twentyFourHour())
                    .replaceAll(",", "")
                    .replaceAll("only (in|on|at)", "every");
            if (description.matches("^At \\d\\d:\\d\\d(:\\d\\d)?$")) {
                description = description.replaceAll("^At", "Everyday");
            }
            return Translator.translate(description, "en", "zh");
        }
        catch (ParseException e) {
            throw new IllegalArgumentException("not a valid cron expression");
        }
    }

}
