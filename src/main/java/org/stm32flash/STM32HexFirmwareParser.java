package org.stm32flash;

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

public class STM32HexFirmwareParser extends STM32FirmwareParser {
    public STM32HexFirmwareParser(String path) {
        super(path);
        //throw new NotImplementedException();
    }

    @Override
    public byte[] parse() throws IOException {
        return null;
    }

}
