package org.me.gcu.cw1;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class AllCurrenciesFragment extends Fragment {

    private ListView listView;
    private ArrayList<CurrencyItem> currencies = new ArrayList<>();
    private FragmentListener listener;

    private EditText searchEditText;
    private Button searchButton;

    public interface FragmentListener {
        void onCurrencySelected(CurrencyItem item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_currencies, container, false);

        listView = view.findViewById(R.id.currencyListView);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> filterCurrencies());

        updateUI(currencies);
        return view;
    }

    public void updateCurrencies(ArrayList<CurrencyItem> items) {
        currencies = items;
        updateUI(currencies);
    }

    private void filterCurrencies() {
        String query = searchEditText.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            updateUI(currencies);
            return;
        }

        ArrayList<CurrencyItem> filtered = new ArrayList<>();
        for (CurrencyItem item : currencies) {
            if (item.getCurrencyName().toLowerCase().contains(query) ||
                    item.getCurrencyCode().toLowerCase().contains(query)) {
                filtered.add(item);
            }
        }

        updateUI(filtered);
    }

    private void updateUI(ArrayList<CurrencyItem> displayCurrencies) {
        if (listView == null || displayCurrencies.isEmpty()) return;

        String[] displayData = new String[displayCurrencies.size()];
        for (int i = 0; i < displayCurrencies.size(); i++) {
            CurrencyItem item = displayCurrencies.get(i);
            String name = item.getCurrencyName();
            String code = item.getCurrencyCode();
            if (name == null || name.isEmpty()) name = code;
            displayData[i] = name + " (" + code + ")\n" + "Rate: " + item.getRate() + "\n" + "Date: " + item.getDate();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,
                displayData) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                double rate = displayCurrencies.get(position).getRate();
                int bgColor = ColorUtils.getBackgroundColorForRate(rate);
                int textColor = ColorUtils.getTextColorForRate(rate);

                view.setBackgroundColor(bgColor);

                TextView tv = view.findViewById(android.R.id.text1);
                if (tv != null) {
                    tv.setTextColor(textColor);
                    int padding = (int) (8 * getResources().getDisplayMetrics().density);
                    tv.setPadding(padding, padding, padding, padding);
                }

                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            CurrencyItem selectedItem = displayCurrencies.get(position);
            listener.onCurrencySelected(selectedItem);
        });
    }
}
