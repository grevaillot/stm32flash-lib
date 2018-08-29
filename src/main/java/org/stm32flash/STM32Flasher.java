package org.stm32flash;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class STM32Flasher {
    private boolean mDebug = false;
    private STM32Device mSTM32Device;

    public STM32Flasher(STM32UsartInterface iface, boolean debug) {
        mSTM32Device = new STM32Device(iface);
        mDebug = debug;
    }

    public STM32Flasher(STM32UsartInterface iface) {
        mSTM32Device = new STM32Device(iface, mDebug);
    }

    public STM32Device getDevice() {
        return mSTM32Device;
    }

    public boolean connect() throws IOException, TimeoutException {
        if (!mSTM32Device.connect()) {
            System.err.println("Could not connect to STM32 device.");
            return false;
        }
        return true;
    }

    public boolean flashFirmware(byte fw[], boolean erase) throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        if (erase) {
            if (!mSTM32Device.eraseAll())
                return false;
        }

        mDebug = true;

        return mSTM32Device.writeFlash(fw, true);
    }

    public boolean flashFirmware(byte fw[]) throws IOException, TimeoutException {
        return flashFirmware(fw, true);
    }

    public boolean flashFirmware(String path) throws IOException, TimeoutException {
        STM32Firmware fw;

        try {
            fw = new STM32Firmware(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return flashFirmware(fw.getBuffer(), true);
    }

    public byte[] dumpFirmware() throws IOException, TimeoutException {
        return dumpFirmware(mSTM32Device.getFlashSize());
    }

    public byte[] dumpFirmware(int size) throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return null;
        }

        byte[] fw = new byte[size];

        if (!mSTM32Device.readFlash(fw))
            return null;

        return fw;
    }

    public boolean eraseFirmware() throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }
        return mSTM32Device.eraseAll();
    }

    public boolean resetDevice() throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        // mSTM32Device.cmGo();

        return false;
    }
}
