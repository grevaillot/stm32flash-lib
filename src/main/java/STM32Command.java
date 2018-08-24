public enum STM32Command {
    Get(0x00),
    GetVersionReadProtection(0x01),
    GetId(0x02),
    ReadMemory(0x11),
    Go(0x21),
    WriteMemory(0x31),
    Erase(0x43),
    ExtendedErase(0x44),
    WriteProtect(0x63),
    WriteUnprotect(0x73),
    ReadoutProtect(0x82),
    ReadoutUnprotect(0x92);

    private byte mCode;
    STM32Command(int c) {
        mCode = (byte)c;
    }
    byte getCommandCode() {
        return mCode;
    }
}
