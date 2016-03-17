/**
 * 
 */
package com.twotao.babybirthdays;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * This class extends the Cursor Adapter class, and overrides the newView() and
 * bindView() so we can display the row data as dates, not milliseconds as they
 * are stored in the database. It also uses some logic to display the age of the
 * person on each row, based on how old they are - in days, weeks, months and
 * years.
 * 
 * TODO: The values used for age determination may become customizable and will
 * need to use the DB to get stored data.
 * 
 * @author Jim
 * 
 */
public class BirthdayCursorAdapter extends SimpleCursorAdapter {
	
	private static final int MILLISECONDS_IN_SEC = 1000;
	private static final int NUM_SECONDS_IN_MINUTE = 60;
	private static final int NUM_MINUTES_IN_HOUR = 60;
	private static final int NUM_HOURS_IN_DAY = 24;
	private static final int NUM_DAYS_IN_WEEK = 7;
	private static final int NUM_DAYS_IN_YEAR = 365;
	private static final int NUM_MONTHS_PER_YEAR = 12;
	private static final int NUM_WEEKS_FOR_DISPLAY = 8;
	private static final int NUM_MONTHS_FOR_DISPLAY = 2;

	private static final int NUM_DAYS_FOR_DAYS_DISPLAY = NUM_DAYS_IN_WEEK;
	private static final int NUM_DAYS_FOR_WEEKS_DISPLAY = (NUM_WEEKS_FOR_DISPLAY * NUM_DAYS_IN_WEEK);
	private static final int NUM_DAYS_FOR_MONTHS_DISPLAY = (NUM_MONTHS_FOR_DISPLAY * NUM_DAYS_IN_YEAR);
	
	private static final String DATE_FORMAT = "MMM-dd";

	// Not sure if this is needed, as it isn't used much
	private Context mContext;
	private int mLayout;

	/**
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 */
	public BirthdayCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		mContext = context;
		mLayout = layout;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		Cursor c = getCursor();

		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(mLayout, parent, false);

		// create the row to display
		createRowDisplay(v, c);

		return v;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		// create the row to display
		createRowDisplay(v, c);
	}

	/*
	 * Creates the customized display for each row in the main birthday list
	 */
	private void createRowDisplay(View v, Cursor c) {

		// get the name information
		int nameCol = c.getColumnIndex(BirthdaysDbAdapter.KEY_NAME);
		String name = c.getString(nameCol);

		// set the name of the entry.
		TextView nameText = (TextView) v.findViewById(R.id.text1);
		if (nameText != null) {
			nameText.setText(name);
		}

		// get the birth date in milliseconds and convert it to an actual date
		// for manipulation for the next two entry displays
		int birthdateCol = c.getColumnIndex(BirthdaysDbAdapter.KEY_BIRTHDATE);
		long birthdateInMillis = c.getLong(birthdateCol);
		Date birthdate = new Date(birthdateInMillis);

		// set the birthday of the entry in MMM-DD format
		TextView birthday = (TextView) v.findViewById(R.id.text2);
		if (birthday != null) {
			SimpleDateFormat displayDate = new SimpleDateFormat(DATE_FORMAT);
			birthday.setText(displayDate.format(birthdate));
		}

		// set the age of the entry
		TextView ageText = (TextView) v.findViewById(R.id.text3);
		if (ageText != null) {
			String ageToDispaly = getAgeDisplayString(birthdate);

			if (ageToDispaly != null) {
				ageText.setText(ageToDispaly);
			}
		}
	}

	/*
	 * Creates the appropriate string to display for the age based on
	 * pre-determined parameters
	 * 
	 * TODO: not currently taking into consideration negatives - necessary?
	 */
	private String getAgeDisplayString(Date birthdate) {

		String ageToDisplay = null;

		// get the current date
		Date now = new Date();

		// compare the dates - getting the difference in milliseconds
		long timeSinceBirth = now.getTime() - birthdate.getTime();

		// convert the time to days
		int ageInDays = (int)(timeSinceBirth / (MILLISECONDS_IN_SEC * NUM_SECONDS_IN_MINUTE * NUM_MINUTES_IN_HOUR * NUM_HOURS_IN_DAY) );

		// if in the area to display days, create a string in days
		if (ageInDays < NUM_DAYS_FOR_DAYS_DISPLAY) {
			ageToDisplay = getAgeTextInDays(ageInDays);
		}
		// if in the area to display weeks, create a string in weeks
		else if (ageInDays < NUM_DAYS_FOR_WEEKS_DISPLAY) {
			ageToDisplay = getAgeTextInWeeks(ageInDays);
		}
		// if in the area to display months or years, we do some special checks for months - using years
		else {

			// we know we are in months or years now, so lets rule out years first
			int ageInYears = (ageInDays / NUM_DAYS_IN_YEAR);
			
			// if we are beyond the month display, then create the year string
			if (ageInDays >= NUM_DAYS_FOR_MONTHS_DISPLAY)
			{
				ageToDisplay = getAgeTextInYears(ageInYears);
			}
			// we must be in month display, so create the month string
			else {
				ageToDisplay = getAgeTextInMonths(birthdate, now, ageInDays, ageInYears);
			}
		}

		return ageToDisplay;
	}
	
	/*
	 * Returns the string to display for days
	 */
	private String getAgeTextInDays(int ageInDays) {
		
		String ageToDisplay = Integer.toString(ageInDays) + " ";
		
		// now if it is a single day, use the singular form of day
		if (ageInDays == 1) {
			ageToDisplay += mContext.getString(R.string.day);
		} else {
			ageToDisplay += mContext.getString(R.string.days);
		}
		
		return ageToDisplay;
	}
	
	/*
	 * Returns the string to display for weeks
	 */
	private String getAgeTextInWeeks(int ageInDays) {
				
		int ageInWeeks = (ageInDays / NUM_DAYS_IN_WEEK);
		String ageToDisplay = Integer.toString(ageInWeeks) + " ";
		
		if (ageInWeeks == 1) {
			ageToDisplay += mContext.getString(R.string.week);
		} else {
			ageToDisplay += mContext.getString(R.string.weeks);				
		}

		return ageToDisplay;
	}
	
	/*
	 * Returns the string to display for Months
	 */
	private String getAgeTextInMonths(Date birthdate, Date now, int ageInDays, int ageInYears) {
		
		// first create the age in months variable, and add any years we have
		int ageInMonths = (ageInYears * NUM_MONTHS_PER_YEAR);				
		
		// using calendar objects here to get number of months by date, 
		// rather than an arbitrary average of number of days in a month
        final Calendar birthdateCal = Calendar.getInstance();
        birthdateCal.setTimeInMillis(birthdate.getTime());
        int birthMonth = birthdateCal.get(Calendar.MONTH);
        int birthDay = birthdateCal.get(Calendar.DAY_OF_MONTH);
        
        final Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(now.getTime());
        int nowMonth = nowCal.get(Calendar.MONTH);
        int nowDay = nowCal.get(Calendar.DAY_OF_MONTH);
        
        // now get the difference in months and determine how to add the correct number of months
        int monthDiff = (nowMonth - birthMonth);
        if (monthDiff < 0) {		        
        	ageInMonths += (NUM_MONTHS_PER_YEAR + monthDiff);
        }
        // if it is the same birth month, it may be close to 12 or 24 months
        // this is a special case check - and makes the assumption that there will be more than 5 weeks
        // of weeks display and will never arrive here with months of life actually at 0
        else if ( (monthDiff == 0) && (nowDay < birthDay) ) {
        	ageInMonths += NUM_MONTHS_PER_YEAR;
        }
        // then the months should just be accurate
        else {
        	ageInMonths += monthDiff;
        }
        
        // finally, subtract a month if today's day is before the birth day
        if (nowDay < birthDay)
        {
        	ageInMonths--;
        }

        // the last check to make is for less than NUM_MONTHS_FOR_DISPLAY but over the number of 
        // weeks to display, as the number of days for months can be a little longer than the 
        // weeks, and I'd rather display weeks, than 1 month
        if (ageInMonths < NUM_MONTHS_FOR_DISPLAY) {
        	return (getAgeTextInWeeks(ageInDays) );
        }
        
		// TODO: Do we want partial months?
		String ageToDisplay = Integer.toString(ageInMonths) + " ";
		
		if (ageInMonths == 1) {
			ageToDisplay += mContext.getString(R.string.month);
		} else {
			ageToDisplay += mContext.getString(R.string.months);				
		}

		return ageToDisplay;
		
	}
	
	/*
	 * Returns the string to display for years
	 */
	private String getAgeTextInYears(int ageInYears) {
		
		// TODO: Create partial years - configurable, or just half years?
		String ageToDisplay = Integer.toString(ageInYears) + " ";
		
		if (ageInYears == 1) {
			ageToDisplay += mContext.getString(R.string.year);
		} else {
			ageToDisplay += mContext.getString(R.string.years);				
		}

		return ageToDisplay;
	}
	
}
