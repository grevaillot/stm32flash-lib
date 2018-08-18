import java.io.IOException;

public class STM32Device {
    private boolean mDebug = false;

    private static final byte INIT = 0x7F;
    private static final byte ACK = 0x79;
    private static final byte NACK = 0x1f;

    private static final STM32DevInfo mStm32DevInfo[] = new STM32DevInfo[] {
            /* F0 */
            new STM32DevInfo(0x440, "STM32F030x8/F05xxx", 0x20000800, 0x20002000, 0x08000000, 0x08010000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFEC00, 0x1FFFF800, 0),
            new STM32DevInfo(0x442, "STM32F030xC/F09xxx", 0x20001800, 0x20008000, 0x08000000, 0x08040000, 2,STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, STM32DevInfo.flags_t.F_OBLL.getValue()),
            new STM32DevInfo(0x444, "STM32F03xx4/6", 0x20000800, 0x20001000, 0x08000000, 0x08008000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFEC00, 0x1FFFF800, 0),
            new STM32DevInfo(0x445, "STM32F04xxx/F070x6", 0x20001800, 0x20001800, 0x08000000, 0x08008000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFC400, 0x1FFFF800, 0),
            new STM32DevInfo(0x448, "STM32F070xB/F071xx/F72xx", 0x20001800, 0x20004000, 0x08000000, 0x08020000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFC800, 0x1FFFF800, 0),
            /* F1 */
            new STM32DevInfo(0x412, "STM32F10xxx Low-density", 0x20000200, 0x20002800, 0x08000000, 0x08008000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new STM32DevInfo(0x410, "STM32F10xxx Medium-density", 0x20000200, 0x20005000, 0x08000000, 0x08020000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new STM32DevInfo(0x414, "STM32F10xxx High-density", 0x20000200, 0x20010000, 0x08000000, 0x08080000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new STM32DevInfo(0x420, "STM32F10xxx Medium-density VL", 0x20000200, 0x20002000, 0x08000000, 0x08020000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new STM32DevInfo(0x428, "STM32F10xxx High-density VL", 0x20000200, 0x20008000, 0x08000000, 0x08080000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new STM32DevInfo(0x418, "STM32F105xx/F107xx", 0x20001000, 0x20010000, 0x08000000, 0x08040000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFB000, 0x1FFFF800, 0),
            new STM32DevInfo(0x430, "STM32F10xxx XL-density", 0x20000800, 0x20018000, 0x08000000, 0x08100000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFE000, 0x1FFFF800, 0),
            /* F2 */
            new STM32DevInfo(0x411, "STM32F2xxxx", 0x20002000, 0x20020000, 0x08000000, 0x08100000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            /* F3 */
            new STM32DevInfo(0x432, "STM32F373xx/F378xx", 0x20001400, 0x20008000, 0x08000000, 0x08040000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new STM32DevInfo(0x422, "STM32F302xB(C)/F303xB(C)/F358xx", 0x20001400, 0x2000A000, 0x08000000, 0x08040000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new STM32DevInfo(0x439, "STM32F301xx/F302x4(6/8)/F318xx", 0x20001800, 0x20004000, 0x08000000, 0x08010000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new STM32DevInfo(0x438, "STM32F303x4(6/8)/F334xx/F328xx", 0x20001800, 0x20003000, 0x08000000, 0x08010000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new STM32DevInfo(0x446, "STM32F302xD(E)/F303xD(E)/F398xx", 0x20001800, 0x20010000, 0x08000000, 0x08080000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            /* F4 */
            new STM32DevInfo(0x413, "STM32F40xxx/41xxx", 0x20003000, 0x20020000, 0x08000000, 0x08100000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x419, "STM32F42xxx/43xxx", 0x20003000, 0x20030000, 0x08000000, 0x08200000, 1, STM32DevInfo.f4db, 0x1FFEC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x423, "STM32F401xB(C)", 0x20003000, 0x20010000, 0x08000000, 0x08040000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x433, "STM32F401xD(E)", 0x20003000, 0x20018000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x458, "STM32F410xx", 0x20003000, 0x20008000, 0x08000000, 0x08020000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x431, "STM32F411xx", 0x20003000, 0x20020000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x441, "STM32F412xx", 0x20003000, 0x20040000, 0x08000000, 0x08100000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x421, "STM32F446xx", 0x20003000, 0x20020000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x434, "STM32F469xx/479xx", 0x20003000, 0x20060000, 0x08000000, 0x08200000, 1, STM32DevInfo.f4db, 0x1FFEC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new STM32DevInfo(0x463, "STM32F413xx/423xx", 0x20003000, 0x20050000, 0x08000000, 0x08180000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            /* F7 */
            new STM32DevInfo(0x452, "STM32F72xxx/73xxx", 0x20004000, 0x20040000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, 0),
            new STM32DevInfo(0x449, "STM32F74xxx/75xxx", 0x20004000, 0x20050000, 0x08000000, 0x08100000, 1, STM32DevInfo.f7, 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, 0),
            new STM32DevInfo(0x451, "STM32F76xxx/77xxx", 0x20004000, 0x20080000, 0x08000000, 0x08200000, 1, STM32DevInfo.f7, 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, 0),
            /* L0 */
            new STM32DevInfo(0x425, "STM32L031xx/041xx" , 0x20001000, 0x20002000, 0x08000000, 0x08008000, 32, STM32DevInfo.p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, 0),
            new STM32DevInfo(0x417, "STM32L05xxx/06xxx" , 0x20001000, 0x20002000, 0x08000000, 0x08010000, 32, STM32DevInfo.p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, 0),
            new STM32DevInfo(0x447, "STM32L07xxx/08xxx" , 0x20002000, 0x20005000, 0x08000000, 0x08030000, 32, STM32DevInfo.p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF02000, 0),
            /* L1 */
            new STM32DevInfo(0x416, "STM32L1xxx6(8/B)" , 0x20000800, 0x20004000, 0x08000000, 0x08020000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, STM32DevInfo.flags_t.F_NO_ME.getValue()),
            new STM32DevInfo(0x429, "STM32L1xxx6(8/B)A" , 0x20001000, 0x20008000, 0x08000000, 0x08020000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, 0),
            new STM32DevInfo(0x427, "STM32L1xxxC" , 0x20001000, 0x20008000, 0x08000000, 0x08040000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF02000, 0),
            new STM32DevInfo(0x436, "STM32L1xxxD" , 0x20001000, 0x2000C000, 0x08000000, 0x08060000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8009F, 0x1FF00000, 0x1FF02000, 0),
            new STM32DevInfo(0x437, "STM32L1xxxE" , 0x20001000, 0x20014000, 0x08000000, 0x08080000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8009F, 0x1FF00000, 0x1FF02000, STM32DevInfo.flags_t.F_NO_ME.getValue()),
            /* L4 */
            new STM32DevInfo(0x415, "STM32L476xx/486xx" , 0x20003100, 0x20018000, 0x08000000, 0x08100000,  1, STM32DevInfo.p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, 0),
            /* These are not (yet) in AN2606: */
            new STM32DevInfo(0x641, "Medium_Density PL" , 0x20000200, 0x20005000, 0x08000000, 0x08020000,  4, STM32DevInfo.p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new STM32DevInfo(0x9a8, "STM32W-128K" , 0x20000200, 0x20002000, 0x08000000, 0x08020000,  4, STM32DevInfo.p_1k  , 0x08040800, 0x0804080F, 0x08040000, 0x08040800, 0),
            new STM32DevInfo(0x9b0, "STM32W-256K" , 0x20000200, 0x20004000, 0x08000000, 0x08040000,  4, STM32DevInfo.p_2k  , 0x08040800, 0x0804080F, 0x08040000, 0x08040800, 0),
    };

    private int mId;
    private int mBootloaderVersion;
    private boolean mUseExtendedErase = false;

    private final STM32UsartInterface mUsartInterface;
    private boolean mIsConnected = false;


    public STM32Device(STM32UsartInterface iface) {
        mUsartInterface = iface;
    }

    public STM32Device(STM32UsartInterface iface, boolean debug) {
        mUsartInterface = iface;
        mDebug = debug;
    }

    public boolean connect() throws IOException {
        if (!mIsConnected) {
            // stm init will return nack if already connected - dont run it twice.
            stmInit();
        }

        if (!cmdGet())
            return false;

        if (!cmdGetVersionReadProtection())
            return false;

        if (!cmdGetId())
            return false;

        STM32DevInfo i = getDevInfo(mId);
        if (i == null) {
            System.err.println("connect: could not find STM32DevInfo for id " + mId);
        } else {
            if (mDebug) {
                System.out.println("connect: found STM32DevInfo: " + i);
            }
        }

        mIsConnected = true;
        return true;
    }


    public boolean isConnected() {
        return mIsConnected;
    }

    public boolean stmInit() throws IOException {
        mUsartInterface.write(INIT);
        if (!readAck())
            System.out.println("stmInit: returned NACK, continue - init might have been already done.");
        return true;
    }

    public boolean cmdGet() throws IOException {
        if (mIsConnected)
            return false;

        if (!writeCommand(STM32Command.Get))
            return false;
        byte numByte = read();

        mBootloaderVersion = read();
        numByte--;

        if (mDebug)
            System.out.println("cmdGet: bootversion " + (mBootloaderVersion >> 4) + "." + (mBootloaderVersion & 0xf));

        while (numByte >= 0) {
            byte Command = read();
            for (STM32Command c : STM32Command.values()) {
                if (c.getCommandCode() == Command) {
                    if (mDebug)
                        System.out.println("cmdGet: " + c + " supported.");
                    if (c == STM32Command.ExtendedErase) {
                        mUseExtendedErase = true;
                    }
                    break;
                }
            }

            numByte--;
        }

        return readAck();
    }


    private boolean cmdGetVersionReadProtection() throws IOException {
        if (mIsConnected)
            return false;

        if (!writeCommand(STM32Command.GetVersionReadProtection))
            return false;
        byte bootVersion = read();
        byte option1 = read();
        byte option2 = read();

        if (mDebug) {
            System.out.println("cmdGetVersionReadProtection: bootversion " + (bootVersion >> 4) + "." + (bootVersion & 0xf));
            System.out.println("cmdGetVersionReadProtection: option " + option1 + " " + option2);
        }

        boolean ret = readAck();
        if (bootVersion != mBootloaderVersion) {
            System.err.println("cmdGetVersionReadProtection: bootversion does not match get cmd");
            return false;
        } else {
            return ret;
        }
    }

    private boolean cmdGetId() throws IOException {
        if (mIsConnected)
            return false;

        if (!writeCommand(STM32Command.GetId))
            return false;
        byte numByte = read(); // 1 on stm32.. but actually two bytes for id ?
        byte[] data = read(2);
        mId = (data[0] << 8) | data[1];

        if (mDebug)
            System.out.println("cmdGetId: id " + mId);

        return readAck();
    }

    private boolean cmdReadMemory() throws IOException {
        return false;
    }

    public boolean eraseAll() throws IOException {
        if (mIsConnected)
            return false;

        if (mUseExtendedErase) {
            return cmdExtendedEraseAll();
        }

        return cmdEraseAll();
    }

    private boolean cmdEraseAll() {
        return false;
    }

    private boolean cmdExtendedEraseAll() throws IOException {
        if (!writeCommand(STM32Command.ExtendedErase))
            return false;

        return false;
    }

    static public STM32DevInfo getDevInfo(int id) {
        for (STM32DevInfo d : mStm32DevInfo) {
            if (d.getId() == id)
                return d;
        }
        return null;
    }

    private boolean readAck() throws IOException {
        byte ack = read();

        switch (ack) {
            case ACK:
                return true;

            default:
            case NACK:
                return false;
        }
    }

    private boolean writeCommand(STM32Command command) throws IOException {
        mUsartInterface.write(command.getCommandCode());
        mUsartInterface.write(command.getCommandCodeComplement());
        return readAck();
    }

    private byte[] read(int len) throws IOException {
        return mUsartInterface.read(len);
    }

    private byte read() throws IOException {
        return mUsartInterface.read();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("STM32Device{ Id=" + Integer.toHexString(mId) +", BootloaderVersion=" + mBootloaderVersion +", Connected=" + mIsConnected);
        if (mIsConnected)
            sb.append(" " + getDevInfo(mId));
        sb.append(" }");
        return sb.toString();
    }
}
