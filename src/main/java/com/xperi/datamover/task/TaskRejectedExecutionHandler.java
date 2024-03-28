package com.xperi.datamover.task;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/** Handle queue reject case to make sure, we put on the queue */
public class TaskRejectedExecutionHandler implements RejectedExecutionHandler {

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    try {
      executor.getQueue().put(r);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
