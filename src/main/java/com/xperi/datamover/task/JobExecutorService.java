package com.xperi.datamover.task;

import com.xperi.datamover.config.ThreadPoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.concurrent.*;

/** Thread pool executers - uses configured parameters including retry */
@Slf4j
public class JobExecutorService extends ThreadPoolExecutor {

  private final ThreadPoolConfig threadPoolConfig;
  private final Map<Runnable, Integer> retryTaskMap = new ConcurrentHashMap<>();

  public JobExecutorService(
      ThreadPoolConfig threadPoolConfig,
      BlockingQueue<Runnable> taskQueue,
      CustomizableThreadFactory factory,
      RejectedExecutionHandler handler) {

    super(
        threadPoolConfig.getCorePoolSize(),
        threadPoolConfig.getMaximumPoolSize(),
        threadPoolConfig.getKeepAliveTime(),
        TimeUnit.SECONDS,
        taskQueue,
        factory,
        handler);
    this.threadPoolConfig = threadPoolConfig;
  }

  /** Manage any exceptions from task, retry if pool config allows */
  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    if (t != null && shouldRetryTask(r)) {
      retryTask(r);

    } else {
      log.debug("Retry tasks {}", retryTaskMap);
      final Integer num = retryTaskMap.remove(r);
      if (num != null) {
        log.debug("Max retry was not reached for task {}", num);
      }
    }
  }

  protected boolean shouldRetryTask(Runnable r) {
    if (threadPoolConfig.getRetryCount() > 1) {
      final Integer retryCount = retryTaskMap.getOrDefault(r, 0);
      return retryCount < threadPoolConfig.getRetryCount();
    }
    return false;
  }

  /**
   * Retry a failed task, update retry count
   *
   * @param r
   */
  protected void retryTask(Runnable r) {
    log.debug("Retrying for task {}", r);
    final Integer retryCount = retryTaskMap.getOrDefault(r, 0);
    retryTaskMap.put(r, retryCount + 1);
    this.execute(r);
  }
}
