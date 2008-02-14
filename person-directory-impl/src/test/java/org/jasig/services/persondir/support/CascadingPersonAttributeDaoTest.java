/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.services.persondir.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.mock.ThrowingPersonAttributeDao;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.util.Util;

/**
 * CascadingPersonAttributeDao testcase.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class CascadingPersonAttributeDaoTest 
    extends AbstractAggregatingDefaultQueryPersonAttributeDaoTest {
    
    private ComplexStubPersonAttributeDao sourceOne;
    private ComplexStubPersonAttributeDao sourceTwo;
    private StubPersonAttributeDao nullSource;
        
    @Override
    protected void setUp() {
        Map<String, Map<String, List<Object>>> daoBackingMap1 = new HashMap<String, Map<String, List<Object>>>();
        
        Map<String, List<Object>> user1 = new HashMap<String, List<Object>>();
        user1.put("phone", Util.list("777-7777"));
        user1.put("studentId", Util.list("123456789"));
        daoBackingMap1.put("edalquist", user1);
        
        Map<String, List<Object>> user2 = new HashMap<String, List<Object>>();
        user2.put("phone", Util.list("888-8888"));
        user2.put("studentId", Util.list("987654321"));
        daoBackingMap1.put("awp9", user2);
        
        Map<String, List<Object>> user3 = new HashMap<String, List<Object>>();
        user3.put("phone", Util.list("666-6666"));
        user3.put("studentId", Util.list("000000000"));
        daoBackingMap1.put("erider", user3);
        
        this.sourceOne = new ComplexStubPersonAttributeDao();
        this.sourceOne.setBackingMap(daoBackingMap1);
        this.sourceOne.setDefaultAttributeName("username");
        

        Map<String, Map<String, List<Object>>> daoBackingMap2 = new HashMap<String, Map<String, List<Object>>>();
        
        Map<String, List<Object>> user1a = new HashMap<String, List<Object>>();
        user1a.put("phone", Util.list("777-7777x777"));
        user1a.put("major", Util.list("CS"));
        daoBackingMap2.put("123456789", user1a);
        
        Map<String, List<Object>> user2a = new HashMap<String, List<Object>>();
        user2a.put("phone", Util.list("888-8887x888"));
        user2a.put("major", Util.list("ME"));
        daoBackingMap2.put("987654321", user2a);
        
        Map<String, List<Object>> user3a = new HashMap<String, List<Object>>();
        user3a.put("phone", Util.list("666-6666x666"));
        user3a.put("major", Util.list("EE"));
        daoBackingMap2.put("000000000", user3a);
        
        this.sourceTwo = new ComplexStubPersonAttributeDao();
        this.sourceTwo.setBackingMap(daoBackingMap2);
        this.sourceTwo.setDefaultAttributeName("studentId");
        
        
        this.nullSource = new StubPersonAttributeDao();
    }
    
    public void testCascadingQuery() {
        final List<IPersonAttributeDao> targets = new ArrayList<IPersonAttributeDao>();
        targets.add(this.sourceOne);
        targets.add(this.nullSource);
        targets.add(this.sourceTwo);
        
        final CascadingPersonAttributeDao targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());
        
        final Map<String, List<Object>> results = targetDao.getMultivaluedUserAttributes("edalquist");
        
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.put("studentId", Util.list("123456789"));
        expected.put("major", Util.list("CS"));
        expected.put("phone", Util.list("777-7777", "777-7777x777"));
        
        assertEquals(expected, results);
    }

    public void testNoChildren() {
        final CascadingPersonAttributeDao targetDao = new CascadingPersonAttributeDao();
        
        try {
            targetDao.getMultivaluedUserAttributes("edalquist");
            fail("IllegalStateException should have been thrown with no child DAOs");
        }
        catch (IllegalStateException ise) {
            //expected
        }
    }
    
    public void testThrowingChildDao() {
        final List<IPersonAttributeDao> targets = new ArrayList<IPersonAttributeDao>();
        targets.add(this.sourceOne);
        targets.add(new ThrowingPersonAttributeDao());
        targets.add(this.sourceTwo);
        
        final CascadingPersonAttributeDao targetDao = new CascadingPersonAttributeDao();
        targetDao.setPersonAttributeDaos(targets);
        targetDao.setMerger(new MultivaluedAttributeMerger());
        
        
        targetDao.setRecoverExceptions(true);
        Map<String, List<Object>> results = targetDao.getMultivaluedUserAttributes("edalquist");
        
        Map<String, List<Object>> expected = new HashMap<String, List<Object>>();
        expected.put("studentId", Util.list("123456789"));
        expected.put("major", Util.list("CS"));
        expected.put("phone", Util.list("777-7777","777-7777x777"));
        
        assertEquals(expected, results);
        
        
        
        targetDao.setRecoverExceptions(false);
        try {
            targetDao.getMultivaluedUserAttributes("edalquist");
            fail("RuntimeException should have been thrown with no child DAOs");
        }
        catch (RuntimeException ise) {
            //expected
        }
    }
    
    /**
     * @see org.jasig.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getConfiguredAbstractAggregatingDefaultQueryPersonAttributeDao() {
        List<IPersonAttributeDao> attributeSources = new ArrayList<IPersonAttributeDao>();
        
        attributeSources.add(this.sourceOne);
        attributeSources.add(this.sourceTwo);
        
        final CascadingPersonAttributeDao impl = new CascadingPersonAttributeDao();
        impl.setPersonAttributeDaos(attributeSources);
        
        return impl;
    }

    /**
     * @see org.jasig.services.persondir.support.AbstractAggregatingDefaultQueryPersonAttributeDaoTest#getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao()
     */
    @Override
    protected AbstractAggregatingDefaultQueryPersonAttributeDao getEmptyAbstractAggregatingDefaultQueryPersonAttributeDao() {
        return new CascadingPersonAttributeDao();
    }
}