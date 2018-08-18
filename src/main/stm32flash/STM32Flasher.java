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

    public void connect() throws IOException {
        if (!mSTM32Device.connect())
            System.err.println("Could not connect to STM32 device.");
        System.out.println("Connected STM32 device.");
    }

    public boolean flashFirmware(ByteBuffer fw) throws IOException
    {
        if (!mSTM32Device.isConnected()) {
            if (!mSTM32Device.connect())
                return false;
        }

        //mSTM32Device.rase();

        return false;
    }


    public STM32Device getDevice() {
        return mSTM32Device;
    }

    public void dumpFirmware() {

    }

    public void reset() throws IOException {
       // mSTM32Device.go();
    }

    public void erase() {
        mSTM32Device.eraseAll();
    }
}
