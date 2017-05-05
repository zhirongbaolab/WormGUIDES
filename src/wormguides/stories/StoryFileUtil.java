/*
 * Bao Lab 2017
 */

package wormguides.stories;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javafx.collections.ObservableList;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.join;

import static wormguides.stories.StoriesLoader.COLOR_URL_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_CALLOUT_OFFSETS_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_CELLNAME_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_COMMENTS_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_CONTENTS_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_DISPLAY_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_END_TIME_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_IMG_SOURCE_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_LOCATION_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_MARKER_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_NAME_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_RESOURCE_LOCATION_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_START_TIME_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_TYPE_INDEX;
import static wormguides.stories.StoriesLoader.NOTE_VISIBLE_INDEX;
import static wormguides.stories.StoriesLoader.NUMBER_OF_CSV_FIELDS;
import static wormguides.stories.StoriesLoader.STORY_AUTHOR_INDEX;
import static wormguides.stories.StoriesLoader.STORY_DATE_INDEX;
import static wormguides.stories.StoriesLoader.STORY_DESCRIPTION_INDEX;
import static wormguides.stories.StoriesLoader.STORY_NAME_INDEX;
import static wormguides.stories.StoriesLoader.loadFromFile;

/**
 * Utility methods for loading/saving {@linkplain Story stories}
 */
public class StoryFileUtil {

    private static final String CS = ",",
            BR = "\n",
            NAME = "Tag Name",
            CONTENTS = "Tag Contents",
            DISPLAY = "Tag Display",
            CALlOUT_OFFSETS = quoteForCsv("Callout Offsets (horizontal, vertical)"),
            ATTACHMENT = "Attachment Type",
            LOCATION = "XYZ Location",
            CELLS = "Cells",
            MARKER = "Marker",
            SOURCE = "Imaging Source",
            RESOURCE = "Resource Location",
            START = "Start Time",
            END = "End Time",
            COMMENTS = "Comments",
            VISIBLE = "Visible",
            AUTHOR = "Author",
            DATE = "Date",
            COLOR_URL = "Color Scheme Url";

    public static Story loadFromCSVFile(final ObservableList<Story> stories, final File file, final int offset) {
        loadFromFile(stories, file, offset);
        // the newly added story is always last in the list of stories
        return stories.get(stories.size() - 1);
    }

    public static File saveToCSVFile(final Story story, final File file, final int offset) {
        // false in file writer constructor means do not append
        try (final FileWriter fstream = new FileWriter(file, false);
             final BufferedWriter out = new BufferedWriter(fstream)) {

            // write headers
            out.append(NAME)
                    .append(CS)
                    .append(CONTENTS)
                    .append(CS)
                    .append(DISPLAY)
                    .append(CS)
                    .append(CALlOUT_OFFSETS)
                    .append(CS)
                    .append(ATTACHMENT)
                    .append(CS)
                    .append(LOCATION)
                    .append(CS)
                    .append(CELLS)
                    .append(CS)
                    .append(MARKER)
                    .append(CS)
                    .append(SOURCE)
                    .append(CS)
                    .append(RESOURCE)
                    .append(CS)
                    .append(START)
                    .append(CS)
                    .append(END)
                    .append(CS)
                    .append(COMMENTS)
                    .append(CS)
                    .append(VISIBLE)
                    .append(CS)
                    .append(AUTHOR)
                    .append(CS)
                    .append(DATE)
                    .append(CS)
                    .append(COLOR_URL)
                    .append(BR);

            // write story
            final String[] storyParams = new String[NUMBER_OF_CSV_FIELDS];
            for (int i = 0; i < NUMBER_OF_CSV_FIELDS; i++) {
                storyParams[i] = "";
            }
            storyParams[STORY_NAME_INDEX] = quoteForCsv(story.getTitle());
            storyParams[STORY_DESCRIPTION_INDEX] = quoteForCsv(story.getDescription());
            storyParams[STORY_AUTHOR_INDEX] = quoteForCsv(story.getAuthor());
            storyParams[STORY_DATE_INDEX] = quoteForCsv(story.getDate());
            storyParams[COLOR_URL_INDEX] = quoteForCsv(story.getColorUrl());
            out.append(join(",", storyParams)).append(BR);

            // notes
            for (Note note : story.getNotes()) {
                final String[] noteParams = new String[NUMBER_OF_CSV_FIELDS];
                for (int i = 0; i < NUMBER_OF_CSV_FIELDS; i++) {
                    noteParams[i] = "";
                }
                noteParams[NOTE_NAME_INDEX] = quoteForCsv(note.getTagName());
                noteParams[NOTE_CONTENTS_INDEX] = quoteForCsv(note.getTagContents());
                noteParams[NOTE_DISPLAY_INDEX] = note.getTagDisplay().toString();
                noteParams[NOTE_TYPE_INDEX] = note.getAttachmentType().toString();
                noteParams[NOTE_LOCATION_INDEX] = quoteForCsv(note.getLocationString());
                noteParams[NOTE_CELLNAME_INDEX] = note.getCellName();
                noteParams[NOTE_IMG_SOURCE_INDEX] = quoteForCsv(note.getImgSource());
                noteParams[NOTE_MARKER_INDEX] = quoteForCsv(note.getMarker());
                noteParams[NOTE_RESOURCE_LOCATION_INDEX] = quoteForCsv(note.getResourceLocation());
                // if time is not specified, do not use Integer.MIN_VALUE, leave it blank
                int start = note.getStartTime();
                int end = note.getEndTime();
                if (start != MIN_VALUE && end != MIN_VALUE) {
                    noteParams[NOTE_START_TIME_INDEX] = Integer.toString(start + offset);
                    noteParams[NOTE_END_TIME_INDEX] = Integer.toString(end + offset);
                }
                noteParams[NOTE_COMMENTS_INDEX] = note.getComments();
                if (!note.isVisible()) {
                    noteParams[NOTE_VISIBLE_INDEX] = "n";
                }
                if (note.hasColorScheme()) {
                    noteParams[COLOR_URL_INDEX] = note.getColorUrl();
                }
                if (note.isCallout()) {
                    noteParams[NOTE_CALLOUT_OFFSETS_INDEX] = quoteForCsv(
                            note.getCalloutHorizontalOffset()
                            + ", "
                            + note.getCalloutVerticalOffset());
                }
                out.append(join(",", noteParams))
                        .append(BR);
            }

        } catch (IOException e) {
            System.err.println("Error in writing to CSV file: " + e.getMessage());
        }

        return file;
    }

    /**
     * Inserts quotation marks around a field if it contains commas
     *
     * @param field
     *         the field
     *
     * @return the field that is ready for inserting into a CSV line
     */
    private static String quoteForCsv(final String field) {
        if (field != null && field.contains(",")) {
            return "\"" + field + "\"";
        }
        return field;
    }
}
