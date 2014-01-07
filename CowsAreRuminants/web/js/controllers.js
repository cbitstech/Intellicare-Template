// controllers.js

// var app = angular.module('cowsAreRuminantsApp', []);
// app.controller('SplashCtl', function($scope) {
//   $scope.splash = {
//     image: {
//       alt: "Cartoon Cow",
//       class: "splash",
//       url: "assets/free-cartoon-cow-clip-art.jpg"
//     }
//   };
// });


var cowsAreRuminantsControllers = angular.module('cowsAreRuminantsControllers', []);





// Splash controller
cowsAreRuminantsControllers.controller('SplashCtl', ['$scope', '$http',
  function($scope, $http) {
    $scope.splash = {
      image: {
        alt: "Cartoon Cow",
        class: "splash",
        url: "assets/free-cartoon-cow-clip-art.jpg"
      },
      nav: {
        back: { url: "#", title: "Back" },
        next: { url: "#/intro", title: "Next" }
      }
    };
  }
]);





// Intro controller
cowsAreRuminantsControllers.controller('IntroCtl', ['$scope', '$routeParams',
  function($scope, $routeParams) {
    $scope.intro = {
      image: {
        alt: "Cartoon Cow",
        class: "splash",
        url: "assets/free-cartoon-cow-clip-art.jpg"
      },
      nav: {
        back: { url: "#", title: "Back" },
        next: { url: "#/main", title: "Next" }
      }
    };
  }
]);





// Main controller
cowsAreRuminantsControllers.controller('MainCtl', ['$scope', '$routeParams',
  function($scope, $routeParams) {
    $scope.main = {
      nav: {
        ruminating: { url: "#/questionnaire", title: "Ruminating? Tap here!" },
        lessons: { url: "#/lessons", title: "Lessons" },
        define: { url: "#/define", title: "Define Rumination Events" },
        strategies: { url: "#/strategies", title: "Strategies" },
        intro: { url: "#/intro", title: "Replay Intro" }
      }
    };
  }
]);





// Questionnaire controller
cowsAreRuminantsControllers
  .controller('QuestionnaireCtl', ['$scope', '$routeParams',
    function($scope, $routeParams) {

      $scope.selectedSeverity = null;
      $scope.severities = _.map(_.range(1,8), function(severity) { return { severity: severity }; });
      // console.log($scope.severities);

      $scope.questionnaire = {
        content: {
          isRuminating: {
            interrogations: [
              {
                question: "Are you ruminating?",
                answers: [ "Yes", "No" ],
                uiType: "radio"
              },
              {
                question: "What are you ruminating about?",
                answers: [],
                uiType: "text"
              },
              {
                question: "Do you have time for some help?",
                answers: [ "No", "Yes, Lessons (easy)", "Yes, identify times of events about which I ruminate", "Yes, help me learn strategies for coping" ],
                uiType: "radio"
              }
            ]
          }
        },
        nav: {
          back: { url: "#", title: "Back" },
          next: { url: "#/main", title: "Next" }
        },
        fn: {
          // passthrough: function(x) { return x; },
          // makeOptions: function() { 
          //   console.log('in makeOptions'); 
          //   var opts = makeOption("severity", _.range(1,8), 7, this.passthrough); 
          //   console.log('[makeOptions] opts = ' + opts); 
          //   return opts;
          // },
          // genResponseFields: function(interrogation) {
          //   var inputFieldHtml = "";
            
          //   console.log(interrogation.uiType);
            
          //   if(interrogation.uiType == "radio") {
          //     inputFieldHtml += '<input name="" type="radio">';
          //     inputFieldHtml += 
          //       _.reduce(
          //         _.map(
          //           interrogation.answers,
          //           function(a, i) {
          //             return '<option name="' + i + '_' + a + '" value="' + a + '">' + a + '</option>';
          //         }),
          //         function(memo, i) {
          //           return memo + i;
          //       });
          //     inputFieldHtml += '</input>';
          //   }

          //   if(interrogation.uiType == "text") {
          //     inputFieldHtml += '<input type="text"></input>';
          //   }

          //   return inputFieldHtml;
          // }
        }
      };

    }
  ])

  // .directive('myOption', function($interval, $scope) {

    // function genOptions(scope, element, attrs) {

    //   var  rcvdValue
    //       ,timeoutId;

    //   function updateTime() {
    //     element.text(rcvdValue);
    //   }

    //   scope.$watch(attrs.myOption, function(value) {
    //     rcvdValue = value;
    //     updateTime();
    //   });

    //   element.on('$destroy', function() {
    //     $interval.cancel(timeoutId);
    //   });

    //   // start the UI update process, save the timeoutId for canceling
    //   timeoutId = $interval(function() {
    //     updateTime();     // update DOM
    //   }, 1000);

    // }

    // return {
    //   genOptions: genOptions
    // };
    



    // $scope.init = function() {
    //   console.log('in init');
    // };
    



    // $scope.$on('$viewContentLoaded', function() {
    //   //call it here
    //   console.log('in viewContentLoaded')
    // });
    
    // return {
    //   template: "<option>" + 
    // };
    
  //   $scope.options = []
  // })
;

