app = angular.module 'widow-analyze', ['wa.controllers', 'ui.router']

app.config ($stateProvider, $urlRouterProvider) ->

  $stateProvider.state 'home',
    url: '/home',
    templateUrl: 'templates/home.html',
    controller: 'HomeController'

  $stateProvider.state 'pageSummary',
    url: '/page/:id',
    templateUrl: 'templates/pageSummary.html',
    controller: 'PageSummaryController'

  $urlRouterProvider.otherwise '/home'