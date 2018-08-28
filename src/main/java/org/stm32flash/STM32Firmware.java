package org.stm32flash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.security.MessageDigest.getInstance;

public class STM32Firmware {
    private byte[] mBuffer;

    public STM32Firmware(String path) throws Exception {
        STM32FirmwareParser mParser;

        if (path.endsWith(".hex"))
            mParser = new STM32HexFirmwareParser(path);
        else if (path.endsWith(".bin"))
            mParser = new STM32BinFirmwareParser(path);
        else
            throw new Exception("could not find appropriate parser for " + path);

        mBuffer = mParser.parse();
    }

    public STM32Firmware(byte[] buffer) {
        mBuffer = buffer;
    }

    public byte[] getBuffer() {
        return mBuffer;
    }

    public byte[] getChecksum() {
        MessageDigest mda;

        if (mBuffer == null)
            return null;
        try {
            mda = getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        return mda.digest(mBuffer);
    }

    @Override
    public String toString() {
        return "STM32Firmware{ Size=" + getSize() + "b }";
    }

    public int getSize() {
        if (mBuffer == null)
            return -1;
        else
            return mBuffer.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        STM32Firmware that = (STM32Firmware) o;
        return Arrays.equals(mBuffer, that.mBuffer);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mBuffer);
    }
}
