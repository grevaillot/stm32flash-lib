import java.io.IOException;
import java.nio.ByteBuffer;

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

    public boolean connect() throws IOException {
        if (!mSTM32Device.connect()) {
            System.err.println("Could not connect to STM32 device.");
            return false;
        }
        return true;
    }

    public boolean flashFirmware(String path) throws IOException
    {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        try {
            STM32Firmware fw = new STM32Firmware(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //mSTM32Device.erase();

        return false;
    }

    public byte[] dumpFirmware() throws IOException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return null;
        }

        byte[] fw = new byte[mSTM32Device.getFlashSize()];

        if (!mSTM32Device.readFlash(fw))
            return null;

        return fw;
    }

    public boolean resetDevice() throws IOException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        // mSTM32Device.go();
        return false;
    }

    public boolean eraseFirmware() throws IOException {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }
        return mSTM32Device.eraseAll();
    }
}
