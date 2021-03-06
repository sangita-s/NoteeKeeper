package generisches.lab.noteekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract(){}

    public static final String AUTHORITY = "generisches.lab.noteekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    protected interface CoursesIdColumns {
        String COLUMN_COURSE_ID = "course_id";
    }

    protected interface CourseColumns{
        String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NotesColumns{
        String COLUMN_NOTE_TITLE = "note_title";
        String COLUMN_NOTE_TEXT = "note_text";
    }

    public static final class Courses implements BaseColumns, CoursesIdColumns, CourseColumns{
        public static final String PATH = "courses";
        //content://generisches.lab.noteekeeper.provider/courses
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }
    public static final class Notes implements BaseColumns, CoursesIdColumns, NotesColumns, CourseColumns{
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
        public static final String PATH_EXPANDED = "notes_expanded";
        public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);
    }
}
