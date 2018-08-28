package org.stm32flash;

import java.util.Arrays;

public class STM32DevInfo {
    static final int SZ_128 = 0x00000080;
    static final int SZ_256 = 0x00000100;
    static final int SZ_1K = 0x00000400;
    static final int SZ_2K = 0x00000800;
    static final int SZ_16K = 0x00004000;
    static final int SZ_32K = 0x00008000;
    static final int SZ_64K = 0x00010000;
    static final int SZ_128K = 0x00020000;
    static final int SZ_256K = 0x00040000;

    /* fixed size pages */
    public static final int p_128[] = { SZ_128 };
    public static final int p_256[] = { SZ_256 };
    public static final int[] p_1k = { SZ_1K };
    public static final int[] p_2k = { SZ_2K };
    /* F2 and F4 page size */
    public static final int[] f2f4 = { SZ_16K, SZ_16K, SZ_16K, SZ_16K, SZ_64K, SZ_128K };
    /* F4 dual bank page size */
    public static final int[] f4db = {	SZ_16K, SZ_16K, SZ_16K, SZ_16K, SZ_64K, SZ_128K, SZ_128K, SZ_128K, SZ_16K, SZ_16K, SZ_16K, SZ_16K, SZ_64K, SZ_128K };
    /* F7 page size */
    public static final int[] f7 = { SZ_32K, SZ_32K, SZ_32K, SZ_32K, SZ_128K, SZ_256K };

    public int getId() {
        return mId;
    }

    @Override
    public String toString() {
        return "STM32DevInfo {" +
                "Id=0x" + Integer.toHexString(mId) +
                ", Name=" + mName +
                ", RamSize=" + getRamSize() / (1024) + "kB" +
                ", FlashSize=" + getFlashSize() / (1024) + "kB" +
                ", RamStart=0x" + Integer.toHexString(mRamStart) +
                ", RamEnd=0x" + Integer.toHexString(mRamEnd )+
                ", FlashStart=0x" + Integer.toHexString(mFlashStart) +
                ", FlashEnd=0x" + Integer.toHexString(mFlashEnd) +
                ", PagesPerSector=" + mPagesPerSector +
                ", PageSize=" + Arrays.toString(mPageSize) +
                ", OptionStart=0x" + Integer.toHexString(mOptionStart) +
                ", OptionEnd=0x" + Integer.toHexString(mOptionEnd) +
                ", MemStart=0x" + Integer.toHexString(mMemStart) +
                ", MemEnd=0x" + Integer.toHexString(mMemEnd)+
                ", Flags=" + mFlags +
                '}';
    }

    public int getFlashSize() {
        return mFlashEnd - mFlashStart;
    }

    public int getRamSize() {
        return mRamEnd - mRamStart;
    }

    public int getFlashStart() {
        return mFlashStart;
    }

    public int getRamStart() {
        return mRamStart;
    }

    public enum flags_t {
        F_NO_ME( 1 << 0),	/* Mass-Erase not supported */
        F_OBLL( 1 << 1 ),	/* OBL_LAUNCH required */
        F_PEMPTY( 1 << 2),;    /* clear PEMPTY bit required */

        private final int mValue;

        flags_t(int i) {
            mValue = i;
        }

        public int getValue() {
            return mValue;
        }
    }

    private final int mId;
    private final String mName;
    private final int mRamEnd;
    private final int mRamStart;
    private final int mFlashStart;
    private final int mFlashEnd;
    private final int mPagesPerSector;
    private final int[] mPageSize;
    private final int mOptionStart;
    private final int mOptionEnd;
    private final int mMemStart;
    private final int mMemEnd;
    private final int mFlags;

    public STM32DevInfo(int id, String name, int ramStart, int ramEnd, int flashStart, int flashEnd, int pagesPerSector, int[] pageSize, int optionStart, int optionEnd, int memStart, int memEnd, int flags) {
        mId = id;
        mName = name;
        mRamStart = ramStart;
        mRamEnd = ramEnd;
        mFlashStart = flashStart;
        mFlashEnd = flashEnd;
        mPagesPerSector = pagesPerSector;
        mPageSize = pageSize;
        mOptionStart = optionStart;
        mOptionEnd = optionEnd;
        mMemStart = memStart;
        mMemEnd = memEnd;
        mFlags = flags;
    }
}
