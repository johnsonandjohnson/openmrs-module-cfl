package org.openmrs.module.cfldistribution.api.activator.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cfldistribution.api.metadata.UpdateGlobalParametersMetadata;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class UpdateGlobalParametersMetadataTest {

  @Mock private AdministrationService administrationService;

  @InjectMocks private UpdateGlobalParametersMetadata updateGlobalParametersMetadata;

  @Before
  public void setUp() {
    mockStatic(Context.class);
    when(Context.getAdministrationService()).thenReturn(administrationService);
  }

  @Test
  public void shouldReturnProperVersion() {
    int actual = updateGlobalParametersMetadata.getVersion();

    assertEquals(4, actual);
  }

  @Test
  public void shouldUpdateGlobalProperties() {
    new UpdateGlobalParametersMetadata().installNewVersion();

    verify(administrationService, times(8)).getGlobalPropertyObject(anyString());
  }
}
