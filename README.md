[![Build Status](https://travis-ci.org/LoyolaChicagoCode/falgebra-explorations-scala.svg?branch=master)](https://travis-ci.org/LoyolaChicagoCode/droste-explorations-scala)
[![codecov](https://codecov.io/gh/LoyolaChicagoCode/falgebra-explorations-scala/branch/master/graph/badge.svg)](https://codecov.io/gh/LoyolaChicagoCode/falgebra-explorations-scala)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9f077fda29704e5d84691c2362bb637e)](https://www.codacy.com/app/laufer/falgebra-explorations-scala?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=LoyolaChicagoCode/droste-explorations-scala&amp;utm_campaign=Badge_Grade)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)

[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/LoyolaChicagoCode/falgebra-explorations-scala.svg)](http://isitmaintained.com/project/LoyolaChicagoCode/falgebra-explorations-scala "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/LoyolaChicagoCode/falgebra-explorations-scala.svg)](http://isitmaintained.com/project/LoyolaChicagoCode/falgebra-explorations-scala "Percentage of issues still open")


Basic examples of recursive structures and behaviors based on F-algebras 
(see also [this introduction](https://bartoszmilewski.com/2013/06/10/understanding-f-algebras).

These examples use Scala's higher-kinded types directly instead of relying on any third-party libraries for category theory or recursion schemes.

This project also illustrates property-based testing using [ScalaCheck](https://www.scalacheck.org).

To run the tests:

      sbt test
