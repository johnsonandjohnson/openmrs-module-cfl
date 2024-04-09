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
       'adminui.myAccount',
       'visits.manageVisitsBreadcrumb',
       'common.confirm',
       'common.cancel'
     ], () => applyCustomChanges());
  });

  function applyCustomChanges() {
    addBreadCrumbOnHomePage();
    translateMyAccountLabelInTopHeaderSection();
    addTitleOnHomeAndSystemAdministrationPages();
    redesignAllergyUI();
    setHomeBreadCrumbOnPatientDashboard();
    removeOccasionalUndefinedBreadCrumbs();
    moveAllWidgetsToFirstColumn();
    removeDatePickerPlaceholdersFromHtmlForms();
    replaceURLsOnManageLocationsPage();
    overrideUserAccountLinks();
    redirectToCorrectFindPatientPage();
    updateBreadcrumbsInHtmlForms();
    addSystemAdministrationBreadcrumbOnMissingPages();
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

     const allergy = document.querySelector('#allergy');
     if (!!allergy) {
      const saveAllergyButton = document.querySelector('#addAllergyBtn');
      jq(saveAllergyButton).val(emr.message('common.confirm'));
     }

    if (new URL(window.location.href).pathname.includes("/allergyui")) {
      const cancelButton = jq('form').last().prev()[0];
      jq(cancelButton).text(emr.message('common.cancel'));
    }
  }

  function setHomeBreadCrumbOnPatientDashboard() {
    const firstBreadcrumbElement = jq('#breadcrumbs li:first');
    const homeBreadcrumbElement = jq('<a>', {
        href: '/openmrs',
        text: emr.message('cfl.home.title')
    });

    homeBreadcrumbElement.insertBefore(firstBreadcrumbElement);
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

  // HTML Forms bug: remove date picker placeholders - "(dd/mm/yyyy)" etc.
  function removeDatePickerPlaceholdersFromHtmlForms() {
    const htmlForm = document.getElementById('htmlform');
    if (!!htmlForm) {
      removeDatePickerPlaceholders(htmlForm);
    }
  }

  function removeDatePickerPlaceholders(node) {
    if (node.nodeType === NODE_TYPE_TEXT) {
      node.data = node.data.replace(DATE_PICKER_PLACEHOLDER_REGEX, '');
    } else if (node.nodeType === NODE_TYPE_ELEMENT) {
      for (var i = 0; i < node.childNodes.length; i++) {
        removeDatePickerPlaceholders(node.childNodes[i]);
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

      overrideEditUserAccountLinks(document.querySelectorAll('#list-accounts .icon-pencil.edit-action'));

      watchElementMutations('#list-accounts', () => {
        overrideEditUserAccountLinks(document.querySelectorAll('#list-accounts .icon-pencil.edit-action'));
      }, document.getElementById('list-accounts_wrapper'));
    }
  }

  function overrideEditUserAccountLinks (editUserAccoutLinks) {
    editUserAccoutLinks.forEach(editUserAccoutLink => {
      const currentLocationHref = editUserAccoutLink.getAttribute('onclick');
      const personIdPosition = currentLocationHref.indexOf('personId=');

      if (personIdPosition !== -1) {
        const personId = readDigits(currentLocationHref.slice(personIdPosition + 'personId='.length, currentLocationHref.length));
        editUserAccoutLink.setAttribute('onclick', `location.href='${CFL_UI_BASE}index.html#/user-account?personId=${personId}'`);
      }
    });
  }

  function readDigits(text) {
    let result = '';
    for (var i = 0; i < text.length; i++) {
      let char = text.charAt(i);
      if( char >= '0' && char <= '9' ) {
        result += char;
      } else {
        break;
      }
    }
    return result;
  }

//redirects the user to CfL find patient page instead of the default one
  function redirectToCorrectFindPatientPage() {
    const url = location.href;
    if (url.endsWith('app=coreapps.findPatient')) {
      window.location.href = '/openmrs/owa/cfl/index.html#/find-patient'
    }
  }

  function updateBreadcrumbsInHtmlForms() {
    if (new URL(window.location.href).pathname.includes("/htmlformentryui/htmlform")) {
      const patientUuid = getPatientUuidParamFromURL();
      if (isUUID(patientUuid)) {
        setNameBreadcrumbUrl(patientUuid);
        addManageVisitsBreadcrumb(patientUuid);
      }
    }
  }

  function setNameBreadcrumbUrl(patientUuid) {
    const nameBreadcrumbElement = jq('#breadcrumbs li:nth-child(3)');
    nameBreadcrumbElement.find('a').attr('href', '/openmrs/coreapps/clinicianfacing/patient.page?patientId=' + patientUuid);
  }

  function addManageVisitsBreadcrumb(patientUuid) {
    const lastBreadcrumbElement = jq('#breadcrumbs li:last');
    const manageVisitBreadcrumbElement = jq('<li>').append(
      jq('<a>', {
        href: '/openmrs/owa/visits/index.html#/visits/manage/' + patientUuid,
        text: emr.message('visits.manageVisitsBreadcrumb') + ' '
      })
    );

    manageVisitBreadcrumbElement.insertBefore(lastBreadcrumbElement);
  }

  function addSystemAdministrationBreadcrumbOnMissingPages() {
    if (window.location.href.includes('adminui/metadata/locations/manageLocations.page') ||
        window.location.href.includes('adminui/metadata/configureMetadata.page')) {
      
      const homeBreadcrumb = jq('#breadcrumbs li:first');
      const systemAdministrationBreadcrumb = jq('<li>').append(
        jq('<a>', {
          href: '/openmrs/coreapps/systemadministration/systemAdministration.page',
          text: emr.message('coreapps.app.system.administration.label')
        })
      );

      homeBreadcrumb.after(systemAdministrationBreadcrumb);
    }
  }

  function getPatientUuidParamFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('patientId');
  }

  function isUUID(string) {
    const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;

    return uuidRegex.test(string);
  }

  function htmlToElements(htmlString) {
    var template = document.createElement('template');
    template.innerHTML = htmlString;
    return template.content.childNodes;
  }

  /**
   * Waits for an element satisfying selector to exist, then resolves promise with the element.
   * Useful for resolving race conditions.
   *
   * @param selector
   * @param parentElement
   * @param notEmpty
   * @returns {Promise}
   */
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

  /**
   * Observes changes to DOM starting from {@code parentElement} and its children, then calls {@code callback} on all elements
   * fitting {@code selector} once a change is detected.
   *
   * @param selector
   * @param callback
   * @param parentElement
   */
  function watchElementMutations(selector, callback, parentElement = document) {
    new MutationObserver((mutationRecords, observer) => {
      // Query for elements matching the specified selector
      Array.from(parentElement.querySelectorAll(selector)).forEach(element => {
        if (!!element.textContent) {
      callback(element);
    }
    });
    }).observe(parentElement === document ? document.documentElement : parentElement, {
      childList: true,
      subtree: true
    });
  }
