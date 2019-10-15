package net.aimeizi.quartz.event;

import javax.annotation.PostConstruct;

import net.aimeizi.quartz.service.ScheduleJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时任务初始化
 * @author Damon
 * @date 2019-10-15
 **/
@Component
public class ScheduleJobInit {

    private static final Logger log = LoggerFactory.getLogger(ScheduleJobInit.class);

    /** 定时任务service */
    @Autowired
    private ScheduleJobService scheduleJobService;

    /**
     * 项目启动时初始化
     */
    @PostConstruct
    public void init() {

        if (log.isInfoEnabled()) {
            log.info("init");
        }

        scheduleJobService.initScheduleJob();

        if (log.isInfoEnabled()) {
            log.info("end");
        }
    }

}
