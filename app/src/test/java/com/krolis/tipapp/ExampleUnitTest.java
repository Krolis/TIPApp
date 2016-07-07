package com.krolis.tipapp;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    @Test
    public void testFinalGradeValue(){
        HashMap hashMap = new HashMap();
        hashMap.put("4.0","12-13-2213");
        hashMap.put("2.0", "12-13-2213");
        hashMap.put("3.0", "12-13-2213");

        Grade g = new Grade("SUBJECT", hashMap);

        assertEquals(4.0, SemesterAdapter.getFinalGrade(g), 0.1);

        HashMap temp2 = new HashMap();
        temp2.put("X", "22-22-2222");

        Grade g2 = new Grade("SUBJECT", temp2);

        assertEquals(2.0, SemesterAdapter.getFinalGrade(g2), 0.1);

        HashMap temp3 = new HashMap();
        temp3.put("X", "22-22-2222");
        temp3.put("Z", "33-33-3333");

        Grade g3 = new Grade("SUBJECT",temp3);

        assertEquals(-1.0, SemesterAdapter.getFinalGrade(g3), 0.1);
    }
}