/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-9/license-header.txt
 */
package org.jasig.services.persondir.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.mock.ThrowingPersonAttributeDao;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.util.Util;

/**
 * Provides base tests for classes that implement AbstractAggregatingDefaultQueryPersonAttributeDao.
 * 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractAggregatingDefaultQueryPersonAttributeDaoTest extends AbstractDefaultQueryPersonAttributeDaoTest {
    /**
     * @see org.jasig.services.persondir.support.AbstractDefaultQueryPersonAttributeDaoTest#getAbstractDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractDefaultAttributePersonAttributeDao getAbstractDefaultQueryPersonAttributeDao() {
        return this.getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao();
    }

    protected abstract AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao();

    protected abstract AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
 
    
    public void testGetPossibleNamesWithException() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        final Map<String, List<Object>> attrMap1 = new HashMap<String, List<Object>>();
        attrMap1.put("key1.1", Util.list("val1.1"));
        attrMap1.put("key1.2", Util.list("val1.2"));
        
        final Map<String, List<Object>> attrMap2 = new HashMap<String, List<Object>>();
        attrMap1.put("key2.1", Util.list("val2.1"));
        attrMap1.put("key2.2", Util.list("val2.2"));
        
        final Set<String> expectedNames = new HashSet<String>();
        expectedNames.addAll(attrMap1.keySet());
        expectedNames.addAll(attrMap2.keySet());
        
        final List<IPersonAttributeDao> childDaos = new ArrayList<IPersonAttributeDao>(3);
        childDaos.add(new StubPersonAttributeDao(attrMap1));
        childDaos.add(new ThrowingPersonAttributeDao());
        childDaos.add(new StubPersonAttributeDao(attrMap2));
        
        dao.setPersonAttributeDaos(childDaos);
        
        //Test exception recovery
        dao.setRecoverExceptions(true);
        final Set<String> resultNames = dao.getPossibleUserAttributeNames();
        assertEquals(expectedNames, resultNames);
        
        //Test fail on exception
        dao.setRecoverExceptions(false);
        try {
            dao.getPossibleUserAttributeNames();
            fail("Expected RuntimeException on getPossibleUserAttributeNames() with ThrowingPersonAttributeDao as a child DAO");
        } 
        catch (RuntimeException re) {
            //expected
        }
    }
 
    
    public void testStopOnSuccess() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        final Map<String, List<Object>> attrMap1 = new HashMap<String, List<Object>>();
        attrMap1.put("username", Util.list("test"));
        attrMap1.put("key1.1", Util.list("val1.1"));
        attrMap1.put("key1.2", Util.list("val1.2"));
        
        final Map<String, List<Object>> attrMap2 = new HashMap<String, List<Object>>();
        attrMap2.put("username", Util.list("test"));
        attrMap2.put("key2.1", Util.list("val2.1"));
        attrMap2.put("key2.2", Util.list("val2.2"));
        
        final Set<String> expectedNamesWithStop = new HashSet<String>();
        expectedNamesWithStop.addAll(attrMap1.keySet());
        
        final Set<String> expectedNamesWithoutStop = new HashSet<String>();
        expectedNamesWithoutStop.addAll(attrMap1.keySet());
        expectedNamesWithoutStop.addAll(attrMap2.keySet());
        
        final List<IPersonAttributeDao> childDaos = new ArrayList<IPersonAttributeDao>(3);
        childDaos.add(new ThrowingPersonAttributeDao());
        childDaos.add(new StubPersonAttributeDao(attrMap1));
        childDaos.add(new StubPersonAttributeDao(attrMap2));
        
        dao.setPersonAttributeDaos(childDaos);

        
        dao.setStopOnSuccess(true);
        
        final Set<String> resultNamesWithStop = dao.getPossibleUserAttributeNames();
        assertEquals(expectedNamesWithStop, resultNamesWithStop);
        
        final IPersonAttributes personWithStop = dao.getPerson("test");
        assertEquals(new AttributeNamedPersonImpl(attrMap1), personWithStop);
        
        
        dao.setStopOnSuccess(false);
        
        final Set<String> resultNamesWithoutStop = dao.getPossibleUserAttributeNames();
        assertEquals(expectedNamesWithoutStop, resultNamesWithoutStop);
        
        final IPersonAttributes personWithoutStop = dao.getPerson("test");
        assertEquals(new AttributeNamedPersonImpl(attrMap1), personWithoutStop);
        
    }
    
    public void testSetNullMerger() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        try {
            dao.setMerger(null);
            fail("Expected IllegalArgumentException on setMerger(null)");
        } 
        catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    public void testSetNullPersonAttributeDaos() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        try {
            dao.setPersonAttributeDaos(null);
            fail("Expected IllegalArgumentException on setPersonAttributeDaos(null)");
        } 
        catch (IllegalArgumentException iae) {
            //expected
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testProperties() {
        final AbstractAggregatingDefaultQueryPersonAttributeDao dao = this.getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao();
        
        final MultivaluedAttributeMerger merger = new MultivaluedAttributeMerger();
        dao.setMerger(merger);
        assertEquals(merger, dao.getMerger());
        
        dao.setPersonAttributeDaos(Collections.EMPTY_LIST);
        assertEquals(Collections.EMPTY_LIST, dao.getPersonAttributeDaos());
        
        dao.setRecoverExceptions(true);
        assertTrue(dao.isRecoverExceptions());
    }
}
