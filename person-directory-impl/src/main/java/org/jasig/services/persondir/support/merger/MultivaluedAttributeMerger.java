/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support.merger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;


/**
 * Merger that retains values from both maps. If a value exists for
 * a key in both maps the following is done:
 * <ul>
 *  <li>If both maps have a {@link List} they are merged into a single {@link List}</li>
 *  <li>If one map has a {@link List} and the other a single value the value is added to the {@link List}</li>
 *  <li>If both maps have a single value a {@link List} is created from the two.</li>
 * </ul>
 * 
 * @author Eric Dalquist
 * @version $Revision$ $Date$
 */
public class MultivaluedAttributeMerger extends BaseAdditiveAttributeMerger {
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.merger.BaseAdditiveAttributeMerger#mergePersonAttributes(java.util.Map, java.util.Map)
     */
    @Override
    protected Map<String, List<Object>> mergePersonAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider) {
        Validate.notNull(toModify, "toModify cannot be null");
        Validate.notNull(toConsider, "toConsider cannot be null");
        
        for (final Map.Entry<String, List<Object>> sourceEntry : toConsider.entrySet()) {
            final String sourceKey = sourceEntry.getKey();
            
            List<Object> destList = toModify.get(sourceKey);
            if (destList == null) {
                destList = new LinkedList<Object>();
                toModify.put(sourceKey, destList);
            }
            
            final List<Object> sourceValue = sourceEntry.getValue();
            destList.addAll(sourceValue);
        }
        
        return toModify;
    }
}
