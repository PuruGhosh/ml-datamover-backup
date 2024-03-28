package com.xperi.datamover.task;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedTransferQueue;

/** Queue to manage incoming task requests */
@SuppressWarnings({"hiding", "serial"})
public class TaskQueue<Runnable> extends LinkedTransferQueue<Runnable> {
  @Override
  public boolean offer(Runnable e) {
    return tryTransfer(e);
  }
}
