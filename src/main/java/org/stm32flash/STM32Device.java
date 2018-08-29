package org.stm32flash;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.min;

public class STM32Device {
    private boolean mDebug = false;

    private static final byte INIT = 0x7F;

    private static final byte ACK = 0x79;
    private static final byte NACK = 0x1f;

    private static final int CMD_READ_MAX_SIZE = 256;
    private static final int CMD_WRITE_MAX_SIZE = 256;

    private static final int READ_TIMEOUT_DEFAULT = 1 * 1000;
    private static final int ACK_TIMEOUT_DEFAULT = 1 * 1000;
    private static final int ACK_TIMEOUT_MASS_ERASE = 30 * 1000;

    private static final STM32DevInfo mStm32DevInfoList[] = new STM32DevInfo[] {
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

    static public STM32DevInfo getDevInfo(int id) {
        for (STM32DevInfo d : mStm32DevInfoList) {
            if (d.getId() == id)
                return d;
        }
        return null;
    }

    enum STM32Command {
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

    private int mId;
    private int mBootloaderVersion;
    private boolean mUseExtendedErase = false;

    private STM32DevInfo mSTM32DevInfo = null;

    private final STM32UsartInterface mUsartInterface;
    private boolean mIsConnected = false;

    public STM32Device(STM32UsartInterface iface) {
        mUsartInterface = iface;
    }

    public STM32Device(STM32UsartInterface iface, boolean debug) {
        mUsartInterface = iface;
        mDebug = debug;
    }

    public boolean connect() throws IOException, TimeoutException {
        if (!mIsConnected) {
            // stm init will return nack if already connected - dont run it twice.
            // also from time to time first try fails / timeout - retry before throwing exception.
            // XXX underlying layer should throw TimeoutException instead of just IOException/Exception...
            int retry = 2;
            while (retry-- != 0) {
                try {
                    stmInit();
                    break;
                } catch (TimeoutException e) {
                    if (retry == 0)
                        throw e;
                    System.out.println("connect: retry after " + e.toString());
                }
            }
        }

        if (!cmdGet())
            return false;

        if (!cmdGetVersionReadProtection())
            return false;

        if (!cmdGetId())
            return false;

        mSTM32DevInfo = getDevInfo(mId);
        if (mSTM32DevInfo == null) {
            System.err.println("connect: could not find STM32DevInfo for id " + mId);
        } else {
            if (mDebug) {
                System.out.println("connect: found STM32DevInfo: " + mSTM32DevInfo);
            }
        }

        mIsConnected = true;
        return true;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public boolean eraseAll() throws IOException, TimeoutException {
        if (mUseExtendedErase)
            return cmdExtendedErase((byte)0xff, (byte)0xff, null);
        return cmdErase((byte)0xff, null);
    }

    public boolean readFlash(byte[] flash) throws IOException, TimeoutException {
        int count = flash.length;
        int read = 0;
        System.out.println("readFlash: reading " + count / 1024 + "kB");
        while (read < count) {
            int len = min(count - read, CMD_READ_MAX_SIZE);
            byte[] b = new byte[len];
            if (!cmdReadMemory(mSTM32DevInfo.getFlashStart() + read, b)) {
                System.out.println("\ncould not cmdReadMemory, abort.");
                return false;
            }
            System.arraycopy(b, 0, flash, read, len);
            read += len;
            System.out.print("\rreadFlash: " + (read * 100) / count + "%");
        }
        System.out.println("\ndone.");

        return true;
    }

    public boolean writeFlash(byte[] flash, boolean compare) throws IOException, TimeoutException {
        int count = flash.length;
        int written = 0;

        System.out.println("writeFlash: writing " + count / 1024 + "kB");

        while (written < count) {
            int len = min(count - written, CMD_WRITE_MAX_SIZE);

            byte[] b = new byte[len];
            System.arraycopy(flash, written, b, 0, len);

            if (!cmdWriteMemory(mSTM32DevInfo.getFlashStart() + written, b)) {
                System.err.println("\ncould not cmdWriteMemory, abort.");
                return false;
            }

            if (compare) {
                byte[] v = new byte[len];

                if (!cmdReadMemory(mSTM32DevInfo.getFlashStart() + written, v)) {
                    System.out.println("\ncould not cmdReadMemory, abort.");
                    return false;
                }

                if (!Arrays.equals(v, b)) {
                    System.err.println("\nCompare bad at 0x" + Integer.toHexString(mSTM32DevInfo.getFlashStart() + written) + ", abort.");
                    return false;
                }
            }

            written += len;
            System.out.print("\rwriteFlash: " + (written * 100) / count + "%");
        }

        return false;
    }

    private boolean stmInit() throws IOException, TimeoutException {
        write(INIT);
        if (!readAck())
            System.out.println("stmInit: returned NACK, continue - init might have been already done.");
        return true;
    }

    private boolean cmdGet() throws IOException, TimeoutException {
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

    private boolean cmdGetVersionReadProtection() throws IOException, TimeoutException {
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

    private boolean cmdGetId() throws IOException, TimeoutException {
        if (!writeCommand(STM32Command.GetId))
            return false;
        byte numByte = read(); // 1 on stm32.. but actually two bytes for id ?
        byte[] data = read(2);
        mId = (data[0] << 8) | data[1];

        if (mDebug)
            System.out.println("cmdGetId: id " + mId);

        return readAck();
    }

    private boolean cmdReadMemory(int address, byte buffer[]) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdReadMemory: " + buffer.length + "b @ 0x" + Integer.toHexString(address));

        byte len = (byte)buffer.length;

        if (buffer.length > 256)
            return false;

        if ((address & 0x3) != 0)
            return false;

        if (!writeCommand(STM32Command.ReadMemory))
            return false;

        if (!writeAddress(address))
            return false;

        write(new byte[] {(byte) (len - 1), (byte) ~(len - 1)});
        if (!readAck())
            return false;

        byte[] b = read(buffer.length);
        System.arraycopy(b, 0, buffer, 0, b.length);

        return true;
    }

    private boolean cmdWriteMemory(int address, byte buffer[]) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdWriteMemory: " + buffer.length + "b @ 0x" + Integer.toHexString(address));

        byte len = (byte)buffer.length;

        if (buffer.length > 256)
            return false;

        if ((address & 0x3) != 0)
            return false;

        if (!writeCommand(STM32Command.WriteMemory))
            return false;

        if (!writeAddress(address))
            return false;

        write((byte) (len - 1));
        write(buffer);
        write((byte) (getChecksum(buffer) ^ (byte) (len - 1)));

        return readAck();
    }

    private boolean cmdErase(byte pageCount, byte[] pages) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdErase: 0x" + Integer.toHexString(pageCount));

        if (!writeCommand(STM32Command.Erase))
            return false;

        write(pageCount);

        if (pageCount == (byte)0xff) {
            // Full erase.
            // XXX not tested
            write((byte) 0x00);
        } else {
            // XXX not tested
            byte checksum = pageCount;
            for (byte page : pages) {
                write(page);
                checksum ^= page;
            }
            write(checksum);
        }

        return readAck();
    }

    private boolean cmdExtendedErase(byte msb, byte lsb, byte[][] pages) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdExtendedErase: 0x" + Integer.toHexString((msb << 8 | lsb) & 0xffff));

        if (!writeCommand(STM32Command.ExtendedErase))
            return false;

        if (msb == (byte)0xff) {
            // Full/Bank erase.
            write(msb);
            write(lsb);
            write((byte) (msb ^ lsb));
        } else {
            // XXX not tested
            byte checksum = msb;
            checksum ^= lsb;
            for (byte[] page : pages) {
                write(page[0]);
                write(page[1]);
                checksum ^= page[0];
                checksum ^= page[1];
            }
            write(checksum);
        }
        return readAck(ACK_TIMEOUT_MASS_ERASE);
    }

    }

    private boolean readAck() throws IOException, TimeoutException {
        return readAck(ACK_TIMEOUT_DEFAULT);
    }

    private boolean readAck(int timeout) throws IOException, TimeoutException {
        byte b;
        switch (b = readWithTimeout(timeout)) {
            case ACK:
                return true;

            case NACK:
                return false;

            default:
                System.err.println("readAck: err, got unexpected 0x" + Integer.toHexString(b));
                return false;
        }
    }

    private boolean writeCommand(STM32Command command) throws IOException, TimeoutException {
        write(new byte[] { command.getCommandCode(), (byte) ~command.getCommandCode()});
        return readAck();
    }

    private boolean writeAddress(int address) throws IOException, TimeoutException {
        byte[] buf = new byte[4];
        buf[0] = (byte) ((address >> 24) & 0xff);
        buf[1] = (byte) ((address >> 16) & 0xff);
        buf[2] = (byte) ((address >> 8) & 0xff);
        buf[3] = (byte) ((address) & 0xff);
        write(buf);
        write(getChecksum(buf));

        return readAck();
    }

    private byte getChecksum(byte[] buffer) {
        byte cs = 0;
        for (byte b : buffer)
            cs ^= b;
        return cs;
    }

    private void write(byte b) throws IOException {
        mUsartInterface.write(new byte[] {b});
    }

    private void write(byte[] b) throws IOException {
        mUsartInterface.write(b);
    }

    private byte read() throws IOException, TimeoutException {
        return mUsartInterface.read(1, READ_TIMEOUT_DEFAULT)[0];
    }

    private byte readWithTimeout(int timeout) throws IOException, TimeoutException {
        return mUsartInterface.read(1, timeout)[0];
    }

    private byte[] read(int len) throws IOException, TimeoutException {
        return mUsartInterface.read(len, READ_TIMEOUT_DEFAULT);
    }

    private byte[] readWithTimeout(int len, int timeout) throws IOException, TimeoutException {
        return mUsartInterface.read(len, timeout);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("STM32Device{ Id=" + Integer.toHexString(mId) +", BootloaderVersion=" + mBootloaderVersion +", Connected=" + mIsConnected);
        if (mSTM32DevInfo != null)
            sb.append(" " + mSTM32DevInfo);
        sb.append(" }");
        return sb.toString();
    }

    public int getFlashSize() {
        if (mSTM32DevInfo == null)
            return -1;
        return mSTM32DevInfo.getFlashSize();
    }
}
