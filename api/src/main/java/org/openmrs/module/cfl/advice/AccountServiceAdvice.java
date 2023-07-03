/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.advice;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.api.APIException;
import org.openmrs.module.adminui.account.Account;
import org.openmrs.module.adminui.account.AccountService;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import static java.util.Optional.ofNullable;

/**
 * The OpenMRS AOP advice applied to {@link AccountService#getAllAccounts()} to remove disabled
 * accounts from the result. The list is limited so the administration ui's manage account page
 * would contain only active accounts.
 */
public class AccountServiceAdvice implements AfterReturningAdvice {
  private static final String GET_ALL_ACCOUNTS_METHOD_NAME = "getAllAccounts";

  @Override
  public void afterReturning(Object returnValue, Method method, Object[] args, Object target)
      throws Throwable {
    if (target instanceof AccountService
        && GET_ALL_ACCOUNTS_METHOD_NAME.equals(method.getName())
        && method.getParameterCount() == 0) {
      removeDisabledAccounts(new InvocationContext(returnValue, method, args, target));
    }
  }

  private void removeDisabledAccounts(InvocationContext invocationContext) {
    if (!(invocationContext.returnValue instanceof Collection)) {
      throw newResultNotCollectionException(invocationContext);
    }

    final Collection<?> originalResult = (Collection) invocationContext.returnValue;
    final Iterator<?> originalResultIterator = originalResult.iterator();

    while (originalResultIterator.hasNext()) {
      final Object originalResultItem = originalResultIterator.next();

      if (!(originalResultItem instanceof Account)) {
        newResultItemNotAccountException(invocationContext, originalResultItem);
      }

      if (isDisabled((Account) originalResultItem)) {
        originalResultIterator.remove();
      }
    }
  }

  /**
   * Disabled account is an account which has all related Users retired.
   *
   * @param account the account to check, not null
   * @return true if account is disabled
   */
  private boolean isDisabled(Account account) {
    return account.getUserAccounts().stream()
        .map(BaseOpenmrsMetadata::getRetired)
        .reduce(true, (acc1, acc2) -> acc1 && acc2);
  }

  private String getFullyQualifiedMethodName(Method method) {
    return method.getDeclaringClass().getName() + "#" + method.getName();
  }

  private APIException newResultNotCollectionException(InvocationContext invocationContext) {
    return new APIException(
        "Method result is not a Collection for AccountServiceAdvice on method: "
            + getFullyQualifiedMethodName(invocationContext.method)
            + ", found type: "
            + ofNullable(invocationContext.returnValue)
                .map(Object::getClass)
                .map(Class::getName)
                .orElse("null"));
  }

  private APIException newResultItemNotAccountException(
      InvocationContext invocationContext, Object originalResultItem) {
    throw new APIException(
        "Method result is a Collection of unsupported types for AccountServiceAdvice on method: "
            + getFullyQualifiedMethodName(invocationContext.method)
            + ", found: "
            + ofNullable(originalResultItem)
                .map(Object::getClass)
                .map(Class::getName)
                .orElse("null"));
  }

  private static class InvocationContext {
    final Object returnValue;
    final Method method;
    final Object[] args;
    final Object target;

    private InvocationContext(Object returnValue, Method method, Object[] args, Object target) {
      this.returnValue = returnValue;
      this.method = method;
      this.args = args.clone();
      this.target = target;
    }
  }
}
