package com.innospots.nexus.core.quartz;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzScheduleManagerTest {

    private QuartzScheduleManager manager;

    @AfterEach
    void shutdown() {
        if (manager != null) {
            manager.shutdown();
        }
    }

    @Test
    void schedulesUpdatesPausesResumesAndDeletesCronJob() {
        manager = new QuartzScheduleManager("test-cron-scheduler", 2);
        manager.startup();

        QuartzJobRequest request = QuartzJobRequest.cron(
                "sync-customers",
                DemoJob.class,
                "0/5 * * * * ?",
                Map.of("tenant", "nexus")
        );

        assertThat(manager.refreshJob(request)).isTrue();
        assertThat(manager.hasJob("sync-customers")).isTrue();
        assertThat(manager.scheduleJobs()).containsExactly("sync-customers");
        assertThat(manager.schedulerInfo()).singleElement()
                .satisfies(info -> {
                    assertThat(info.jobName()).isEqualTo("sync-customers");
                    assertThat(info.jobClass()).isEqualTo(DemoJob.class.getName());
                    assertThat(info.dataMap()).containsEntry("tenant", "nexus");
                    assertThat(info.triggers()).hasSize(1);
                });

        assertThat(manager.pauseJob("sync-customers")).isTrue();
        assertThat(manager.resumeJob("sync-customers")).isTrue();
        assertThat(manager.latestUpdateTime()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(manager.deleteJob("sync-customers")).isTrue();
        assertThat(manager.hasJob("sync-customers")).isFalse();
    }

    @Test
    void rejectsInvalidCronExpression() {
        manager = new QuartzScheduleManager("test-invalid-cron-scheduler", 1);
        manager.startup();

        QuartzJobRequest request = QuartzJobRequest.cron("bad-job", DemoJob.class, "bad cron", Map.of());

        assertThat(manager.refreshJob(request)).isFalse();
        assertThat(manager.hasJob("bad-job")).isFalse();
    }

    @Test
    void schedulesOnceJobWithStartTime() {
        manager = new QuartzScheduleManager("test-once-scheduler", 1);
        manager.startup();
        Date startAt = Date.from(LocalDateTime.now().plusMinutes(5)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        QuartzJobRequest request = QuartzJobRequest.once("once-job", DemoJob.class, startAt, Map.of());

        assertThat(manager.refreshJob(request)).isTrue();
        assertThat(manager.schedulerInfo()).singleElement()
                .satisfies(info -> assertThat(info.triggers().getFirst().startTime()).isEqualTo(startAt));
    }

    public static class DemoJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            // Test job marker.
        }
    }
}
