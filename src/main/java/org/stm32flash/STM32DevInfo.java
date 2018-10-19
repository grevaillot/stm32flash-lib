package org.stm32flash;

import java.util.Arrays;
import java.util.List;

public class STM32DevInfo implements Cloneable {
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
    public static final int p_1k[] = { SZ_1K };
    public static final int p_2k[] = { SZ_2K };

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

    public void setFlashSize(Integer flashSize) {
        mFlashEnd = mFlashStart + flashSize;
    }

    public int getRamSize() {
        return mRamEnd - 0x20000000;
    }

    public int getFlashStart() {
        return mFlashStart;
    }

    public int getRamStart() {
        return mRamStart;
    }

    public int[] getPagesSize() {
        return mPageSize;
    }

    public int getPagesPerSector() {
        return mPagesPerSector;
    }

    public String getName() {
        return mName;
    }

    public boolean hasFlag(flags_t f) {
        if (mFlags != null)
            return mFlags.contains(f);
        return false;
    }

    public enum flags_t {
        F_NO_ME,	/* Mass-Erase not supported */
        F_OBLL,	/* OBL_LAUNCH required */
        F_PEMPTY;    /* clear PEMPTY bit required */
    }

    private final int mId;
    private final String mName;
    private final int mRamStart;
    private int mRamEnd;
    private final int mFlashStart;
    private int mFlashEnd;
    private final int mPagesPerSector;
    private final int[] mPageSize;
    private final int mOptionStart;
    private final int mOptionEnd;
    private final int mMemStart;
    private final int mMemEnd;
    private final List mFlags;

    public STM32DevInfo(int id, String name, int ramStart, int ramEnd, int flashStart, int flashEnd, int pagesPerSector, int[] pageSize, int optionStart, int optionEnd, int memStart, int memEnd, List flags) {
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

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
