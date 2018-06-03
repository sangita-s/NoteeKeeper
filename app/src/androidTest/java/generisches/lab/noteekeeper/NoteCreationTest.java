package generisches.lab.noteekeeper;

import android.support.test.runner.AndroidJUnit4;
//import android.support.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() throws Exception{
        sDataManager = DataManager.getInstance();
    }
    @Rule
    //public ActivityTestRule<NoteListActivity> mNoteListActivityActivityRule =
    //        ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote(){
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.text_note_title)).perform(typeText("New Title"));
        onView(withId(R.id.text_note_text)).perform(typeText("New Text"),closeSoftKeyboard());
    }
}