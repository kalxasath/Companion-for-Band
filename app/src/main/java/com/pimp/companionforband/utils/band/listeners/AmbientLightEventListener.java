package com.pimp.companionforband.utils.band.listeners;

import android.os.Environment;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;
import com.opencsv.CSVWriter;
import com.pimp.companionforband.R;
import com.pimp.companionforband.activities.main.MainActivity;
import com.pimp.companionforband.fragments.sensors.SensorActivity;
import com.pimp.companionforband.fragments.sensors.SensorsFragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class AmbientLightEventListener implements BandAmbientLightEventListener {
    @Override
    public void onBandAmbientLightChanged(final BandAmbientLightEvent event) {
        if (event != null) {
            MainActivity.sActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SensorActivity.series1.appendData(new DataPoint(SensorActivity.graphLastValueX,
                            (double) event.getBrightness()), true, 100);
                    SensorActivity.graphLastValueX += 1;
                }
            });
            SensorsFragment.appendToUI(MainActivity.sContext.getString(R.string.brightness) + String.format(" = %d lux\n", event.getBrightness()), SensorsFragment.ambientLightTV);
            if (MainActivity.sharedPreferences.getBoolean("log", false)) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CompanionForBand" + File.separator + "AmbientLight");
                if (file.exists() || file.isDirectory()) {
                    try {
                        Date date = new Date();
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                        if (new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "CompanionForBand" + File.separator + "AmbientLight" + File.separator + "AmbientLight_" + DateFormat.getDateInstance().format(date) + ".csv").exists()) {
                            String str = DateFormat.getDateTimeInstance().format(event.getTimestamp());
                            CSVWriter csvWriter = new CSVWriter(new FileWriter(path + File.separator + "CompanionForBand" + File.separator + "AmbientLight" + File.separator + "AmbientLight_" + DateFormat.getDateInstance().format(date) + ".csv", true));
                            csvWriter.writeNext(new String[]{String.valueOf(event.getTimestamp()),
                                    str, String.valueOf(event.getBrightness())});
                            csvWriter.close();
                        } else {
                            CSVWriter csvWriter = new CSVWriter(new FileWriter(path + File.separator + "CompanionForBand" + File.separator + "AmbientLight" + File.separator + "AmbientLight_" + DateFormat.getDateInstance().format(date) + ".csv", true));
                            csvWriter.writeNext(new String[]{MainActivity.sContext.getString(R.string.timestamp), MainActivity.sContext.getString(R.string.date_time), MainActivity.sContext.getString(R.string.brightness)});
                            csvWriter.close();
                        }
                    } catch (IOException e) {
                        Log.e("CSV", e.toString());
                    }
                } else {
                    file.mkdirs();
                }
            }
        }
    }
}
