package com.smartpocket.cuantoteroban;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CurrencySuggestionProvider extends ContentProvider {
	Set<Currency> allUnusedCurrencies = null;
	
	public Cursor query(@NotNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (allUnusedCurrencies == null)
			allUnusedCurrencies = CurrencyManager.getInstance().getAllUnusedCurrencies();
		
		MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_ID", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_TEXT_2});
		String query = uri.getLastPathSegment();

		if (SearchManager.SUGGEST_URI_PATH_QUERY.equals(query)) {
			// user hasn't entered anything
			// thus return a default cursor
		} else {
			// query contains the users search
			// return a cursor with appropriate data
			int counter = 1;
			for(Currency curr : allUnusedCurrencies) {
				if (curr.matchesQuery(query)){
					matrixCursor.addRow(new Object[]{counter, curr.getName(), curr.getFlagIdentifier(), curr.getCode()});
					counter++;
				}
			}
		}
		return matrixCursor;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(@NotNull Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(@NotNull Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(@NotNull Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(@NotNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
