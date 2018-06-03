package generisches.lab.noteekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() throws Exception{
        sDataManager = DataManager.getInstance();
    }

    @Before
    public void setup() throws Exception{
        //final DataManager dm = DataManager.getInstance();
        sDataManager.getNotes().clear();
        sDataManager.initializeExampleNotes();
    }

    @Test
    public void createNewNote() throws Exception{
        //final DataManager dm = DataManager.getInstance();
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test Note Titlee";
        final String NoteText = "Test Note Text";

        int noteIndex = sDataManager.createNewNote();
        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setText(noteTitle);
        newNote.setText(NoteText);

        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
        //assertSame(compareNote, newNote);
        assertEquals(course, compareNote.getCourse());
        //assertEquals(noteTitle, compareNote.getTitle());
        assertEquals(NoteText, compareNote.getText());
    }

    @Test
    public void findSimilarNotes() throws Exception{
        //final DataManager dm = DataManager.getInstance();
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle1 = "Test Note Title1";
        final String noteTitle2 = "Test Note Title2";
        final String NoteText1 = "Test Note Text 1";
        final String NoteText2 = "Test Note Text 2";

        int noteIndex1 = sDataManager.createNewNote();
        NoteInfo newNote1 = sDataManager.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setText(NoteText1);
        newNote1.setText(noteTitle1);

        int noteIndex2 = sDataManager.createNewNote();
        NoteInfo newNote2 = sDataManager.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setText(NoteText2);
        newNote2.setText(noteTitle2);

        int foundindex1 = sDataManager.findNote(newNote1);
        assertEquals(noteIndex1, foundindex1);

        int foundindex2 = sDataManager.findNote(newNote2);
        assertEquals(noteIndex2, foundindex2);
    }
}