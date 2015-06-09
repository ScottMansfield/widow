/**
 * Copyright 2015 Scott Mansfield
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.widowcrawler.core.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Scott Mansfield
 */
public class DomainUtilsTest {

    @Test
    public void isBaseDomain_isBaseDomain_returnsTrue() {
        // Arrange
        String baseDomain = "xoxide.com";
        String url = "http://www.xoxide.com/powersupplies.html";

        // Act
        boolean result = DomainUtils.isBaseDomain(baseDomain, url);

        // Assert
        assertTrue(result);
    }

    @Test
    public void isBaseDomain_wrongDomain_returnsFalse() {
        // Arrange
        String baseDomain = "xoxide.com";
        String url = "http://www.google.com/foo.html";

        // Act
        boolean result = DomainUtils.isBaseDomain(baseDomain, url);

        // Assert
        assertFalse(result);
    }

    @Test
    public void isBaseDomain_noDomain_returnsFalse() {
        // Arrange
        String baseDomain = "xoxide.com";
        String url = "/powersupplies.html";

        // Act
        boolean result = DomainUtils.isBaseDomain(baseDomain, url);

        // Assert
        assertFalse(result);
    }

    @Test
    public void isBaseDomain_badUrl_returnsFalse() {
        // Arrange
        String baseDomain = "xoxide.com";
        String url = "hfn:/fdbsdb nl]kmnasdbfnjvah7sg967*&06r5*E$^%&^*&(*ymhvi";

        // Act
        boolean result = DomainUtils.isBaseDomain(baseDomain, url);

        // Assert
        assertFalse(result);
    }
}
