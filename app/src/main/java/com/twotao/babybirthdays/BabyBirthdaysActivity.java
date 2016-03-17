/**
 *   Copyright (C) 2012 - TwoTau, LLC
 */
package com.twotao.babybirthdays;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class BabyBirthdaysActivity extends ListActivity {
	
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private BirthdaysDbAdapter mDbHelper;
//    private ListView mListView;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.birthdays_list);
        mDbHelper = new BirthdaysDbAdapter(this);
        mDbHelper.open();
        
        // Add a header to the list view
        ListView listView = getListView();
        View header = getLayoutInflater().inflate(R.layout.birthdays_header, null);
        listView.addHeaderView(header, null, false);
//        listView.addHeaderView(header);
        
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor birthdaysCursor = mDbHelper.fetchAllBirthdays();
        startManagingCursor(birthdaysCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{BirthdaysDbAdapter.KEY_NAME, BirthdaysDbAdapter.KEY_BIRTHDATE, BirthdaysDbAdapter.KEY_BIRTHDATE};

        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3};

        // Now create a simple cursor adapter and set it to display
        BirthdayCursorAdapter birthdays = 
            new BirthdayCursorAdapter(this, R.layout.birthdays_row, birthdaysCursor, from, to);
        
        setListAdapter(birthdays);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createBirthday();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteBirthday(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createBirthday() {
        Intent i = new Intent(this, BirthdayEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, BirthdayEdit.class);
        i.putExtra(BirthdaysDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}