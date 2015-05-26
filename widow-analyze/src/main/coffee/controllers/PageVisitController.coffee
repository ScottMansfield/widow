controllers = angular.module 'wa.controllers'

controllers.controller 'PageVisitController', ($scope, $stateParams, $http, $modal, Encoding) ->

  # defaults
  $scope.visitInfo = null

  $scope.visitProps = [
    {propKey: 'TITLE',            display: 'Page Title'}
    {propKey: 'ORIGINAL_URL',     display: 'Original URL'}
    {propKey: 'REFERRER',         display: 'Referrer'}
    {propKey: 'TIME_ACCESSED',    display: 'Time Accessed'}
    {propKey: 'STATUS_CODE',      display: 'Status Code'}
    {propKey: 'LOAD_TIME_MILLIS', display: 'Load Time (ms)'}
    {propKey: 'CONTENT_SIZE',     display: 'Content Size'}
    {propKey: 'RESPONSE_SIZE',    display: 'Response Size'}
    {propKey: 'SIZE_WITH_ASSETS', display: 'Size Including Assets'}
    {propKey: 'PAGE_CONTENT_REF', display: 'Content Reference ID'}
  ]

  $scope.customProps = [
    {propKey: 'HEADERS',   display: 'Headers'}
  ]

  $scope.linkProps = [
    {propKey: 'OUT_LINKS', display: 'Links to other pages'}
    {propKey: 'CSS_LINKS', display: 'CSS Links'}
    {propKey: 'JS_LINKS',  display: 'Javascript Links'}
    {propKey: 'IMG_LINKS', display: 'Image Links'}
  ]

  $scope.visitInfo = { }
  $scope.pageUrl = Encoding.decode $stateParams.id
  $scope.visitTime = new Date(Number($stateParams.time)).toString()

  $scope.showRawContents = ->

    return unless $scope.visitInfo?.PAGE_CONTENT_REF

    $modal.open {
      templateUrl: 'templates/modals/rawContentModal.html'
      controller: 'RawContentModalController'
      size: 'fit',
      resolve: {
        contentID: -> $scope.visitInfo.PAGE_CONTENT_REF
      }
    }

  $http.get("REST/pages/#{$stateParams.id}/#{$stateParams.time}")
    .success (data, status, headers, config) ->
      console.log data
      $scope.visitInfo = data.pageInfo
    .error (data, status, headers, config) ->
      console.log "Error: Status #{status} | data: #{data}"
      $scope.visitInfo = { }