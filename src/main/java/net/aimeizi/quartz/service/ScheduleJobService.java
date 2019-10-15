package net.aimeizi.quartz.service;

import net.aimeizi.quartz.vo.ScheduleJobVo;

import java.util.List;

/**
 * 定时任务服务
 * @author Damon
 * @date 2019-10-15
 **/
public interface ScheduleJobService {

    /**
     * 初始化定时任务
     */
    void initScheduleJob();

    /**
     * 新增
     * @param scheduleJobVo 定时任务模型
     */
    void insert(ScheduleJobVo scheduleJobVo);

    /**
     * 直接修改 只能修改运行的时间，参数、同异步等无法修改
     * @param scheduleJobVo 定时任务模型
     */
    void update(ScheduleJobVo scheduleJobVo);

    /**
     * 删除重新创建方式
     * @param scheduleJobVo 定时任务模型
     */
    void delUpdate(ScheduleJobVo scheduleJobVo);

    /**
     * 删除
     * @param scheduleJobId 任务id
     */
    void delete(Long scheduleJobId);

    /**
     * 运行一次任务
     *
     * @param scheduleJobId the schedule job id
     */
    void runOnce(Long scheduleJobId);

    /**
     * 暂停任务
     *
     * @param scheduleJobId the schedule job id
     */
    void pauseJob(Long scheduleJobId);

    /**
     * 恢复任务
     *
     * @param scheduleJobId the schedule job id
     */
    void resumeJob(Long scheduleJobId);

    /**
     * 获取单个任务
     * @param scheduleJobId 任务id
     * @return 获取任务对象
     */
    ScheduleJobVo get(Long scheduleJobId);

    /**
     * 查询所有任务列表
     * @param scheduleJobVo 任务VO
     * @return 查询任务列表
     */
    List<ScheduleJobVo> queryList(ScheduleJobVo scheduleJobVo);

    /**
     * 获取运行中的任务列表
     * @return 运行中的任务类表
     */
    List<ScheduleJobVo> queryExecutingJobList();

}
