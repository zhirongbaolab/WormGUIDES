/*
 * Bao Lab 2017
 */

package wormguides.stories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javafx.collections.ObservableList;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.addAll;

/**
 * Loader for stories specified in the internal stories config file
 */
public class StoriesLoader {

    public static final int NUMBER_OF_CSV_FIELDS = 17;

    public static final int STORY_NAME_INDEX = 0,
            STORY_DESCRIPTION_INDEX = 1,
            STORY_AUTHOR_INDEX = 14,
            STORY_DATE_INDEX = 15;

    public static final int NOTE_NAME_INDEX = 0,
            NOTE_CONTENTS_INDEX = 1,
            NOTE_DISPLAY_INDEX = 2,
            NOTE_CALLOUT_OFFSETS_INDEX = 3,
            NOTE_TYPE_INDEX = 4,
            NOTE_LOCATION_INDEX = 5,
            NOTE_CELLNAME_INDEX = 6,
            NOTE_MARKER_INDEX = 7,
            NOTE_IMG_SOURCE_INDEX = 8,
            NOTE_RESOURCE_LOCATION_INDEX = 9,
            NOTE_START_TIME_INDEX = 10,
            NOTE_END_TIME_INDEX = 11,
            NOTE_COMMENTS_INDEX = 12,
            NOTE_VISIBLE_INDEX = 13;

    public static final int COLOR_URL_INDEX = 16;

    private static final String STORY_LIST_CONFIG = "/wormguides/stories/StoryListConfig.csv";

    public static void loadFromFile(final ObservableList<Story> stories, final File file, final int offset) {
        if (file != null) {
            try (final InputStream stream = new FileInputStream(file)) {
                processStream(stream, stories, offset);
            } catch (IOException ioe) {
                System.out.println("Could not read file '" + file.getName() + "' in the system.");
            }
        }
    }

    public static void loadConfigFile(final ObservableList<Story> stories, final int offset) {
        final URL url = StoriesLoader.class.getResource(STORY_LIST_CONFIG);
        if (url != null) {
            try (final InputStream stream = url.openStream()) {
                processStream(stream, stories, offset);
            } catch (IOException e) {
                System.out.println("Could not read file '" + STORY_LIST_CONFIG + "' in the system.");
            }
        }
    }

    private static void processStream(
            final InputStream stream,
            final ObservableList<Story> stories,
            final int offset) {
        // used for accessing the current story for adding scene elements
        int storyCounter = stories.size() - 1;

        try {
            final InputStreamReader streamReader = new InputStreamReader(stream);
            final BufferedReader reader = new BufferedReader(streamReader);

            String line;

            // Skip heading line
            reader.readLine();
            final List<String> lineTokens = new LinkedList<>();
            String[] split;

            while ((line = reader.readLine()) != null) {
                split = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (split.length != NUMBER_OF_CSV_FIELDS) {
                    // if this is the first of incomplete line of params for this one note
                    // then just add tokens to list of tokens
                    if (lineTokens.isEmpty()) {
                        addAll(lineTokens, split);
                    } else {
                        // if this is not the first line,
                        // then modify the previous final token and append the remaining
                        int lastIndex = lineTokens.size() - 1;
                        final String lastToken = lineTokens.get(lastIndex);
                        lineTokens.remove(lastIndex);
                        lineTokens.add(lastToken + split[0]);
                        lineTokens.addAll(asList(split).subList(1, split.length));
                    }
                } else {
                    addAll(lineTokens, split);
                }

                if (lineTokens.size() == NUMBER_OF_CSV_FIELDS) {
                    split = lineTokens.toArray(new String[NUMBER_OF_CSV_FIELDS]);
                    // get rid of quotes in story description/note contents since field might have contained commas
                    String contents = split[NOTE_CONTENTS_INDEX];
                    if (contents.startsWith("\"") && contents.endsWith("\"")) {
                        split[NOTE_CONTENTS_INDEX] = contents.substring(1, contents.length() - 1);
                    }

                    // if line makes up a story
                    if (isStory(split)) {
                        // remove quotes from author line, since a comma might exist in that field
                        String author = split[STORY_AUTHOR_INDEX];
                        if (author.startsWith("\"") && author.endsWith("\"")) {
                            author = author.substring(1, author.length() - 1);
                        }
                        stories.add(new Story(
                                split[STORY_NAME_INDEX],
                                split[STORY_DESCRIPTION_INDEX],
                                author,
                                split[STORY_DATE_INDEX],
                                split[COLOR_URL_INDEX]));
                        storyCounter++;

                    } else {
                        // if line makes up a note, create the note and add it to story
                        addNoteToStory(
                                stories.get(storyCounter),
                                split,
                                offset);
                    }

                    lineTokens.clear();
                }
            }
            reader.close();

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Unable to process Url file.");

        } catch (NumberFormatException e) {
            System.out.println("Number format error in file.");

        } catch (IOException e) {
            System.out.println("Config file was not found.");
        }
    }

    private static Note addNoteToStory(final Story story, final String[] split, final int offset) {
        final Note note = new Note(story, split[NOTE_NAME_INDEX], split[NOTE_CONTENTS_INDEX]);
        story.addNote(note);

        try {
            note.setTagDisplay(split[NOTE_DISPLAY_INDEX]);
            note.setAttachmentType(split[NOTE_TYPE_INDEX]);
            note.setLocation(split[NOTE_LOCATION_INDEX]);
            note.setCellName(split[NOTE_CELLNAME_INDEX]);

            note.setImagingSource(split[NOTE_IMG_SOURCE_INDEX]);
            note.setResourceLocation(split[NOTE_RESOURCE_LOCATION_INDEX]);

            final String startTime = split[NOTE_START_TIME_INDEX];
            final String endTime = split[NOTE_END_TIME_INDEX];
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                note.setStartTime(parseInt(startTime) - offset);
                note.setEndTime(parseInt(endTime) - offset);
            }

            note.setComments(split[NOTE_COMMENTS_INDEX]);

            final String visible = split[NOTE_VISIBLE_INDEX];
            if (visible.equalsIgnoreCase("n")) {
                note.setVisible(false);
            }

            note.setColorUrl(split[COLOR_URL_INDEX]);

            if (!split[NOTE_CALLOUT_OFFSETS_INDEX].isEmpty()) {
                // remove quotes inserted from Excel
                String offsetsString = split[NOTE_CALLOUT_OFFSETS_INDEX];
                if (offsetsString.startsWith("\"") && offsetsString.endsWith("\"")) {
                    offsetsString = offsetsString.substring(1, offsetsString.length() - 1);
                }
                final String[] offsetTokens = offsetsString.split(",");
                note.setCalloutHorizontalOffset((int) parseDouble(offsetTokens[0].trim()));
                note.setCalloutVerticalOffset((int) parseDouble(offsetTokens[1].trim()));
            }

        } catch (Exception e) {
            System.out.println("Error trying to parse the following note params:");
            System.out.println(String.join(", ", split));
            e.printStackTrace();
        }

        return note;
    }

    private static boolean isStory(String[] csvLine) {
        try {
            if (csvLine[NOTE_DISPLAY_INDEX].isEmpty() && csvLine[NOTE_TYPE_INDEX].isEmpty()) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }
}
