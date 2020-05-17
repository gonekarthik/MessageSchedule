package com.iqvia.app.controller;

import com.iqvia.app.data.ScheduleMessageRequest;
import com.iqvia.app.data.ScheduleMessageResponse;
import com.iqvia.app.job.MessageJob;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
public class MessageSchedulerController {
    private static final Logger logger = LoggerFactory.getLogger(MessageSchedulerController.class);

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/scheduleMessage")
    public ResponseEntity<ScheduleMessageResponse> scheduleMessage(@Valid @RequestBody ScheduleMessageRequest scheduleMessageRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(scheduleMessageRequest.getDateTime(), scheduleMessageRequest.getTimeZone());
            if(dateTime.isBefore(ZonedDateTime.now())) {
                ScheduleMessageResponse ScheduleMessageResponse = new ScheduleMessageResponse(false,
                        "dateTime must be after current time");
                return ResponseEntity.badRequest().body(ScheduleMessageResponse);
            }

            JobDetail jobDetail = buildJobDetail(scheduleMessageRequest);
            Trigger trigger = buildJobTrigger(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);

            ScheduleMessageResponse ScheduleMessageResponse = new ScheduleMessageResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Message Scheduled Successfully!");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(ScheduleMessageResponse);
        } catch (SchedulerException ex) {
            logger.error("Error scheduling message", ex);

            ScheduleMessageResponse ScheduleMessageResponse = new ScheduleMessageResponse(false,
                    "Error scheduling message. Please try later!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ScheduleMessageResponse);
        }
    }

    private JobDetail buildJobDetail(ScheduleMessageRequest scheduleMessageRequest) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("message", scheduleMessageRequest.getMessage());

        return JobBuilder.newJob(MessageJob.class)
                .withIdentity(UUID.randomUUID().toString(), "message-jobs")
                .withDescription("print Message Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "message-triggers")
                .withDescription("print Message Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
