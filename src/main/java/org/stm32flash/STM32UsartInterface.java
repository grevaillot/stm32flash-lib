package org.stm32flash;

import java.io.IOException;

public abstract class STM32UsartInterface {
    abstract public byte read() throws IOException;
    abstract public byte[] read(int count) throws IOException;
    protected abstract void write(byte b) throws IOException;
    protected abstract void write(byte[] b) throws IOException;
}
