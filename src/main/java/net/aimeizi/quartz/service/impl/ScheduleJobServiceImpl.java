package net.aimeizi.quartz.service.impl;

import com.dexcoder.commons.bean.BeanConverter;
import com.dexcoder.dal.JdbcDao;
import com.dexcoder.dal.build.Criteria;
import net.aimeizi.quartz.model.ScheduleJob;
import net.aimeizi.quartz.service.ScheduleJobService;
import net.aimeizi.quartz.utils.ScheduleUtils;
import net.aimeizi.quartz.vo.ScheduleJobVo;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Damon
 */
@Service
public class ScheduleJobServiceImpl implements ScheduleJobService {

    /** 调度工厂Bean */
    @Autowired
    private Scheduler scheduler;

    /** 通用dao */
    @Autowired
    private JdbcDao jdbcDao;

    @Override
    public void initScheduleJob() {
        List<ScheduleJob> scheduleJobList = jdbcDao.queryList(Criteria.select(ScheduleJob.class).where("status",new Object[]{"1"}));
        if (CollectionUtils.isEmpty(scheduleJobList)) {
            return;
        }
        for (ScheduleJob scheduleJob : scheduleJobList) {

            CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup());

            //不存在，创建一个
            if (cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            }
        }
    }

    @Override
    public void insert(ScheduleJobVo scheduleJobVo) {
        ScheduleJob scheduleJob = scheduleJobVo.getTargetObject(ScheduleJob.class);
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
        jdbcDao.insert(scheduleJob);
    }

    @Override
    public void delUpdate(ScheduleJobVo scheduleJobVo) {
        ScheduleJob scheduleJob = scheduleJobVo.getTargetObject(ScheduleJob.class);
        //先删除
        ScheduleUtils.deleteScheduleJob(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup());
        //再创建
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
        //数据库直接更新即可
        jdbcDao.update(scheduleJob);
    }

    @Override
    public void delete(ScheduleJobVo scheduleJobVo) {
        Long scheduleJobId = scheduleJobVo.getScheduleJobId();
        ScheduleJob scheduleJob = jdbcDao.get(ScheduleJob.class, scheduleJobId);
        //删除运行的任务
        ScheduleUtils.deleteScheduleJob(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup());
        //删除数据
        scheduleJob.setStatus("0");
        jdbcDao.update(scheduleJob);
    }

    @Override
    public void runOnce(Long scheduleJobId) {
        ScheduleJob scheduleJob = jdbcDao.get(ScheduleJob.class, scheduleJobId);
        ScheduleUtils.runOnce(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup());
    }

    @Override
    public void pauseJob(Long scheduleJobId) {
        ScheduleJob scheduleJob = jdbcDao.get(ScheduleJob.class, scheduleJobId);
        ScheduleUtils.pauseJob(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup());
        //演示数据库就不更新了
    }

    @Override
    public void resumeJob(Long scheduleJobId) {
        ScheduleJob scheduleJob = jdbcDao.get(ScheduleJob.class, scheduleJobId);
        ScheduleUtils.resumeJob(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup());
        //演示数据库就不更新了
    }

    @Override
    public ScheduleJobVo get(Long scheduleJobId) {
        ScheduleJob scheduleJob = jdbcDao.get(ScheduleJob.class, scheduleJobId);
        return scheduleJob.getTargetObject(ScheduleJobVo.class);
    }

    @Override
    public List<ScheduleJobVo> queryList(ScheduleJobVo scheduleJobVo) {
        List<ScheduleJob> scheduleJobs = jdbcDao.queryList(Criteria.select(ScheduleJob.class).where("status",new Object[]{"1"}).desc("description"));
        List<ScheduleJobVo> scheduleJobVoList = BeanConverter.convert(ScheduleJobVo.class, scheduleJobs);
        try {
            for (ScheduleJobVo vo : scheduleJobVoList) {

                JobKey jobKey = ScheduleUtils.getJobKey(vo.getJobName(), vo.getJobGroup());
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                if (CollectionUtils.isEmpty(triggers)) {
                    continue;
                }

                //这里一个任务可以有多个触发器， 但是我们一个任务对应一个触发器，所以只取第一个即可，清晰明了
                Trigger trigger = triggers.iterator().next();
                vo.setJobTrigger(trigger.getKey().getName());

                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                vo.setStatus(triggerState.name());

                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    vo.setCronExpression(cronExpression);
                }
            }
        } catch (SchedulerException e) {
            //演示用，就不处理了
        }
        return scheduleJobVoList;
    }

    /**
     * @return 获取运行中的job列表
     */
    @Override
    public List<ScheduleJobVo> queryExecutingJobList() {
        try {
            // 存放结果集
            List<ScheduleJobVo> jobList = new ArrayList<ScheduleJobVo>();

            // 获取scheduler中的JobGroupName
            for (String group:scheduler.getJobGroupNames()){
                // 获取JobKey 循环遍历JobKey
                for(JobKey jobKey:scheduler.getJobKeys(GroupMatcher.groupEquals(group))){
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    JobDataMap jobDataMap = jobDetail.getJobDataMap();
                    ScheduleJob scheduleJob = (ScheduleJob)jobDataMap.get(ScheduleJobVo.JOB_PARAM_KEY);
                    ScheduleJobVo scheduleJobVo = new ScheduleJobVo();
                    BeanConverter.convert(scheduleJobVo,scheduleJob);
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    Trigger trigger = triggers.iterator().next();
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    scheduleJobVo.setJobTrigger(trigger.getKey().getName());
                    scheduleJobVo.setStatus(triggerState.name());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        scheduleJobVo.setCronExpression(cronExpression);
                    }
                    // 获取正常运行的任务列表
                    if(triggerState.name().equals("NORMAL")){
                        jobList.add(scheduleJobVo);
                    }
                }
            }

            /* 非集群环境获取正在执行的任务列表 */
            /*List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
            List<ScheduleJobVo> jobList = new ArrayList<ScheduleJobVo>(executingJobs.size());
            for (JobExecutionContext executingJob : executingJobs) {
                ScheduleJobVo job = new ScheduleJobVo();
                JobDetail jobDetail = executingJob.getJobDetail();
                JobKey jobKey = jobDetail.getKey();
                Trigger trigger = executingJob.getTrigger();
                job.setJobName(jobKey.getName());
                job.setJobGroup(jobKey.getGroup());
                job.setJobTrigger(trigger.getKey().getName());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setStatus(triggerState.name());
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    job.setCronExpression(cronExpression);
                }
                jobList.add(job);
            }*/

            return jobList;
        } catch (SchedulerException e) {
            return null;
        }

    }
}
