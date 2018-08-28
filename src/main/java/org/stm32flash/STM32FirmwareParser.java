package org.stm32flash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class STM32FirmwareParser {
    String mFirmwarePath;

    public STM32FirmwareParser(String path) {
        this.mFirmwarePath = path;
    }

    public abstract byte[] parse() throws IOException;
    protected byte[] loadFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte buffer[] = new byte[(int) file.length()];
        fis.read(buffer);
        fis.close();
        return buffer;
    }
}
