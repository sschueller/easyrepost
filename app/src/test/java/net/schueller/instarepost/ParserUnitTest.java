package net.schueller.instarepost;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static net.schueller.instarepost.helpers.Parser.matchInstagramUri;

import android.content.Context;
import android.os.Build;
import android.test.suitebuilder.annotation.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;
import org.junit.runner.*;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
@LargeTest
public class ParserUnitTest {

    @Test
    public void testMatchInstagramUri() {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // matches
        assertEquals("https://www.instagram.com/p/CDRGNQTHh7X/",
                matchInstagramUri("https://www.instagram.com/p/CDRGNQTHh7X/?igshid=1opjbfc2u28wa",
                        context));

        assertEquals("https://www.instagram.com/tv/CDRGNQTHh7X/",
                matchInstagramUri("https://www.instagram.com/tv/CDRGNQTHh7X/",
                        context));

        //no matches
        assertNull(matchInstagramUri("https://www.instagram.com/r/CDRGN",
                context));
        assertNull(
                matchInstagramUri("https://www.instagram.com/p/CDRGNQTHh7X",
                        context));
        assertNull(matchInstagramUri("https://www.google.com/p/CDRGNQTHh7X/?igshid=1opjbfc2u28wa",
                context));
    }
}