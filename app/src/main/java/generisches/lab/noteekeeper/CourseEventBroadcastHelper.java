package generisches.lab.noteekeeper;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastHelper {
    public static final String ACTION_COURSE_EVENT = "generisches.lab.noteekeeper.action.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID = "generisches.lab.noteekeeper.extra.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE = "generisches.lab.noteekeeper.extra.COURSE_MESSAGE";

    public static void sendEventBroadcast(Context context,String courseId, String message){
        Intent i = new Intent(ACTION_COURSE_EVENT);
        i.putExtra(EXTRA_COURSE_ID, courseId);
        i.putExtra(EXTRA_COURSE_MESSAGE, message);

        context.sendBroadcast(i);
    }
}
