// via: http://docs.angularjs.org/tutorial/step_07
var app = angular.module('cowsAreRuminantsApp', 
  ['ngRoute', 'cowsAreRuminantsControllers']
);

// define routes
app.config(['$routeProvider', function($routeProvider) {
  $routeProvider.

    when('/', {
      templateUrl: 'partials/splash.html',
      controller: 'SplashCtl'
    }).

    when('/intro', {
      templateUrl: 'partials/intro.html',
      controller: 'IntroCtl'
    }).

    when('/main', {
      templateUrl: 'partials/main.html',
      controller: 'MainCtl'
    }).

    when('/questionnaire', {
      templateUrl: 'partials/questionnaire.html',
      controller: 'QuestionnaireCtl'
    }).

    otherwise({
      redirectTo: '/'
    });

}]);