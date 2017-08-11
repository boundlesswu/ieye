package com.vorxsoft.ieye.microservice;

import com.coreos.jetcd.Watch;

public interface WatchCallerInterface {
  public void WatchCaller(Watch.Watcher watch);
}
