package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class StockWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StockWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor data = null;
    private Context mContext;

    StockWidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        data = mContext.getContentResolver().query(Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
        }
        RemoteViews views = new RemoteViews(mContext.getPackageName(),
                R.layout.stocks_widget_list_item);
        DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
        String price = dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE));
        views.setTextViewText(R.id.widget_symbol, symbol);
        views.setTextViewText(R.id.widget_price, price);

        DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            views.setTextViewCompoundDrawables(R.id.widget_change, R.drawable.percent_change_pill_green, 0, 0, 0 );
        } else {
            views.setTextViewCompoundDrawables(R.id.widget_change, R.drawable.percent_change_pill_red, 0, 0, 0 );
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.widget_change, change);
        } else {
            views.setTextViewText(R.id.widget_change, percentage);
        }

        final Intent fillInIntent = new Intent();
        fillInIntent.setData(Contract.Quote.URI);
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.stocks_widget_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data.moveToPosition(position)) {
            return data.getLong(Contract.Quote.POSITION_ID);
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
