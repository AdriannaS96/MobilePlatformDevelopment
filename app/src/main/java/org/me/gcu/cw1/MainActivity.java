/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                Adrianna Starzec
// Student ID          S2337774
// Programme of Study   Software Development
//
package org.me.gcu.cw1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import java.util.Locale;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends AppCompatActivity implements
        AllCurrenciesFragment.FragmentListener {

    private LinearLayout topCurrenciesContainer;
    private AllCurrenciesFragment allFragment;
    private final String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";

    private final Handler handler = new Handler();
    private final int UPDATE_INTERVAL_MS = 60 * 60 * 1000;

    private final Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            fetchCurrencies(urlSource);
            handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topCurrenciesContainer = findViewById(R.id.topCurrenciesContainer);
        allFragment = new AllCurrenciesFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.allCurrenciesContainer, allFragment)
                .commit();


        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> fetchCurrencies(urlSource));
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(fetchRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(fetchRunnable);
    }

    @Override
    public void onCurrencySelected(CurrencyItem item) {
        showConversionDialog(item);
    }

    private void showConversionDialog(CurrencyItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.currency_conversion_dialog, null);
        builder.setView(dialogView);

        EditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        RadioButton rbGbpToOther = dialogView.findViewById(R.id.rbGbpToOther);
        RadioButton rbOtherToGbp = dialogView.findViewById(R.id.rbOtherToGbp);
        Button convertButton = dialogView.findViewById(R.id.convertButton);
        TextView resultTextView = dialogView.findViewById(R.id.resultTextView);

        rbGbpToOther.setText("GBP → " + item.getCurrencyCode());
        rbOtherToGbp.setText(item.getCurrencyCode() + " → GBP");

        convertButton.setOnClickListener(v -> {
            String input = amountEditText.getText().toString().trim();
            if (input.isEmpty()) {
                amountEditText.setError("Please enter a value");
                return;
            }
            try {
                double amount = Double.parseDouble(input);
                double result = rbGbpToOther.isChecked()
                        ? amount * item.getRate()
                        : amount / item.getRate();
                resultTextView.setText(String.format("%.2f", result));
            } catch (NumberFormatException e) {
                amountEditText.setError("Invalid number");
            }
        });

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void fetchCurrencies(String url) {
        new Thread(() -> {
            try {
                URLConnection conn = new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                ArrayList<CurrencyItem> items = parseCurrencies(sb.toString());

                handler.post(() -> {
                    showTopCurrencies(items);
                    allFragment.updateCurrencies(items);
                });

            } catch (IOException e) {
                e.printStackTrace();

                handler.post(() -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Network Error")
                            .setMessage("Please check your internet connection.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            }
        }).start();
    }

    private ArrayList<CurrencyItem> parseCurrencies(String xmlData) {
        ArrayList<CurrencyItem> currencyList = new ArrayList<>();
        CurrencyItem currentItem = null;

        xmlData = xmlData.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;)", "&amp;");

        try {
            boolean insideItem = false;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();

                    if ("item".equalsIgnoreCase(tag)) {
                        insideItem = true;
                        currentItem = new CurrencyItem("", "", 0.0, "");
                        Log.d("CurrencyParser", "New currency item found!");
                    } else if (insideItem) {
                        switch (tag.toLowerCase()) {
                            case "title":
                                String title = xpp.nextText();
                                currentItem.setCurrencyName(title);
                                Log.d("CurrencyParser", "Currency title: " + title);
                                break;

                            case "description":
                                String desc = xpp.nextText();
                                String[] parts = desc.split("=");
                                if (parts.length == 2) {
                                    try {
                                        double rate = Double.parseDouble(parts[1].trim().split(" ")[0]);
                                        String currencyName = parts[1].trim().substring(parts[1].trim().indexOf(" ") + 1);
                                        String code = mapCurrencyNameToCode(currencyName);

                                        currentItem.setRate(rate);
                                        currentItem.setCurrencyCode(code);

                                        Log.d("CurrencyParser", "Rate: " + rate + ", Code: " + code);
                                    } catch (Exception e) {
                                        Log.e("CurrencyParser", "Error parsing rate: " + e.getMessage());
                                    }
                                }
                                break;

                            case "pubdate":
                                String today = new java.text.SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        java.util.Locale.getDefault()
                                ).format(new java.util.Date());
                                currentItem.setDate(today);
                                Log.d("CurrencyParser", "Date set to today: " + today);
                                break;
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("item".equalsIgnoreCase(xpp.getName()) && currentItem != null) {
                        currencyList.add(currentItem);
                        insideItem = false;
                        Log.d("CurrencyParser", "Currency item parsing completed: " + currentItem.getCurrencyCode());
                    }
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e("CurrencyParser", "Parsing error", e);
        }

        Log.d("CurrencyParser", "End of document reached, total items: " + currencyList.size());
        return currencyList;
    }

    private String mapCurrencyNameToCode(String name) {
        name = name.toLowerCase();
        if (name.contains("usd") || name.contains("us dollar")) return "USD";
        if (name.contains("eur") || name.contains("euro")) return "EUR";
        if (name.contains("jpy") || name.contains("yen")) return "JPY";
        return name.length() >= 3 ? name.substring(0, 3).toUpperCase() : name.toUpperCase();
    }

    private void showTopCurrencies(ArrayList<CurrencyItem> items) {
        topCurrenciesContainer.removeAllViews();
        String[] topCodes = {"USD", "EUR", "JPY"};

        for (String code : topCodes) {
            for (CurrencyItem item : items) {
                if (code.equals(item.getCurrencyCode())) {

                    Button btn = new Button(this);
                    btn.setText(code + ": " + String.format(Locale.getDefault(), "%.2f", item.getRate()));
                    btn.setBackgroundColor(ColorUtils.getBackgroundColorForRate(item.getRate()));

                    int flagResId = 0;
                    switch (code) {
                        case "USD":
                            flagResId = R.drawable.usa;
                            break;
                        case "EUR":
                            flagResId = R.drawable.europe;
                            break;
                        case "JPY":
                            flagResId = R.drawable.japan;
                            break;
                    }

                    if (flagResId != 0) {
                        Drawable drawable = ContextCompat.getDrawable(this, flagResId);
                        if (drawable != null) {
                            int width = (int) (48 * getResources().getDisplayMetrics().density);
                            int height = (int) (32 * getResources().getDisplayMetrics().density);

                            drawable.setBounds(0, 0, width, height);
                            btn.setCompoundDrawables(drawable, null, null, null);
                            btn.setCompoundDrawablePadding((int) (8 * getResources().getDisplayMetrics().density));
                        }
                    }

                    btn.setOnClickListener(v -> showConversionDialog(item));


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
                    params.setMargins(
                            (int) (8 * getResources().getDisplayMetrics().density),
                            (int) (4 * getResources().getDisplayMetrics().density),
                            (int) (8 * getResources().getDisplayMetrics().density),
                            (int) (4 * getResources().getDisplayMetrics().density)
                    );
                    btn.setLayoutParams(params);

                    topCurrenciesContainer.addView(btn);
                    break;
                }
            }
        }
    }
}
