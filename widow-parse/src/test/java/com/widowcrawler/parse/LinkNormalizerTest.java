package com.widowcrawler.parse;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Scott Mansfield
 */
public class LinkNormalizerTest {

    private LinkNormalizer linkNormalizer;

    @Before
    public void before() {
        this.linkNormalizer = new LinkNormalizer();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void normalize_originalIsNull_throwsNullPointerException() {
        // Arrange
        String original = null;
        String extracted = "http://www.baz.com/quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals(extracted, output);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void normalize_extractedIsNull_throwsNullPointerException() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = null;

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        // nothing to assert
    }

    @Test(expected = IllegalArgumentException.class)
    public void normalize_originalIsBlank_throwsIllegalArgumentException() {
        // Arrange
        String original = "";
        String extracted = "http://www.baz.com/quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        // nothing to assert
    }

    @Test
    public void normalize_extractedIsBadURI_returnsNull() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = "ij><g8o.hp(<G^F*";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertNull(output);
    }

    @Test
    public void normalize_extractedIsFullURI_noChange() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = "http://www.baz.com/quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        // nothing to assert
    }

    @Test
    public void normalize_extractedIsPathOnly_addsSchemeAndHost() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = "/quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals("http://www.foo.com/quux", output);
    }

    @Test
    public void normalize_extractedIsPathOnlyNoSlash_addsSchemeAndHost() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = "quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals("http://www.foo.com/quux", output);
    }

    @Test
    public void normalize_extractedIsProtocolRelative_addsScheme() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = "//www.baz.com/quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals("http://www.baz.com/quux", output);
    }

    @Test
    public void normalize_extractedIsFullURIWithBadPath_noChange() {
        // Arrange
        String original = "http://www.foo.com/bar";
        String extracted = "http://www.baz.com/../quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals(extracted, output);
    }

    @Test
    public void normalize_extractedContainsRelativePathWithDirectoryChange_directoryChanged() {
        // Arrange
        String original = "http://www.foo.com/bar/baz";
        String extracted = "../quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals("http://www.foo.com/quux", output);
    }

    @Test
    public void normalize_extractedContainsRelativePathWithDirectoryChangeAndOriginalIsDirectory_directoryChanged() {
        // Arrange
        String original = "http://www.foo.com/bar/baz/";
        String extracted = "../quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals("http://www.foo.com/bar/quux", output);
    }

    @Test
    public void normalize_extractedContainsRelativePathWithExtraDirectoryChange_directoryChangedButStops() {
        // Arrange
        String original = "http://www.foo.com/bar/baz";
        String extracted = "../../quux";

        // Act
        String output = linkNormalizer.normalize(original, extracted);

        // Assert
        assertEquals("http://www.foo.com/../quux", output);
    }
}
