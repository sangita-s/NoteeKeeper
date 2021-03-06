package generisches.lab.noteekeeper;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import generisches.lab.noteekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import generisches.lab.noteekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import generisches.lab.noteekeeper.NoteKeeperProviderContract.Courses;

public class NoteActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "generisches.lab.noteekeeper.NOTE_ID";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mMOriginalNoteTitle;
    private String mMOriginalNoteText;

    //Save for onRestart bundle
    public static final String ORIGINAL_NOTE_COURSE_ID = "generisches.lab.noteekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "generisches.lab.noteekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "generisches.lab.noteekeeper.ORIGINAL_NOTE_TEXT";
    public static final String NOTE_URI = "generisches.lab.noteekeeper.NOTE_URI";
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapter_courses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;
    private ModuleStatusView mViewModuleStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSpinnerCourses = findViewById(R.id.spinner_courses);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        mAdapter_courses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},0);
        mAdapter_courses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapter_courses);

        getLoaderManager().initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
            String stringNoteUri = savedInstanceState.getString(NOTE_URI);
            mNoteUri = Uri.parse(stringNoteUri);
        }

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote) {
            //this  - to comeback to this class
            getLoaderManager().initLoader(LOADER_NOTES, null, this);
        }
        mViewModuleStatus = findViewById(R.id.module_status);
        loadModuleStatusValues();
        Log.e(TAG, "OnCreate");
    }

    private void loadModuleStatusValues() {
        //look up val from content provider
        int totalNoOfModules = 11;
        int completedNoOfVariables = 7;
        boolean[] moduleStatus = new boolean[totalNoOfModules];
        for(int moduleIndex = 0; moduleIndex < completedNoOfVariables; moduleIndex++){
            moduleStatus[moduleIndex] = true;
        }

        mViewModuleStatus.setModuleStatus(moduleStatus);
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID};
        Cursor lCursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null,null,CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapter_courses.changeCursor(lCursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
                null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mMOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mMOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote) {
            return;
        }
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mMOriginalNoteTitle = mNote.getTitle();
        mMOriginalNoteText = mNote.getText();

    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);

        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing Note");
        //TODO: Create an app with receiver
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapter_courses.getCursor();
        int courseidPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more){
            String cursorCourseId = cursor.getString(courseidPos);
            if(courseId.equals(cursorCourseId)){
                break;
            }
            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent i = getIntent();
        //If extra wasnt found in the intent - POS_NOT_SET = -1
        mNoteId = i.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
        Log.i(TAG, "mNoteId: " + mNoteId);
    }

    private void createNewNote() {

        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                Log.d(TAG, "do in bg - " + Thread.currentThread().getId());
                ContentValues insertValues = contentValues[0];
                Uri rowUri = getContentResolver().insert(NoteKeeperProviderContract.Notes.CONTENT_URI, insertValues);
                simulateLongRunningWork();
                publishProgress(2);
                simulateLongRunningWork();
                publishProgress(3);
                return rowUri;//goes to onpostexecute
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                Log.d(TAG, "Post exec - " + Thread.currentThread().getId());
                mNoteUri = uri;
                displaySnackbar(mNoteUri.toString());
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        };

        ContentValues values = new ContentValues();
        values.put(NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID, "");
        values.put(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE, "");
        values.put(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT, "");

        Log.d(TAG, "Call to thread - " + Thread.currentThread().getId());
        task.execute(values);
    }

    private void displaySnackbar(String s) {
        View v = findViewById(R.id.spinner_courses);
        Snackbar.make(v, s, Snackbar.LENGTH_SHORT).show();
    }

    private void simulateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(Exception ex) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            Log.i(TAG, "Cancelling note at position: " + mNoteId);
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void deleteNoteFromDatabase() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                getContentResolver().delete(mNoteUri, null, null);
                return null;
            }
        };
        task.execute();

    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mMOriginalNoteTitle);
        mNote.setText(mMOriginalNoteText);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mMOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mMOriginalNoteText);

        outState.putString(NOTE_URI, mNoteUri.toString());
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNotetoDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapter_courses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNotetoDatabase(String courseId, String noteTitle, String noteText){
        ContentValues values = new ContentValues();
        values.put(NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID, courseId);
        values.put(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT, noteText);

        getContentResolver().update(mNoteUri, values, null, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if(id == R.id.action_next){
            moveNext();
        }else if(id == R.id.action_set_reminder){
            setReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setReminderNotification() {
        String noteText = mTextNoteTitle.getText().toString();
        String noteTitle = mTextNoteText.getText().toString();
        int noteId = (int)ContentUris.parseId(mNoteUri);
//        NoteReminderNotification.notify(this, noteTitle, noteText, noteId);
        Intent i = new Intent(this, NoteReminderReceiver.class);
        i.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        i.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        i.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

        PendingIntent lPendingIntent = PendingIntent.getBroadcast(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager lAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeInMilliseconds = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60*60*1000;
        long ten_secs = 10*1000;
        long alarmTime = currentTimeInMilliseconds + ten_secs;

        lAlarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, lPendingIntent);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem lMenuItem = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        lMenuItem.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String sub = mTextNoteTitle.getText().toString();
        String text = "Check out what I learnt in Pluralsight! \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc2822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(emailIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES){
            loader = createLoaderNotes();
        }
        else if(id == LOADER_COURSES){
            loader = createLoaderCourses();
        }
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;

        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID};
        return new CursorLoader(this, uri, courseColumns, null, null,
                Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createLoaderNotes() {
        //Return cursor loader instance
        mNotesQueryFinished = false;
        String[] noteColumns = {
                NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID,
                NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE,
                NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT
        };

        mNoteUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);

    }

    //When data ready
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Data from cursorLoader in createloadernotes which is called from oncreate loader
        if(loader.getId() == LOADER_NOTES){
            loadFinishNotes(data);
        }
        else if(loader.getId() == LOADER_COURSES){
            mAdapter_courses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueryFinished();
        }
    }

    private void loadFinishNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        mNotesQueryFinished = true;
        displayNoteWhenQueryFinished();
    }

    private void displayNoteWhenQueryFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished)
            displayNote();
    }

    //TO clean up data
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            if(mNoteCursor != null)
                mNoteCursor.close();
        }
        else if(loader.getId() == LOADER_COURSES){
            mAdapter_courses.changeCursor(null);
        }
    }
}
