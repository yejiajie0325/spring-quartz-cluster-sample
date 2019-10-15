package net.aimeizi.quartz.quartz;

import net.aimeizi.quartz.model.ScheduleJob;
import net.aimeizi.quartz.vo.ScheduleJobVo;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 异步任务工厂
 * @author Damon
 * @date 2019-10-15
 **/
public class AsyncJobFactory extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(AsyncJobFactory.class);
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("AsyncJobFactory execute");
        ScheduleJob scheduleJob = (ScheduleJob) context.getMergedJobDataMap().get(ScheduleJobVo.JOB_PARAM_KEY);
        log.info(scheduleJob.getJobName());
    }
}
