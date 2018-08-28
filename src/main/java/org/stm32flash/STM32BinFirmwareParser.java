package org.stm32flash;

import java.io.*;

public class STM32BinFirmwareParser extends STM32FirmwareParser {
    private byte[] mBuffer;

    public STM32BinFirmwareParser(String path) throws IOException {
        super(path);
        mBuffer = loadFile(mFirmwarePath);
    }

    @Override
    public byte[] parse() {
        return mBuffer;
    }
}
