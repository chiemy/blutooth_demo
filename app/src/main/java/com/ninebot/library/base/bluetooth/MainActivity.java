package com.ninebot.library.base.bluetooth;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private MyAdapter mMyAdapter;
    private Handler mHandler = new Handler();

    private BluetoothAdapter mBluetoothAdapter;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mMyAdapter = new MyAdapter();
        recyclerView.setAdapter(mMyAdapter);

        RxPermissions permissions = new RxPermissions(this);
        permissions.request(permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            startScan();
                        }
                    }
                });
    }

    private ScanCallback mScanCallback;
    private void startScan() {
        mMyAdapter.clear();

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setDeviceAddress("D1:07:40:1D:47:8F")
                    .build();
            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(scanFilter);
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .build();
            mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, null, mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    mLeScanCallback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            });
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private void stopScan() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mMyAdapter.add(device);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }

    private class MyAdapter extends Adapter {
        private List<BluetoothDevice> mDevices;
        private Set<String> mStringSet = new HashSet<>();

        public MyAdapter() {
            mDevices = new ArrayList<>();
        }

        public void add(BluetoothDevice device) {
            mDevices.add(device);
            notifyDataSetChanged();
        }

        public void clear() {
            mDevices.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(new TextView(viewGroup.getContext())){};
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            BluetoothDevice device = mDevices.get(i);
            TextView tv = (TextView) viewHolder.itemView;
            tv.setText(device.getName() + "  " + device.getAddress());
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "重新扫描");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            stopScan();
            startScan();
        }
        return super.onOptionsItemSelected(item);
    }
}
