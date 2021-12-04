package com.buaa.academic.model.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.IOException;
import java.text.ParseException;
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
    @JsonDeserialize(using = DateFormatDeserializer.class)
    @ApiModelProperty(value = "上次运行时间", example = "2021-01-14 05:14")
    private Date lastRun;

    @JsonSerialize(using = DateFormatSerializer.class)
    @JsonDeserialize(using = DateFormatDeserializer.class)
    @ApiModelProperty(value = "下次运行时间", example = "2021-08-17 19:26")
    private Date nextRun;

    @JsonSerialize(using = TaskListSerializer.class)
    @JsonDeserialize(using = TaskListDeserializer.class)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ApiModelProperty(value = "所有子任务的状态信息。<b>注意：这个字段是一个列表</b>")
    private Map<String, Task> tasks = new LinkedHashMap<>();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @SuppressWarnings("UnusedReturnValue")
    public Schedule addTask(Task task) {
        this.tasks.put(task.getName(), task);
        return this;
    }

    public Schedule removeTask(String taskName) {
        this.tasks.remove(taskName);
        return this;
    }

    public Schedule clearTasks() {
        this.tasks.clear();
        return this;
    }

    private static class DateFormatSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(Schedule.sdf.format(date));
        }

    }

    private static class DateFormatDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String dateFormat = parser.getValueAsString();
            try {
                return Schedule.sdf.parse(dateFormat);
            }
            catch (ParseException e) {
                e.printStackTrace();
                throw new IOException();
            }
        }

    }

    private static class TaskListSerializer extends JsonSerializer<Map<String, Task>> {

        @Override
        public void serialize(Map<String, Task> taskMap, JsonGenerator generator, SerializerProvider provider) throws IOException {
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

    private static class TaskListDeserializer extends JsonDeserializer<Map<String, Task>> {

        @Override
        public Map<String, Task> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            Map<String, Task> taskMap = new LinkedHashMap<>();
            Task[] tasksArray = parser.readValueAs(Task[].class);
            for (Task task : tasksArray) {
                taskMap.put(task.getName(), task);
            }
            return taskMap;
        }

    }

}
