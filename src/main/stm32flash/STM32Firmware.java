class STM32Firmware {
    private final STM32FirmwareParser mParser;

    STM32Firmware(String path) throws Exception {
        if (path.endsWith(".hex"))
            mParser = new STM32HexFirmwareParser(path);
        else
            throw new Exception("could not find appropriate parser for " + path);
    }
}
