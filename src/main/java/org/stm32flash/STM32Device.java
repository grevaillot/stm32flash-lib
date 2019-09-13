package org.stm32flash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.min;
import static org.stm32flash.STM32DevInfo.flags_t.F_NO_ME;
import static org.stm32flash.STM32DevInfo.flags_t.F_OBLL;

public class STM32Device {
    private boolean mDebug = false;

    private static final byte INIT = 0x7F;

    private static final byte ACK = 0x79;
    private static final byte NACK = 0x1f;

    private static final int CMD_READ_MAX_SIZE = 256;
    private static final int CMD_WRITE_MAX_SIZE = 256;
    private static final int CMD_EXTENDED_ERASE_MAX_PAGES = 512;

    enum eraseParam {
        MASS_ERASE((byte) 0xff);

        byte EraseParamValue;
        eraseParam(byte value) {
            this.EraseParamValue = value;
        }
    }

    private static final int READ_TIMEOUT_DEFAULT = 1 * 1000;
    private static final int ACK_TIMEOUT_DEFAULT = 1 * 1000;
    enum ExtendedEraseParam {
        BANK2_ERASE(0xfffd),
        BANK1_ERASE(0xfffe),
        MASS_ERASE(0xffff);

        int extendedEraseParamValue;
        ExtendedEraseParam(int value) {
            this.extendedEraseParamValue = value;
        }

        public byte[] getByteValue() {
            byte b[] = new byte[2];
            b[0] = (byte) (extendedEraseParamValue >> 8);
            b[1] = (byte) (extendedEraseParamValue & 0xff);
            return b;
        }
    }
    private static final int ACK_TIMEOUT_INIT = 3 * 1000;
    private static final int ACK_TIMEOUT_MASS_ERASE = 30 * 1000;

    private static final STM32DevInfo mStm32DevInfoList[] = new STM32DevInfo[] {
            /* F0 */
            new STM32DevInfo(0x440, "STM32F030x8/F05xxx", 0x20000800, 0x20002000, 0x08000000, 0x08010000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFEC00, 0x1FFFF800, null),
            new STM32DevInfo(0x442, "STM32F030xC/F09xxx", 0x20001800, 0x20008000, 0x08000000, 0x08040000, 2,STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, Arrays.asList(F_OBLL)),
            new STM32DevInfo(0x444, "STM32F03xx4/6", 0x20000800, 0x20001000, 0x08000000, 0x08008000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFEC00, 0x1FFFF800, null),
            new STM32DevInfo(0x445, "STM32F04xxx/F070x6", 0x20001800, 0x20001800, 0x08000000, 0x08008000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFC400, 0x1FFFF800, null),
            new STM32DevInfo(0x448, "STM32F070xB/F071xx/F72xx", 0x20001800, 0x20004000, 0x08000000, 0x08020000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFC800, 0x1FFFF800, null),
            /* F1 */
            new STM32DevInfo(0x412, "STM32F10xxx Low-density", 0x20000200, 0x20002800, 0x08000000, 0x08008000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, null),
            new STM32DevInfo(0x410, "STM32F10xxx Medium-density", 0x20000200, 0x20005000, 0x08000000, 0x08020000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, null),
            new STM32DevInfo(0x414, "STM32F10xxx High-density", 0x20000200, 0x20010000, 0x08000000, 0x08080000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, null),
            new STM32DevInfo(0x420, "STM32F10xxx Medium-density VL", 0x20000200, 0x20002000, 0x08000000, 0x08020000, 4, STM32DevInfo.p_1k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, null),
            new STM32DevInfo(0x428, "STM32F10xxx High-density VL", 0x20000200, 0x20008000, 0x08000000, 0x08080000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, null),
            new STM32DevInfo(0x418, "STM32F105xx/F107xx", 0x20001000, 0x20010000, 0x08000000, 0x08040000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFB000, 0x1FFFF800, null),
            new STM32DevInfo(0x430, "STM32F10xxx XL-density", 0x20000800, 0x20018000, 0x08000000, 0x08100000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFE000, 0x1FFFF800, null),
            /* F2 */
            new STM32DevInfo(0x411, "STM32F2xxxx", 0x20002000, 0x20020000, 0x08000000, 0x08100000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            /* F3 */
            new STM32DevInfo(0x432, "STM32F373xx/F378xx", 0x20001400, 0x20008000, 0x08000000, 0x08040000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, null),
            new STM32DevInfo(0x422, "STM32F302xB(C)/F303xB(C)/F358xx", 0x20001400, 0x2000A000, 0x08000000, 0x08040000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, null),
            new STM32DevInfo(0x439, "STM32F301xx/F302x4(6/8)/F318xx", 0x20001800, 0x20004000, 0x08000000, 0x08010000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, null),
            new STM32DevInfo(0x438, "STM32F303x4(6/8)/F334xx/F328xx", 0x20001800, 0x20003000, 0x08000000, 0x08010000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, null),
            new STM32DevInfo(0x446, "STM32F302xD(E)/F303xD(E)/F398xx", 0x20001800, 0x20010000, 0x08000000, 0x08080000, 2, STM32DevInfo.p_2k, 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, null),
            /* F4 */
            new STM32DevInfo(0x413, "STM32F40xxx/41xxx", 0x20003000, 0x20020000, 0x08000000, 0x08100000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x419, "STM32F42xxx/43xxx", 0x20003000, 0x20030000, 0x08000000, 0x08200000, 1, STM32DevInfo.f4db, 0x1FFEC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x423, "STM32F401xB(C)", 0x20003000, 0x20010000, 0x08000000, 0x08040000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x433, "STM32F401xD(E)", 0x20003000, 0x20018000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x458, "STM32F410xx", 0x20003000, 0x20008000, 0x08000000, 0x08020000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x431, "STM32F411xx", 0x20003000, 0x20020000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x441, "STM32F412xx", 0x20003000, 0x20040000, 0x08000000, 0x08100000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x421, "STM32F446xx", 0x20003000, 0x20020000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x434, "STM32F469xx/479xx", 0x20003000, 0x20060000, 0x08000000, 0x08200000, 1, STM32DevInfo.f4db, 0x1FFEC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            new STM32DevInfo(0x463, "STM32F413xx/423xx", 0x20003000, 0x20050000, 0x08000000, 0x08180000, 1, STM32DevInfo.f2f4, 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, null),
            /* F7 */
            new STM32DevInfo(0x452, "STM32F72xxx/73xxx", 0x20004000, 0x20040000, 0x08000000, 0x08080000, 1, STM32DevInfo.f2f4, 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, null),
            new STM32DevInfo(0x449, "STM32F74xxx/75xxx", 0x20004000, 0x20050000, 0x08000000, 0x08100000, 1, STM32DevInfo.f7, 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, null),
            new STM32DevInfo(0x451, "STM32F76xxx/77xxx", 0x20004000, 0x20080000, 0x08000000, 0x08200000, 1, STM32DevInfo.f7, 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, null),
            /* L0 */
            new STM32DevInfo(0x425, "STM32L031xx/041xx" , 0x20001000, 0x20002000, 0x08000000, 0x08008000, 32, STM32DevInfo.p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, null),
            new STM32DevInfo(0x417, "STM32L05xxx/06xxx" , 0x20001000, 0x20002000, 0x08000000, 0x08010000, 32, STM32DevInfo.p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, null),
            new STM32DevInfo(0x447, "STM32L07xxx/08xxx" , 0x20002000, 0x20005000, 0x08000000, 0x08030000, 32, STM32DevInfo.p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF02000,  Arrays.asList(F_NO_ME)),
            /* L1 */
            new STM32DevInfo(0x416, "STM32L1xxx6(8/B)" , 0x20000800, 0x20004000, 0x08000000, 0x08020000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, Arrays.asList(F_NO_ME)),
            new STM32DevInfo(0x429, "STM32L1xxx6(8/B)A" , 0x20001000, 0x20008000, 0x08000000, 0x08020000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, null),
            new STM32DevInfo(0x427, "STM32L1xxxC" , 0x20001000, 0x20008000, 0x08000000, 0x08040000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF02000, Arrays.asList(F_NO_ME)),
            new STM32DevInfo(0x436, "STM32L1xxxD" , 0x20001000, 0x2000C000, 0x08000000, 0x08060000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8009F, 0x1FF00000, 0x1FF02000, null),
            new STM32DevInfo(0x437, "STM32L1xxxE" , 0x20001000, 0x20014000, 0x08000000, 0x08080000, 16, STM32DevInfo.p_256 , 0x1FF80000, 0x1FF8009F, 0x1FF00000, 0x1FF02000, Arrays.asList(F_NO_ME)),
            /* L4 */
            new STM32DevInfo(0x415, "STM32L476xx/486xx" , 0x20003100, 0x20018000, 0x08000000, 0x08100000,  1, STM32DevInfo.p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, null),
            /* G0 */
            new STM32DevInfo(0x466, "STM32G03xxx/04xxx" , 0x20001000, 0x20009000, 0x08000000, 0x08020000,  1, STM32DevInfo.p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, null),
            new STM32DevInfo(0x460, "STM32G07xxx/08xxx"  , 0x20001000, 0x20009000, 0x08000000, 0x08020000,  1, STM32DevInfo.p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, null),
            /* G4 */
            new STM32DevInfo(0x468, "STM32G431xx/441xx" , 0x20004000, 0x20009000, 0x08000000, 0x08020000,  1, STM32DevInfo.p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, null),
            new STM32DevInfo(0x469, "STM32G47xxx/48xxx" , 0x20004000, 0x20009000, 0x08000000, 0x08080000,  1, STM32DevInfo.p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, null),
            /* These are not (yet) in AN2606: */
            new STM32DevInfo(0x641, "Medium_Density PL" , 0x20000200, 0x20005000, 0x08000000, 0x08020000,  4, STM32DevInfo.p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, null),
            new STM32DevInfo(0x9a8, "STM32W-128K" , 0x20000200, 0x20002000, 0x08000000, 0x08020000,  4, STM32DevInfo.p_1k  , 0x08040800, 0x0804080F, 0x08040000, 0x08040800, null),
            new STM32DevInfo(0x9b0, "STM32W-256K" , 0x20000200, 0x20004000, 0x08000000, 0x08040000,  4, STM32DevInfo.p_2k  , 0x08040800, 0x0804080F, 0x08040000, 0x08040800, null),
    };

    static public STM32DevInfo getDevInfo(int id) {
        for (STM32DevInfo d : mStm32DevInfoList) {
            if (d.getId() == id) {
                try {
                    return (STM32DevInfo) d.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
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

    /* Reset code for ARMv7-M (Cortex-M3) and ARMv6-M (Cortex-M0)
     * see ARMv7-M or ARMv6-M Architecture Reference Manual (table B3-8)
     * or "The definitive guide to the ARM Cortex-M3", section 14.4.
     */
    static final byte stm_reset_code[] = {
            (byte) 0x01, (byte) 0x49,		// ldr     r1, [pc, #4] ; (<AIRCR_OFFSET>)
            (byte) 0x02, (byte) 0x4A,		// ldr     r2, [pc, #8] ; (<AIRCR_RESET_VALUE>)
            (byte) 0x0A, (byte) 0x60,		// str     r2, [r1, #0]
            (byte) 0xfe, (byte) 0xe7,		// endless: b endless
            (byte) 0x0c, (byte) 0xed, (byte) 0x00, (byte) 0xe0,	// .word 0xe000ed0c <AIRCR_OFFSET> = NVIC AIRCR register address
            (byte) 0x04, (byte) 0x00, (byte) 0xfa, (byte) 0x05	// .word 0x05fa0004 <AIRCR_RESET_VALUE> = VECTKEY | SYSRESETREQ
    };

    private int mId = -1;
    private int mBootloaderVersion = -1;
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

    ArrayList<STM32OperationProgressListener> mListeners = new ArrayList();

    public void registerProgressListener(STM32OperationProgressListener l) {
        mListeners.add(l);
    }

    public void unregisterProgressListener(STM32OperationProgressListener l) {
        if (mListeners.contains(l))
            mListeners.remove(l);
    }

    private void progress(int current, int total) {
        for (STM32OperationProgressListener l : mListeners) {
            l.progress(current, total);
        }
    }

    private void complete(boolean success) {
        for (STM32OperationProgressListener l : mListeners) {
            l.completed(success);
        }
    }

    public void disconnect() {
        mIsConnected = false;
        mId = -1;
        mBootloaderVersion = -1;
        mUseExtendedErase = false;
        mSTM32DevInfo = null;
    }

    public boolean connect() throws IOException, TimeoutException {
        if (!mIsConnected) {
            // stm init will return nack if already connected - dont run it twice.
            // also from time to time first try fails / timeout - retry before throwing exception.
            // XXX underlying layer should throw TimeoutException instead of just IOException/Exception...
            int retry = 2;
            while (retry-- != 0) {
                try {
                    writeInit();
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

    private int getFlashAddressPage(int startAddress) {
        if (startAddress < mSTM32DevInfo.getFlashStart() || startAddress > mSTM32DevInfo.getFlashStart() + mSTM32DevInfo.getFlashSize()) {
            System.err.println("getFlashAddressPage: page is out of flash, abort.");
            throw new IllegalArgumentException("page is out of flash");
        }

        int[] pagesSizes = mSTM32DevInfo.getPagesSize();
        if (pagesSizes.length > 1)
            throw new UnsupportedOperationException("target has multiple pages size, no support yet.");

        return (startAddress - mSTM32DevInfo.getFlashStart() ) / pagesSizes[0];
    }

    public boolean eraseFlash(int startAddress, int len) throws IOException, TimeoutException {
        int[] pagesSizes = mSTM32DevInfo.getPagesSize();
        int endAddress = startAddress + len;

        if (pagesSizes.length > 1)
            throw new UnsupportedOperationException("target has multiple pages size, no support yet, use eraseAllFlash.");

        int startPage = getFlashAddressPage(startAddress);
        int endPage = getFlashAddressPage(endAddress - 1);
        int pageCount = (endPage - startPage) + 1;

        if (mDebug)
            System.out.println("eraseFlash 0x"+ Integer.toHexString(startAddress) + ":0x" + Integer.toHexString(endAddress) +  " : " + pageCount + " pages to erase. (" + startPage + ":" + endPage + ").");

        if (mUseExtendedErase) {
            int pagesToErase = pageCount;
            while (pagesToErase > 0) {
                // we need limit number of erased pages per extended erase command
                // because some boots apparently do not like massive page list..
                pageCount = Math.min(pagesToErase, CMD_EXTENDED_ERASE_MAX_PAGES);

                byte[][] pageList = new byte[pageCount][2];
                for (int i = 0; i < pageCount; i++) {
                    int page = (startPage + i);
                    if (mDebug)
                        System.out.println("adding page " + page + " to list.");
                    pageList[i][0] = (byte) (page >> 8);
                    pageList[i][1] = (byte) (page & 0xff);
                }
                if (!cmdExtendedErase(pageList))
                    return false;

                startPage += pageCount;
                pagesToErase -= pageCount;
            }
            return true;
        } else {
            byte[] pageList = new byte[pageCount];
            for (int i = startPage; i < endPage; i++) {
                pageList[i] = (byte) (i);
            }
            return cmdErase(pageList);
        }
    }

    public boolean eraseFlash(int len) throws IOException, TimeoutException {
        return eraseFlash(mSTM32DevInfo.getFlashStart(), len);
    }

    public boolean eraseAllFlash() throws IOException, TimeoutException {
        if (!mSTM32DevInfo.hasFlag(F_NO_ME)) {
            if (mUseExtendedErase)
                return cmdExtendedErase(ExtendedEraseParam.MASS_ERASE);
            return cmdErase(eraseParam.MASS_ERASE);
        } else {
            return eraseFlash(mSTM32DevInfo.getFlashStart(), mSTM32DevInfo.getFlashSize());
        }
    }

    public boolean readAllFlash(byte[] flash) throws IOException, TimeoutException {
        int count = flash.length;
        int read = 0;
        System.out.println("readAllFlash: reading " + count / 1024 + "kB");
        while (read < count) {
            int len = min(count - read, CMD_READ_MAX_SIZE);
            byte[] b = new byte[len];
            if (!cmdReadMemory(mSTM32DevInfo.getFlashStart() + read, b)) {
                System.err.println("\ncould not cmdReadMemory, abort.");
                return false;
            }
            System.arraycopy(b, 0, flash, read, len);
            read += len;
            System.out.print("\rreadAllFlash: " + (read * 100) / count + "%");
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
                complete(false);
                return false;
            }

            if (compare) {
                byte[] v = new byte[len];

                if (!cmdReadMemory(mSTM32DevInfo.getFlashStart() + written, v)) {
                    System.err.println("\ncould not cmdReadMemory, abort.");
                    complete(false);
                    return false;
                }

                if (!Arrays.equals(v, b)) {
                    System.err.println("\nCompare bad at 0x" + Integer.toHexString(mSTM32DevInfo.getFlashStart() + written) + ", abort.");
                    complete(false);
                    return false;
                }
            }

            written += len;
            System.out.print("\rwriteFlash: " + (written * 100) / count + "% ");
            progress(written, count);
        }

        System.out.println(" Done.");
        complete(true);

        return true;
    }

    public boolean reset() throws IOException, TimeoutException {
        if (!cmdWriteMemory(mSTM32DevInfo.getRamStart() + 6 * 1024, stm_reset_code))
            return false;
        return cmdGo(mSTM32DevInfo.getRamStart());
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

    private boolean cmdErase(eraseParam param) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdErase: " + param + " 0x" + Integer.toHexString(param.EraseParamValue));

        write(param.EraseParamValue);
        write((byte) ((byte) 0x00 ^ param.EraseParamValue));

        return readAck(ACK_TIMEOUT_MASS_ERASE);
    }

    private boolean cmdErase(byte[] pages) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdErase: " + pages.length + " pages.");

        if (!writeCommand(STM32Command.Erase))
            return false;

        byte checksum = (byte) (pages.length - 1);
        write((byte) (pages.length - 1));
        for (byte page : pages) {
            write(page);
            checksum ^= page;
        }
        write(checksum);

        return readAck(ACK_TIMEOUT_MASS_ERASE);
    }

    private void writePagesWithChecksum(byte pages[][]) throws IOException {
        byte checksum = 0;
        byte b[] = new byte[2];
        int len = pages.length - 1;

        b[0] = (byte) (len >> 8);
        b[1] = (byte) (len & 0xff);

        if (mDebug)
            System.out.println("Sending : " + pages.length + " pages");

        write(b);
        checksum ^= b[0];
        checksum ^= b[1];

        for (byte[] page : pages) {
            write(page);
            checksum ^= page[0];
            checksum ^= page[1];
        }
        write(checksum);
    }

    private boolean cmdExtendedErase(byte[][] pages) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdExtendedErase: " + pages.length + " pages");

        if (!writeCommand(STM32Command.ExtendedErase))
            return false;

        writePagesWithChecksum(pages);

        return readAck(ACK_TIMEOUT_MASS_ERASE);
    }

    private boolean cmdExtendedErase(ExtendedEraseParam param) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdExtendedErase: 0x" + param.extendedEraseParamValue);

        if (!writeCommand(STM32Command.ExtendedErase))
            return false;

        byte b[] = param.getByteValue();

        write(b);
        write(getChecksum(b));

        return readAck(ACK_TIMEOUT_MASS_ERASE);
    }

    private boolean cmdGo(int address) throws IOException, TimeoutException {
        if (mDebug)
            System.out.println("cmdGo: 0x" + Integer.toHexString(address));

        if (!writeCommand(STM32Command.Go))
            return false;

        return writeAddress(address);
    }

    private boolean writeInit() throws IOException, TimeoutException {
        write(INIT);
        if (!readAck(ACK_TIMEOUT_INIT))
            System.out.println("writeInit: returned NACK, continue - init might have been already done.");
        return true;
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
                System.err.println("readAck: err, got unexpected 0x" + Integer.toHexString(b & 0xff));
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

    public void setFlashSize(Integer forcedFlashSize) {
        if (mSTM32DevInfo == null)
            return;
        mSTM32DevInfo.setFlashSize(forcedFlashSize);
    }

    public int getFlashSize() {
        if (mSTM32DevInfo == null)
            return -1;
        return mSTM32DevInfo.getFlashSize();
    }

    public int getFlashStart() {
        if (mSTM32DevInfo == null)
            return -1;
        return mSTM32DevInfo.getFlashStart();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        if (mSTM32DevInfo == null)
            return "none";
        return mSTM32DevInfo.getName();
    }
}
