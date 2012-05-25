/*
 * Copyright (C) Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.utils;

import static com.google.light.server.utils.ObjectifyUtils.commitTransaction;
import static com.google.light.server.utils.ObjectifyUtils.getKey;
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.server.utils.ObjectifyUtils.rollbackTransactionIfStillActive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ConcurrentModificationException;

import org.junit.Test;

import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyOpts;

/**
 * Test for {@link ObjectifyUtils}
 * 
 * @author Arjun Satyapal
 */
public class ObjectifyUtilsTest extends AbstractLightServerTest {
  // TODO(arjuns) : Add more tests here.

  /**
   * Test for {@link ObjectifyUtils#getKey(Class, Long)}
   */
  @Test
  public void test_getKey_long() {
    Key<PersonEntity> expected = new Key<PersonEntity>(PersonEntity.class, 12345678L);
    assertEquals(expected, getKey(PersonEntity.class, 12345678L));

    // Negative : some other value.
    assertFalse(expected.equals(getKey(PersonEntity.class, 1234L)));

    // Negative Testing : Passing string.
    assertFalse(expected.equals(getKey(PersonEntity.class, "12345678")));
  }

  /**
   * Test for {@link ObjectifyUtils#getKey(Class, Long)}
   */
  @Test
  public void test_getKey_string() {
    Key<PersonEntity> expected = new Key<PersonEntity>(PersonEntity.class, "12345678");
    assertEquals(expected, getKey(PersonEntity.class, "12345678"));

    // Negative : Some other value.
    assertFalse(expected.equals(getKey(PersonEntity.class, "1234")));

    // Negative Testing : Passing Long.
    assertFalse(expected.equals(getKey(PersonEntity.class, 12345678L)));
  }

  /**
   * Test for {@link ObjectifyUtils#getObjectifyOptions(boolean, boolean, boolean)}
   */
  @Test
  public void test_getObjectifyOptions() {
    ObjectifyOpts options = ObjectifyUtils.getObjectifyOptions(false/* globalCacheEnabled */,
        true/* sessionCacheEnabled */, false/* txnEnabled */);
    assertFalse(options.getGlobalCache());
    assertTrue(options.getSessionCache());
    assertFalse(options.getBeginTransaction());
  }

  /**
   * Test for {@link ObjectifyUtils#initiateTransaction()}.
   */
  @Test
  public void test_initiateTransaction() {
    Objectify txn = ObjectifyUtils.initiateTransaction();
    assertNotNull(txn);
    try {
      assertTrue(txn.getTxn().isActive());
    } finally {
      commitTransaction(txn);
    }
  }

  /**
   * Test for {@link ObjectifyUtils#nonTransaction()}.
   */
  @Test
  public void test_nonTransaction() {
    Objectify txn = ObjectifyUtils.nonTransaction();
    assertNotNull(txn);
    assertNull(txn.getTxn());
  }

  /**
   * Test for {@link ObjectifyUtils#commitTransaction(Objectify)}.
   */
  @Test
  public void test_commitTransaction() {
    Objectify txn = initiateTransaction();
    commitTransaction(txn);
    assertNotNull(txn.getTxn());
    assertFalse(txn.getTxn().isActive());
  }
  
  /**
   * Test for {@link ObjectifyUtils#rollbackTransaction(Objectify)}
   */
  @Test
  public void test_rollbackTransaction() {
    Objectify txn = initiateTransaction();
    rollbackTransactionIfStillActive(txn);
    assertNotNull(txn.getTxn());
    assertFalse(txn.getTxn().isActive());
  }

  private static final String TOO_MUCH_FAKE_CONTENTION_MESSAGE = "Too much fake contention";
  private static class FakeFailureTransactable implements Transactable<String> {
    int runCount = 0;
    final int succeedOnCount;
    final String returnString;
    
    public FakeFailureTransactable(int succeedOnCount, String returnString) {
      this.succeedOnCount = succeedOnCount;
      this.returnString = returnString;
    }
    
    @Override
    public String run(Objectify ofy) {
      runCount++;
      if(succeedOnCount > runCount) {
        throw new ConcurrentModificationException(TOO_MUCH_FAKE_CONTENTION_MESSAGE);
      }
      return returnString;
    }
  }
  
  @Test
  public void test_repeatInTransaction() {

    // Fails for any crazy reason
    final String CRAZY_EXCEPTION_MESSAGE = "Crazy exception";
    try {
      ObjectifyUtils.repeatInTransaction(new Transactable<Void>() {@Override
        public Void run(Objectify ofy) {
          throw new RuntimeException(CRAZY_EXCEPTION_MESSAGE);
        }
      });
      fail("should have failed");
    } catch (RuntimeException e) {
      assertEquals(RuntimeException.class, e.getClass());
      assertEquals(CRAZY_EXCEPTION_MESSAGE, e.getMessage());
    }
    
    final String SAMPLE_RETURN_VALUE = "SAMPLE_RETURN_VALUE";
    // Success in the first attempt
    FakeFailureTransactable transactable = new FakeFailureTransactable(1, SAMPLE_RETURN_VALUE);
    assertEquals(SAMPLE_RETURN_VALUE, ObjectifyUtils.repeatInTransaction(transactable));
    assertEquals(transactable.runCount, 1);
    
    // Success in the second one
    transactable = new FakeFailureTransactable(2, SAMPLE_RETURN_VALUE);
    assertEquals(SAMPLE_RETURN_VALUE, ObjectifyUtils.repeatInTransaction(transactable));
    assertEquals(transactable.runCount, 2);
    
    // Success in the third one
    transactable = new FakeFailureTransactable(3, SAMPLE_RETURN_VALUE);
    assertEquals(SAMPLE_RETURN_VALUE, ObjectifyUtils.repeatInTransaction(transactable));
    assertEquals(transactable.runCount, 3);
    
    // Keep failing ...
    transactable = new FakeFailureTransactable(Integer.MAX_VALUE, SAMPLE_RETURN_VALUE);
    try {
      ObjectifyUtils.repeatInTransaction(transactable);
      fail("should have failed");
    } catch(ConcurrentModificationException e) {
      // expected
      assertEquals(TOO_MUCH_FAKE_CONTENTION_MESSAGE, e.getMessage());
    }
    assertEquals(transactable.runCount, LightConstants.OBJECTIFY_REPEAT_COUNT);
  }
}
