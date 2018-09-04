package org.stm32flash;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class STM32Flasher {
    private boolean mDebug = false;
    private STM32Device mSTM32Device;

    public STM32Flasher(STM32UsartInterface iface, boolean debug) {
        mSTM32Device = new STM32Device(iface, debug);
        mDebug = debug;
    }

    public STM32Flasher(STM32UsartInterface iface) {
        mSTM32Device = new STM32Device(iface, mDebug);
    }

    public STM32Device getDevice() {
        return mSTM32Device;
    }

    public void registerProgressListener(STM32OperationProgressListener l) {
        mSTM32Device.registerProgressListener(l);
    }

    public void unregisterProgressListener(STM32OperationProgressListener l) {
        mSTM32Device.unregisterProgressListener(l);
    }

    public boolean connect() throws IOException, TimeoutException {
        if (!mSTM32Device.connect()) {
            System.err.println("Could not connect to STM32 device.");
            return false;
        }
        return true;
    }

    public enum EraseMode {
        Partial,
        Full,
    }

    public boolean flashFirmware(byte fw[], EraseMode erase, boolean verify) throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        switch (erase) {
            case Partial:
                if (!mSTM32Device.eraseFlash(fw.length))
                    return false;
                break;

            case Full:
                if (!mSTM32Device.eraseAllFlash())
                    return false;
                break;
        }

        return mSTM32Device.writeFlash(fw, verify);
    }

    public boolean flashFirmware(byte fw[]) throws IOException, TimeoutException {
        return flashFirmware(fw, EraseMode.Full, true);
    }

    public boolean flashFirmware(String path) throws IOException, TimeoutException {
        STM32Firmware fw;

        try {
            fw = new STM32Firmware(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return flashFirmware(fw.getBuffer(), EraseMode.Full, true);
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

        if (!mSTM32Device.readAllFlash(fw))
            return null;

        return fw;
    }

    public boolean eraseFirmware() throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }
        return mSTM32Device.eraseAllFlash();
    }

    public boolean erase(int startAddress, int length) throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }
        return mSTM32Device.eraseFlash(startAddress, length);
    }

    public boolean resetDevice() throws IOException, TimeoutException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        mSTM32Device.runFlash();

        return false;
    }
}
