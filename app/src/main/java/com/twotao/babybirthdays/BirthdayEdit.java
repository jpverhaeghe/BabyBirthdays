/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twotao.babybirthdays;

import java.util.Calendar;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class BirthdayEdit extends Activity {

    private EditText mNameText;
    private DatePicker mBirthdateChooser;
    private Long mRowId;
	private BirthdaysDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new BirthdaysDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.birthday_edit);
        setTitle(R.string.edit_birthday);

        mNameText = (EditText) findViewById(R.id.name);
        mBirthdateChooser = (DatePicker) findViewById(R.id.birthdate);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(BirthdaysDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(BirthdaysDbAdapter.KEY_ROWID)
                                    : null;
        }

        populateFields();
        
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }
    
    private void populateFields() {
        final Calendar birthdate = Calendar.getInstance();
        
        if (mRowId != null) {
            Cursor birthday = mDbHelper.fetchBirthday(mRowId);
            startManagingCursor(birthday);
            mNameText.setText(birthday.getString(
                    birthday.getColumnIndexOrThrow(BirthdaysDbAdapter.KEY_NAME)));
            
            // get the date in milliseconds and create the calendar object
            long testTime = birthday.getLong(
                    birthday.getColumnIndexOrThrow(BirthdaysDbAdapter.KEY_BIRTHDATE));
            birthdate.setTimeInMillis(testTime);
            
            // now update the date picker widget to show the stored date
            mBirthdateChooser.updateDate(birthdate.get(Calendar.YEAR), 
            		birthdate.get(Calendar.MONTH), birthdate.get(Calendar.DAY_OF_MONTH));
        }
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(BirthdaysDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {
		super.onPause();
        saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
        populateFields();
	}
	
	private void saveState() {
        final Calendar adjustedDate = Calendar.getInstance();
        String name = mNameText.getText().toString();
        
        // set the updated date and store it in milliseconds
        adjustedDate.set(mBirthdateChooser.getYear(), mBirthdateChooser.getMonth(), mBirthdateChooser.getDayOfMonth());
        long birthdate = adjustedDate.getTimeInMillis();

        if (mRowId == null) {
            long id = mDbHelper.createBirthday(name, birthdate);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateBirthday(mRowId, name, birthdate);
        }
    }
}
