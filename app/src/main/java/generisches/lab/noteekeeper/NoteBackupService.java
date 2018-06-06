package generisches.lab.noteekeeper;

import android.app.IntentService;
import android.content.Intent;

public class NoteBackupService extends IntentService {

    public static final String EXTRA_COURSE_ID = "generisches.lab.noteekeeper.extra.COURSE_ID";

    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra(EXTRA_COURSE_ID);
            //Service passed as context
            NoteBackup.doBackup(this, backupCourseId);
        }
    }
}
