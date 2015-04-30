app = angular.module 'widow-analyze', ['wa.controllers', 'ui.router']

app.config ($stateProvider, $urlRouterProvider) ->

  $stateProvider.state 'home',
    url: '/home',
    templateUrl: 'templates/home.html',
    controller: 'HomeController'

  $urlRouterProvider.otherwise '/home'