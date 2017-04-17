package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity {
    @BindView(R.id.symbol)
    TextView symbolTextView;
    @BindView(R.id.chart)
    LineChart chart;

    private String symbol;
    private String[] histories;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        Intent data = getIntent();
        symbol = data.getStringExtra(MainActivity.INTENT_EXTRA_SYMBOL);
        String history = data.getStringExtra(MainActivity.INTENT_EXTRA_HISTORY);
        histories = history.split("\n");

        symbolTextView.setText(symbol);
        createChart();
    }

    private void createChart() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < histories.length; i++) {
            String historyItem = histories[histories.length - 1 - i];
            entries.add(new Entry(i, getClosePrise(historyItem)));
        }
        LineDataSet dataSet = new LineDataSet(entries, symbol);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

        setChartTextColor(Color.WHITE);

        IAxisValueFormatter xAxisFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getYearDate((int)value);
            }
        };

        final DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        IAxisValueFormatter yAxisFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dollarFormat.format(value);
            }
        };

        chart.getXAxis().setValueFormatter(xAxisFormatter);
        chart.getAxisLeft().setValueFormatter(yAxisFormatter);
        chart.getAxisRight().setEnabled(false);
        chart.setDescription(null);
    }

    private void setChartTextColor(int color) {
        chart.getXAxis().setTextColor(color);
        chart.getAxisLeft().setTextColor(color);
        chart.getLegend().setTextColor(color);
    }

    private float getClosePrise(String historyItem) {
        int idx = historyItem.indexOf(",");
        String closePrise = historyItem.substring(idx + 1);
        return Float.parseFloat(closePrise);
    }

    private String getYearDate(int xValue) {
        String historyItem = histories[histories.length - 1 - xValue];
        int idx = historyItem.indexOf(",");
        long dateTimeInMills = Long.parseLong(historyItem.substring(0, idx));
        SimpleDateFormat formatter = new SimpleDateFormat("MMM y", Locale.getDefault());
        Date date = new Date();
        date.setTime(dateTimeInMills);

        return formatter.format(date);
    }
}
