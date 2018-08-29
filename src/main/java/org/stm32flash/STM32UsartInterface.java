package org.stm32flash;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class STM32UsartInterface {
    public abstract byte[] read(int len, int timeout) throws IOException, TimeoutException;
    public abstract void write(byte[] b) throws IOException;
}
