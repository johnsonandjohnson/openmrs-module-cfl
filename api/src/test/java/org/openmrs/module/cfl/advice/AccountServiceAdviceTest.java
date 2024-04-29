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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.adminui.account.Account;
import org.openmrs.module.adminui.account.AccountService;
import org.openmrs.test.BaseContextMockTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountServiceAdviceTest extends BaseContextMockTest {
  private static int idGenerator = 0;

  @Mock private AccountService accountService;
  @Mock private UserService userService;
  @Mock private ProviderService providerService;

  @Test
  public void shouldRemoveDisabledAccount() throws Throwable {
    final List<UserAndPerson> userAndPersons = new ArrayList<>();
    userAndPersons.add(prepareUserAndPerson("userA", false));
    userAndPersons.add(prepareUserAndPerson("userB", false));
    userAndPersons.add(prepareUserAndPerson("retiredUser", true));

    Mockito.when(
            providerService.getProvidersByPerson(Mockito.any(Person.class), Mockito.anyBoolean()))
        .thenReturn(Collections.emptyList());
    Mockito.when(userService.getUsersByPerson(Mockito.any(Person.class), Mockito.anyBoolean()))
        .thenAnswer(
            invocationOnMock -> {
              for (UserAndPerson userAndPerson : userAndPersons) {
                if (userAndPerson.person.equals(invocationOnMock.getArguments()[0])) {
                  return Collections.singletonList(userAndPerson.user);
                }
              }
              return Collections.emptyList();
            });

    final List<Account> accounts =
        userAndPersons.stream()
            .map(userAndPerson -> new Account(userAndPerson.person))
            .collect(Collectors.toList());

    final AccountServiceAdvice advice = new AccountServiceAdvice();
    advice.afterReturning(
        accounts, AccountService.class.getMethod("getAllAccounts"), new Object[0], accountService);

    Assert.assertEquals(
        "Should remove exactly one element.", userAndPersons.size() - 1, accounts.size());
    Assert.assertEquals(
        "There should not be any account with retired user.",
        0,
        accounts.stream()
            .map(Account::getUserAccounts)
            .flatMap(List::stream)
            .filter(BaseOpenmrsMetadata::getRetired)
            .count());
  }

  private UserAndPerson prepareUserAndPerson(String name, boolean retiredUser) {
    final User user = new User(++idGenerator);
    user.setUsername(name);
    user.setRetired(retiredUser);

    return new UserAndPerson(user, new Person(++idGenerator));
  }

  private static class UserAndPerson {
    final User user;
    final Person person;

    private UserAndPerson(User user, Person person) {
      this.user = user;
      this.person = person;
    }
  }
}
