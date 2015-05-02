app = angular.module 'widow-analyze', ['wa.controllers', 'ui.router']

app.run ($rootScope) ->

  $rootScope.navlinks = [
    {sref: 'home' , text: 'Home' }
    {sref: 'about', text: 'About'}
  ]

app.config ($stateProvider, $urlRouterProvider) ->

  $stateProvider.state 'home',
    url: '/home',
    templateUrl: 'templates/home.html',
    controller: 'HomeController'

  $stateProvider.state 'pageSummary',
    url: '/page/:id',
    templateUrl: 'templates/pageSummary.html',
    controller: 'PageSummaryController'

  $stateProvider.state 'pageVisit',
    url: '/page/:id/:time',
    templateUrl: 'templates/pageVisit.html',
    controller: 'PageVisitController'

  $urlRouterProvider.otherwise '/home'