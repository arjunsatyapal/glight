/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.otxn;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */

import com.googlecode.objectify.Objectify;

import com.google.light.server.utils.ObjectifyUtils;

import java.util.ConcurrentModificationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.ObjectifyOpts;

/**
 * DAO that encapsulates a single transaction. Create it and forget about it.
 * Also provides very convenient static methods for making GAE/Python-like transactions.
 * 
 * @author Jeff Schnitzer
 */
public class DAOT {// extends DAO // DAO is your class derived from DAOBase as described above
  /** */
  private static final Logger log = LoggerFactory.getLogger(DAOT.class);

  /** Alternate interface to Runnable for executing transactions */
  public static interface Transactable {
    void run(Objectify ofy);
  }

  /** Create a default DAOT and run the transaction through it */
  public static void runInTransaction(Transactable t) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      t.run(ofy);
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * Run this task through transactions until it succeeds without an optimistic
   * concurrency failure.
   */
  public static void repeatInTransaction(Transactable t) {
    while (true) {
      try {
        runInTransaction(t);
        break;
      } catch (ConcurrentModificationException ex) {
        if (log.isWarnEnabled())
          log.warn("Optimistic concurrency failure for " + t + ": " + ex);
      }
    }
  }

//  /** Starts out with a transaction and session cache */
//  public DAOT() {
//    super(new ObjectifyOpts().setSessionCache(true).setBeginTransaction(true));
//  }

//  /** Adds transaction to whatever you pass in */
//  public DAOT(ObjectifyOpts opts) {
//    super(opts.setBeginTransaction(true));
//  }

//  /**
//   * Executes the task in the transactional context of this DAO/ofy.
//   */
//  public void doTransaction(final Runnable task) {
//    this.doTransaction(new Transactable() {
//      @Override
//      public void run(Objectify ofy) {
//        task.run();
//      }
//    });
//  }
//
//  /**
//   * Executes the task in the transactional context of this DAO/ofy.
//   */
//  public void doTransaction(Transactable task) {
//    Objectify ofy = ObjectifyUtils.initiateTransaction();
//    try {
//      task.run(ofy);
//      ObjectifyUtils.commitTransaction(ofy);
//    } finally {
//      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
//    }
//  }
}
