waModals = angular.module 'wa.modals'

waModals.controller 'RawContentModalController', ($scope, $modalInstance, $http, contentID) ->
  $scope.contentID = contentID

  $http.get("/REST/pages/rawContent/#{contentID}")
    .success (data) ->
      $scope.rawContent = data.rawContent


  $scope.ok = ->
    $modalInstance.close('success')
