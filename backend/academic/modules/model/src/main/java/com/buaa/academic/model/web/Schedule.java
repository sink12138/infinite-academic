package com.buaa.academic.model.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "定时任务的描述模型")
public class Schedule {

    @ApiModelProperty(value = "任务名称", example = "数据更新")
    private String name;

    @ApiModelProperty(value = "运行状态，true代表正在运行", example = "true")
    private boolean running;

    @ApiModelProperty(value = "预定的运行频率", example = "每天")
    private String frequency;

    @JsonSerialize(using = DateFormatSerializer.class)
    @ApiModelProperty(value = "上次运行时间", example = "2021-01-14 05:14")
    private Date lastRun;

    @JsonSerialize(using = DateFormatSerializer.class)
    @ApiModelProperty(value = "下次运行时间", example = "2021-08-17 19:26")
    private Date nextRun;

    @JsonSerialize(using = TaskListSerializer.class)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ApiModelProperty(value = "所有正在运行的子任务信息。<b>注意：这个字段是一个列表</b>")
    private Map<String, Task> tasks = new LinkedHashMap<>();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public Schedule addTask(Task task) {
        this.tasks.put(task.getName(), task);
        return this;
    }

    public Schedule removeTask(String taskName) {
        this.tasks.remove(taskName);
        return this;
    }

    private static class DateFormatSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(Schedule.sdf.format(date));
        }

    }

    private static class TaskListSerializer extends JsonSerializer<LinkedHashMap<String, Task>> {

        @Override
        public void serialize(LinkedHashMap<String, Task> taskMap, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (Task task : taskMap.values()) {
                generator.writeStartObject();
                generator.writeStringField("name", task.getName());
                generator.writeStringField("status", task.getStatus());
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }

    }

}
