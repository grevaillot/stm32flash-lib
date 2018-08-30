package org.stm32flash;

public interface STM32OperationProgressListener {
    public void completed(boolean successfull);
    public void progress(long current, long total);
}
