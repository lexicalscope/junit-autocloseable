package com.lexicalscope.junit.junitautocloseable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runners.model.Statement;

public class AutoCloseRuleTest {
   private static final class MyException extends RuntimeException {}

   private boolean statementInvoked;
   private final Statement doNothingStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
         statementInvoked = true;
      }
   };

   private final Statement throwExceptionStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
         throw new MyException();
      }
   };

   private boolean closeableClosed;
   private final AutoCloseable autoCloseable = new AutoCloseable() {
      @Override
      public void close() throws Exception {
         closeableClosed = true;
      }
   };

   @Test public void invokesStatement() throws Throwable {
      evaluateRule(new Object());
      assertThat("delegate statement was invoked", statementInvoked);
   }

   @Test public void closesPrivateField() throws Throwable {
      evaluateRule(new Object() {
         @SuppressWarnings("unused")
         private final AutoCloseable myCloseable = autoCloseable;
      });
      assertThat("closeable in private field was closed", closeableClosed);
   }

   @Test public void closesParentField() throws Throwable {
      class Parent {
         @SuppressWarnings("unused")
         private final AutoCloseable myCloseable = autoCloseable;
      }
      evaluateRule(new Parent() {});
      assertThat("closeable in parent field was closed", closeableClosed);
   }

   @Test public void closeExceptionIsSupressed() throws Throwable {
      evaluateRule(new Object() {
         @SuppressWarnings("unused")
         private final AutoCloseable autoCloseable = new AutoCloseable() {
            @Override
            public void close() throws Exception {
               throw new RuntimeException();
            }
         };
      });
   }

   @Test public void closesFieldWhenStatementTerminatesWithException() throws Throwable {
      try {
         new AutoCloseRule().apply(throwExceptionStatement, null, new Object() {
            @SuppressWarnings("unused")
            private final AutoCloseable myCloseable = autoCloseable;
         }).evaluate();
         fail("fake test case should have thrown an exception");
      } catch (final MyException e) {
         assertThat("closeable was closed although test threw exception", closeableClosed);
      }
   }

   private void evaluateRule(final Object target) throws Throwable {
      new AutoCloseRule().apply(doNothingStatement, null, target).evaluate();
   }
}
