/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

  const CFL_UI_BASE = '/openmrs/owa/cfl/';
  const NODE_TYPE_ELEMENT = 1;
  const NODE_TYPE_TEXT = 3;
  const DATE_PICKER_PLACEHOLDER_REGEX = /\([dmy]{2,4}\/[dmy]{2,4}\/[dmy]{2,4}\)/g;

  window.addEventListener('load', () => {
     emr.loadMessages([
       'cfl.home.title',
       'coreapps.app.system.administration.label',
       'allergies.manageAllergies.title',
       'allergies.manageAllergies.subtitle',
       'cfl.emptyDashboardWidget.label',
       'coreapps.clinicianfacing.overallActions',
       'adminui.myAccount'
     ], () => applyCustomChanges());
  });

  function applyCustomChanges() {
    addBreadCrumbOnHomePage();
    translateMyAccountLabelInTopHeaderSection();
    addTitleOnHomeAndSystemAdministrationPages();
    replaceNoneLabelOnEmptyWidgets();
    redesignAllergyUI();
    setHomeBreadCrumbOnPatientDashboard();
    removeOccasionalUndefinedBreadCrumbs();
    moveAllWidgetsToFirstColumn();
    replaceURLsOnPatientDashboard();
    addHamburgerForGeneralActionsOnSmallerScreens();
    removeDatePickerPlaceholders();
    replaceURLsOnManageLocationsPage();
    overrideUserAccountLinks();
    redirectToCorrectFindPatientPage();
  }

  function addBreadCrumbOnHomePage() {
    const breadcrumbs = jq('#breadcrumbs');
    if (breadcrumbs.is(':empty')) {
      jq('#breadcrumbs').append('<span>' + emr.message('cfl.home.title') + '</span>');
    }
  }

  function translateMyAccountLabelInTopHeaderSection() {
    jq('#user-account-menu > li > a').text(emr.message('adminui.myAccount'));
  }

  function addTitleOnHomeAndSystemAdministrationPages() {
    const dashboard = jq('#body-wrapper > #content');
    if (!!dashboard.has('.row > #apps').length) {
      dashboard.prepend('<div class="homepage-heading">' + emr.message('cfl.home.title') +'</div>');
    } else if (!!dashboard.has('#tasks.row').length) {
      dashboard.prepend('<div class="homepage-heading">' + emr.message('coreapps.app.system.administration.label') + '</div>');
    }
  }

  function replaceNoneLabelOnEmptyWidgets() {
    // replace 'None' with '-NO DATA-' in each widget
    const noDataLabel = "<span class='label'>" + emr.message('cfl.emptyDashboardWidget.label') + "</span>";
    const emptyWidgetBody = `<div class='info-body empty'>${noDataLabel}</div>`;
    jq('.info-body').each((_, widgetBody) => {
      if (!jq(widgetBody).children().length || (jq(widgetBody).find('ul').length && !jq(widgetBody).find('ul > li').length)) {
        jq(widgetBody).replaceWith(emptyWidgetBody);
      }
    });
  }

  function redesignAllergyUI() {
    const allergies = document.querySelector('#allergies');
    if (!!allergies) {
      const title = document.querySelector('#content > h2');
      if (!!title) {
        title.parentElement.removeChild(title);
      }
      const addAllergyButton = document.querySelector('#allergyui-addNewAllergy');
      if (!!addAllergyButton) {
        addAllergyButton.parentElement.removeChild(addAllergyButton);
      }
      const cancelButton = document.querySelector('#content > button.cancel');
      if (!!cancelButton) {
        cancelButton.classList.add('btn');
      }

      const htmlLines = [
        '<div class="allergies-container">',
        '<div class="allergies-header">',
        '<h2>' + emr.message('allergies.manageAllergies.title') + '</h2>',
        '<span class="helper-text">' + emr.message('allergies.manageAllergies.subtitle') + '</span>',
        addAllergyButton.outerHTML,
        '</div>',
        allergies.outerHTML,
        '</div>'
      ];
      allergies.replaceWith(...htmlToElements(htmlLines.join('\n')));
     }
  }

  function setHomeBreadCrumbOnPatientDashboard() {
    jq('#breadcrumbs li:first-child a').after(emr.message('cfl.home.title'));
  }

  // OpenMRS bug: remove occasional (/undefined) from the System Administration breadcrumbs
  function removeOccasionalUndefinedBreadCrumbs() {
    setTimeout(function () {
      elementReady('#breadcrumbs li:last-child:not(:empty)').then(element => {
        element.textContent = element.textContent.replace('(/undefined)', '');
      });
    }, 100);
  }

  // move all the widgets to the first column
  function moveAllWidgetsToFirstColumn() {
    const firstInfoContainer = jq('.info-container:first-of-type');
    if (firstInfoContainer.length) {
      const remainingContainersChildren = jq('.info-container .info-section');
      remainingContainersChildren.detach().appendTo(firstInfoContainer);
    }
  }

  // replace the url of 'Patient profile', 'Caregiver profile' and 'Conditions'
  function replaceURLsOnPatientDashboard() {
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has('patientId')) {
      const givenName = document.querySelector('.PersonName-givenName')?.textContent;
      const middleName = document.querySelector('.PersonName-middleName')?.textContent;
      const familyName = document.querySelector('.PersonName-familyName')?.textContent;
      const fullName = [givenName, middleName, familyName].join(' ').replace('  ', ' ');
      const patientProfileAnchor = document.querySelector('a#cfl\\.patientProfile');
      const deletePerson =
        document.querySelector('#org\\.openmrs\\.module\\.coreapps\\.deletePatient') ||
        document.querySelector('#cfl\\.personDashboard\\.deletePerson');
      const uuidMatch = /'([^)]+)'/.exec(deletePerson?.href);
      const patientId = (uuidMatch && uuidMatch[1]) || searchParams.get('patientId');
      if (!!patientProfileAnchor) {
        patientProfileAnchor.href = `${CFL_UI_BASE}index.html#/edit-patient/${patientId}?redirect=${window.location.href}&name=${fullName}`;
      }
      const caregiverProfileAnchor = document.querySelector('a#cfl\\.caregiverProfile');
      if (!!caregiverProfileAnchor) {
        caregiverProfileAnchor.href = `${CFL_UI_BASE}index.html#/edit-caregiver/${patientId}?redirect=${window.location.href}&name=${fullName}`;
      }
      const conditionsAnchor = document.querySelector('a#cfl\\.overallActions\\.conditions');
      if (!!conditionsAnchor) {
        conditionsAnchor.href = `${CFL_UI_BASE}index.html#/conditions/${patientId}`;
      }
      const conditionsIcon = document.querySelector('.info-section.conditions i.edit-action');
      if (!!conditionsIcon) {
        conditionsIcon.setAttribute('onclick', `location.href = '${CFL_UI_BASE}index.html#/conditions/${patientId}'`);
      }
    }
  }

  // Add hamburger menu for general actions (visible on smaller screens)
  function addHamburgerForGeneralActionsOnSmallerScreens() {
    const actionContainer = jq('.action-container');
    if (actionContainer.length) {
      const actions = actionContainer.find('.action-section ul li');
      actionContainer.before(
        [
          '<div class="general-actions-toggle navbar-dark">',
          '<button class="navbar-toggler btn btn-secondary" type="button" data-toggle="collapse" data-target="#generalActions" aria-controls="generalActions" aria-expanded="false" aria-label="Toggle general actions">',
          '<span class="navbar-toggler-icon mr-1"></span><span>' + emr.message('coreapps.clinicianfacing.overallActions') + '</span>',
          '</button>',
          '<div class="collapse navbar-collapse" id="generalActions">',
          actions
            .toArray()
            .map(action => '<div>' + action.innerHTML + '</div>')
            .join('\n'),
          '</div>',
          '</div>'
        ].join('\n')
      );
    }
  }

  // HTML Forms bug: remove date picker placeholders - "(dd/mm/yyyy)" etc.
  function removeDatePickerPlaceholders() {
    const htmlForm = document.getElementById('htmlform');
    if (!!htmlForm) {
      if (htmlForm.nodeType === NODE_TYPE_TEXT) {
        htmlForm.data = htmlForm.data.replace(DATE_PICKER_PLACEHOLDER_REGEX, '');
      } else if (htmlForm.nodeType === NODE_TYPE_ELEMENT) {
        for (var i = 0; i < htmlForm.childNodes.length; i++) {
          removeDatePickerPlaceholders(htmlForm.childNodes[i]);
        }
      }
    }
  }

  // AGRE-15: replace URLs of 'Add New Location' and 'Edit' buttons on 'Manage Locations' page
  function replaceURLsOnManageLocationsPage() {
    if (window.location.href.includes('locations/manageLocations.page')) {
      const addNewLocationButton = document.querySelector('#content > a.button');
      if (addNewLocationButton) {
        addNewLocationButton.href = `${CFL_UI_BASE}index.html#/locations/create-location`;
      }
      const editLocationButtons = document.querySelectorAll('#content #list-locations .edit-action');
      if (editLocationButtons) {
        editLocationButtons.forEach(button => {
          const buttonOnClick = button.getAttribute('onclick');
          if (buttonOnClick && buttonOnClick.includes('locationId')) {
            const regexp = /(?<=locationId=).+(?=&)/;
            const locationId = buttonOnClick.match(regexp)[0];
            button.setAttribute('onclick', `location.href='${CFL_UI_BASE}index.html#/locations/edit-location/${locationId}'`);
          }
        });
      }
    }
  }

  function overrideUserAccountLinks() {
    if (window.location.href.includes('accounts/manageAccounts.page')) {
      const addNewUserAccount = document.querySelector('#content > a.button');
      const editUserAccount = document.querySelectorAll('#list-accounts .icon-pencil.edit-action');
      const pagination = document.querySelector('#list-accounts_wrapper > .datatables-info-and-pg');
      const accountFilterInput = document.querySelector('#list-accounts_filter input');

      if (addNewUserAccount) {
        addNewUserAccount.href = `${CFL_UI_BASE}index.html#/user-account`;
      }

      if (editUserAccount.length) {
        overrideEditUserAccountLinks(editUserAccount);
        pagination &&
          pagination.addEventListener('click', function () {
            overrideEditUserAccountLinks(document.querySelectorAll('#list-accounts .icon-pencil.edit-action'));
          });
        accountFilterInput &&
          accountFilterInput.addEventListener('input', function () {
            overrideEditUserAccountLinks(document.querySelectorAll('#list-accounts .icon-pencil.edit-action'));
          });
      }
    }
  }

  function overrideEditUserAccountLinks(editUserAccoutLinks) {
    editUserAccoutLinks.forEach(editUserAccoutLink => {
      const currentLocationHref = editUserAccoutLink.getAttribute('onclick');
      const personIdPosition = currentLocationHref.indexOf('personId=');
      const personId = currentLocationHref.slice(personIdPosition, currentLocationHref.length - 2);

      editUserAccoutLink.setAttribute('onclick', `location.href='${CFL_UI_BASE}index.html#/user-account?${personId}'`);
    });
  }

  //redirects the user to CfL find patient page instead of the default one
  function redirectToCorrectFindPatientPage() {
    const url = location.href;
    if (url.endsWith('app=coreapps.findPatient')) {
      window.location.href = '/openmrs/owa/cfl/index.html#/find-patient'
    }
  }

  function htmlToElements(htmlString) {
    var template = document.createElement('template');
    template.innerHTML = htmlString;
    return template.content.childNodes;
  }

  function elementReady(selector, parentElement = document, notEmpty = false) {
    return new Promise((resolve, reject) => {
      let el = parentElement.querySelector(selector);
      if (el && (!notEmpty || !!el.textContent)) {
        resolve(el);
      }
      new MutationObserver((mutationRecords, observer) => {
        // Query for elements matching the specified selector
        Array.from(parentElement.querySelectorAll(selector)).forEach(element => {
          if (!notEmpty || !!element.textContent) {
            resolve(element);
            // Once we have resolved we don't need the observer anymore.
            observer.disconnect();
          }
        });
      }).observe(parentElement === document ? document.documentElement : parentElement, {
        childList: true,
        subtree: true
      });
    });
  }