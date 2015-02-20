package example.walker.blue.beacon.lib.glass;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import walker.blue.beacon.lib.beacon.Beacon;
import walker.blue.beacon.lib.beacon.BluetoothDeviceToBeacon;
import walker.blue.beacon.lib.client.BeaconClientBuilder;
import walker.blue.beacon.lib.client.BeaconScanClient;
import walker.blue.beacon.lib.service.ScanEndUserCallback;

/**
 * Main Activity of the application
 */
public class MainActivity extends Activity {

    /**
     * Amount of time the client will scan for Beacons
     */
    private static final int SCAN_INTERVAL = 10000;
    /**
     * Timeout period for the wakelock
     */
    private static final int WAKELOCK_TIMEOUT = 15000;
    /**
     * Tag for the wakelock
     */
    private static final String WAKELOCK_TAG = "BeaconLibExample";

    /**
     * Adapter which keeps track of the cards for the cardscrollview
     */
    private BeaconCardScrollAdapter cardAdapter;
    /**
     * Textview which contains the number of beacons found
     */
    private TextView beaconCountTextView;
    /**
     * Progress bar that runs while the application is scanning for Beacons
     */
    private ProgressBar progressBar;
    /**
     * Number of beacons found
     */
    private int beaconCount;
    /**
     * Wakelock used to keep the device on while the client is scanning
     */
    private PowerManager.WakeLock wakeLock;
    /**
     * Client used to scan for beacons
     */
    private BeaconScanClient client;

    /**
     * Callback that is called every time a BLE device is found
     */
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            final Beacon beacon = BluetoothDeviceToBeacon.toBeacon(device, rssi, scanRecord);
            if (beacon != null) {
                addBeacon(beacon);
            }
        }
    };
    /**
     * Callback which is executed once the client is finished scanning
     */
    private ScanEndUserCallback userCallback = new ScanEndUserCallback() {
        @Override
        public void execute() {
            stopScanning();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.cardAdapter = new BeaconCardScrollAdapter(this);
        this.beaconCountTextView = (TextView) findViewById(R.id.beacon_count);
        this.progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        this.beaconCount = 0;
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WAKELOCK_TAG);
        if (this.client == null) {
            this.client = new BeaconClientBuilder()
                    .scanInterval(SCAN_INTERVAL)
                    .setContext(this)
                    .setLeScanCallback(leScanCallback)
                    .setUserCallback(userCallback)
                    .build();
        }
        startScanning();
    }

    /**
     * Sets up the application to start scanning and starts the client
     */
    private void startScanning() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.client.startScanning();
        this.wakeLock.acquire(WAKELOCK_TIMEOUT);
    }

    /**
     * Cleans up once the client is done scanning
     */
    private void stopScanning() {
        this.progressBar.setVisibility(View.GONE);
        if (this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
    }

    /**
     * Wrapper to add a beacon to the adapter
     *
     * @param beacon Beacon
     */
    private void addBeacon(final Beacon beacon) {
        switch(this.cardAdapter.addBeacon(beacon)) {
            case NEW_BEACON:
                incrementBeaconCount();
                break;
            case REPEAT_BEACON:
                break;
        }
    }

    /**
     * Increments the current number of Beacons
     */
    private void incrementBeaconCount() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                beaconCount++;
                beaconCountTextView.setText(String.valueOf(beaconCount));
            }
        });
    }
}