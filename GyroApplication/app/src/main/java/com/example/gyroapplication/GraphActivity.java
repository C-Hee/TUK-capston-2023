package com.example.gyroapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    Intent graphActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graphActivityIntent = getIntent();

        CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month += 1;

                // 선택된 날짜 정보를 사용하여 원하는 작업을 수행합니다.
                String selectedDate = year + "-" + month + "-" + dayOfMonth;

                JSONObject postParam = new JSONObject();
                try {
                    postParam.put("id", graphActivityIntent.getStringExtra("ID"));
                    postParam.put("datetime", selectedDate);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new Thread(() -> {
                    PostManager postManager = new PostManager("138.2.126.137");
                    JSONObject dayFromMonth, weekFromMonth, monthFromMonth;
                    dayFromMonth = postManager.sendPost(postParam.toString(), "request/day_from_month");
                    weekFromMonth = postManager.sendPost(postParam.toString(), "request/week_from_month");
                    monthFromMonth = postManager.sendPost(postParam.toString(), "request/month_from_year");
                    drawDayFromMonth(dayFromMonth);
                    drawWeekFromMonth(weekFromMonth);
                    drawMonthFromYear(monthFromMonth);
                }).start();
            }
        });
    }

    public void drawDayFromMonth(JSONObject data)
    {
        LineChart lineChart = findViewById(R.id.chart1);

        List<Entry> entries = new ArrayList<>();

        JSONArray jsonArray = null;
        try {
            jsonArray = data.getJSONArray("data");

            for(int i=1; i<=31; i++)
            {
                entries.add(new Entry(i, 0));
            }
            for(int i=0; i<jsonArray.length(); i++)
            {
                JSONObject jo = jsonArray.getJSONObject(i);
                int dayofmonth = jo.getInt("dayofmonth");
                float average = (float)jo.getDouble("average");
                entries.set(dayofmonth-1, new Entry(dayofmonth, average));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "날짜");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(true);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setDrawValues(true);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);
        xAxis.setGranularity(1);

        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis;
        yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    public void drawWeekFromMonth(JSONObject data)
    {
        LineChart lineChart = findViewById(R.id.chart2);

        List<Entry> entries = new ArrayList<>();

        JSONArray jsonArray = null;
        try {
            jsonArray = data.getJSONArray("data");

            for(int i=0; i<6; i++)
            {
                entries.add(new Entry(i, 0));
            }
            for(int i=0; i<jsonArray.length(); i++)
            {
                JSONObject jo = jsonArray.getJSONObject(i);
                int dayofweek = jo.getInt("dayofweek");
                float average = (float)jo.getDouble("average");
                entries.set(dayofweek-1, new Entry(dayofweek-1, average));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "주차");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(true);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setDrawValues(true);

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("1주차");
        labels.add("2주차");
        labels.add("3주차");
        labels.add("4주차");
        labels.add("5주차");
        labels.add("6주차");

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1);
        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis;
        yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    public void drawMonthFromYear(JSONObject data)
    {
        LineChart lineChart = (LineChart)findViewById(R.id.chart3);

        List<Entry> entries = new ArrayList<>();

        JSONArray jsonArray = null;
        try {
            jsonArray = data.getJSONArray("data");

            for(int i=0; i<=11; i++)
            {
                entries.add(new Entry(i, 0));
            }
            for(int i=0; i<jsonArray.length(); i++)
            {
                JSONObject jo = jsonArray.getJSONObject(i);
                int month = jo.getInt("month");
                float average = (float)jo.getDouble("average");
                entries.set(month, new Entry(month, average));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "월");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(true);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setDrawValues(true);

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Jan");
        labels.add("Feb");
        labels.add("Mar");
        labels.add("Apr");
        labels.add("May");
        labels.add("Jun");
        labels.add("Jul");
        labels.add("Aug");
        labels.add("Sep");
        labels.add("Oct");
        labels.add("Nov");
        labels.add("Dec");

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1);

        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis;
        yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }
}