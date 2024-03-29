package com.example.besafeapp.ui.network;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.InetAddresses;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.besafeapp.R;

import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {
    static final String PCAPDROID_PACKAGE = "com.emanuelef.remote_capture"; // add ".debug" for the debug build of PCAPdroid
    static final String CAPTURE_CTRL_ACTIVITY = "com.emanuelef.remote_capture.activities.CaptureCtrl";
    static final String CAPTURE_STATUS_ACTION = "com.emanuelef.remote_capture.CaptureStatus";
    static final String TAG = "PCAP Receiver";
    static final int PICAPDROID_TRAIL_SIZE = 32;
    Button mStart;
    CaptureThread mCapThread;
    TextView mLog;
    TextView mIP;
    String ip;
    boolean mCaptureRunning = false;

    private final ActivityResultLauncher<Intent> captureStartLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCaptureStartResult);
    private final ActivityResultLauncher<Intent> captureStopLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCaptureStopResult);
    private final ActivityResultLauncher<Intent> captureStatusLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCaptureStatusResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_info);

        mIP = findViewById(R.id.ip);

        Context context = getApplicationContext();
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        byte[] myIPAddress = BigInteger.valueOf(wifiInfo.getIpAddress()).toByteArray();
        InetAddress myInetIP;
        try {
            myInetIP = InetAddress.getByAddress(myIPAddress);
        } catch (UnknownHostException e) {
            myInetIP = null;
            e.printStackTrace();
        }
        assert myInetIP != null;
        ip = myInetIP.getHostAddress();
        mIP.append(String.format("Твоето IP: %s", ip));

        mLog = findViewById(R.id.pkts_log);
        mStart = findViewById(R.id.start_btn);
        mStart.setOnClickListener(v -> {
            if(!mCaptureRunning)
                startCapture();
            else
                stopCapture();
        });

        if((savedInstanceState != null) && savedInstanceState.containsKey("capture_running"))
            setCaptureRunning(savedInstanceState.getBoolean("capture_running"));
        else
            queryCaptureStatus();

        // will call the "update" method when the capture status changes
        com.example.besafeapp.ui.network.MyBroadcastReceiver.CaptureObservable.getInstance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.example.besafeapp.ui.network.MyBroadcastReceiver.CaptureObservable.getInstance().deleteObserver(this);
        stopCaptureThread();
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean capture_running = (boolean)arg;
        Log.d(TAG, "capture_running: " + capture_running);
        setCaptureRunning(capture_running);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        bundle.putBoolean("capture_running", mCaptureRunning);
        super.onSaveInstanceState(bundle);
    }

    void onPacketReceived(EthernetPacket pkt) {
        byte[] frame = pkt.getRawData();
        int length = frame.length;

        if (length < PICAPDROID_TRAIL_SIZE) {
            return;
        }
        ByteBuffer trailer = ByteBuffer.wrap(Arrays.copyOfRange(frame, length - PICAPDROID_TRAIL_SIZE, frame.length));
        int magic = trailer.getInt();
        int process_id = trailer.getInt();
        trailer = ByteBuffer.wrap(trailer.array(), 0, 20);
        String app_name = Charset.forName("ISO-8859-1").decode(trailer).toString();
        app_name = app_name.substring(8, app_name.length());

        IpV4Packet packet = (IpV4Packet) pkt.getPayload();
        IpV4Packet.IpV4Header packet_header = packet.getHeader();
        String dest = packet_header.getDstAddr().toString();
        String src = packet_header.getSrcAddr().toString();
        String l4_protocol = packet_header.getProtocol().toString();

        int src_port = -1;
        int dest_port = -1;

        if (l4_protocol.contains("TCP")) {
            l4_protocol = new String("TCP");
            TcpPacket segment = (TcpPacket) packet.getPayload();
            TcpPacket.TcpHeader tcp_header = segment.getHeader();
            dest_port = tcp_header.getDstPort().valueAsInt();
            src_port = tcp_header.getSrcPort().valueAsInt();
        } else if (l4_protocol.contains("UDP")) {
            l4_protocol = new String("UDP");
            UdpPacket datagram = (UdpPacket) packet.getPayload();
            UdpPacket.UdpHeader udp_header = datagram.getHeader();
            dest_port = udp_header.getDstPort().valueAsInt();
            src_port = udp_header.getSrcPort().valueAsInt();
        }
        mLog.append(String.format("%s [%s]\nОт: %s -> До: %s\n\n\n", app_name, l4_protocol, src, dest));
    }

    void queryCaptureStatus() {
        Log.d(TAG, "Querying PCAPdroid");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(PCAPDROID_PACKAGE, CAPTURE_CTRL_ACTIVITY);
        intent.putExtra("action", "get_status");

        try {
            captureStatusLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "PCAPdroid package not found: " + PCAPDROID_PACKAGE, Toast.LENGTH_LONG).show();
        }
    }

    void startCapture() {
        Log.d(TAG, "Starting PCAPdroid");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(PCAPDROID_PACKAGE, CAPTURE_CTRL_ACTIVITY);

        intent.putExtra("action", "start");
        intent.putExtra("broadcast_receiver", "com.emanuelef.pcap_receiver.MyBroadcastReceiver");
        intent.putExtra("pcap_dump_mode", "udp_exporter");
        intent.putExtra("collector_ip_address", "127.0.0.1");
        intent.putExtra("collector_port", "5123");
        intent.putExtra("pcapdroid_trailer", true);

        captureStartLauncher.launch(intent);
    }

    void stopCapture() {
        Log.d(TAG, "Stopping PCAPdroid");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(PCAPDROID_PACKAGE, CAPTURE_CTRL_ACTIVITY);
        intent.putExtra("action", "stop");

        captureStopLauncher.launch(intent);
    }

    void setCaptureRunning(boolean running) {
        mCaptureRunning = running;
        mStart.setText(running ? "Спри засичане на трафик" : "Започни засичане на трафик");

        if(mCaptureRunning && (mCapThread == null)) {
            mCapThread = new CaptureThread(this);
            mCapThread.start();
        } else if(!mCaptureRunning)
            stopCaptureThread();
    }

    void stopCaptureThread() {
        if(mCapThread == null)
            return;

        mCapThread.stopCapture();
        mCapThread.interrupt();
        mCapThread = null;
    }

    void handleCaptureStartResult(final ActivityResult result) {
        Log.d(TAG, "PCAPdroid start result: " + result);

        if(result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, "Capture started!", Toast.LENGTH_SHORT).show();
            setCaptureRunning(true);
            mLog.setText("");
        } else
            Toast.makeText(this, "Capture failed to start", Toast.LENGTH_SHORT).show();
    }

    void handleCaptureStopResult(final ActivityResult result) {
        Log.d(TAG, "PCAPdroid stop result: " + result);

        if(result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, "Capture stopped!", Toast.LENGTH_SHORT).show();
            setCaptureRunning(false);
        } else
            Toast.makeText(this, "Could not stop capture", Toast.LENGTH_SHORT).show();

        Intent intent = result.getData();
        if((intent != null) && (intent.hasExtra("bytes_sent")))
            logStats(intent);
    }

    void handleCaptureStatusResult(final ActivityResult result) {
        Log.d(TAG, "PCAPdroid status result: " + result);

        if((result.getResultCode() == RESULT_OK) && (result.getData() != null)) {
            Intent intent = result.getData();
            boolean running = intent.getBooleanExtra("running", false);
            int verCode = intent.getIntExtra("version_code", 0);
            String verName = intent.getStringExtra("version_name");

            if(verName == null)
                verName = "<1.4.6";

            Log.d(TAG, "PCAPdroid " + verName + "(" + verCode + "): running=" + running);
            setCaptureRunning(running);
        }
    }

    void logStats(Intent intent) {
        String stats = "*** Stats ***" +
                "\nBytes sent: " +
                intent.getLongExtra("bytes_sent", 0) +
                "\nBytes received: " +
                intent.getLongExtra("bytes_rcvd", 0) +
                "\nPackets sent: " +
                intent.getIntExtra("pkts_sent", 0) +
                "\nPackets received: " +
                intent.getIntExtra("pkts_rcvd", 0) +
                "\nPackets dropped: " +
                intent.getIntExtra("pkts_dropped", 0) +
                "\nPCAP dump size: " +
                intent.getLongExtra("bytes_dumped", 0);

        Log.i("stats", stats);
    }
}