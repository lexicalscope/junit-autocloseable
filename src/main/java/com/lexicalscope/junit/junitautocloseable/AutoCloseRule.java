package com.lexicalscope.junit.junitautocloseable;

import java.lang.reflect.Field;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class AutoCloseRule implements MethodRule {
   @Override
   public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
      return new Statement() {
         @Override
         public void evaluate() throws Throwable {
            try {
               base.evaluate();
            } finally {
               closeAllAutoCloseableFields(target);
            }
         }
      };
   }

   private void closeAllAutoCloseableFields(final Object target) throws IllegalArgumentException, IllegalAccessException {
      Class<? extends Object> klass = target.getClass();
      while(!klass.equals(Object.class)) {
         for (final Field field : klass.getDeclaredFields()) {
            makeMethodAccessible(field);
            autocloseValue(field.get(target));
         }
         klass = klass.getSuperclass();
      }
   }

   private void autocloseValue(final Object fieldValue) {
      if(fieldValue != null) {
         if(fieldValue instanceof AutoCloseable) {
            try {
               ((AutoCloseable) fieldValue).close();
            } catch (final Exception e) {
               // suppressed
            }
         }
      }
   }

   private void makeMethodAccessible(final Field field) {
      try {
         field.setAccessible(true);
      } catch (final SecurityException e) { /* suppress */ }
   }
}
